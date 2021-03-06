(ns lreddit.components.thread
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [om-tools.core :refer-macros [defcomponent]]
            [cljs.core.async :refer [put! chan <! alts!]]
            [clojure.string :as str]
            [lreddit.components.routed-link :refer [routed-link]]
            [lreddit.reddit-api :as reddit]
            [lreddit.util :as util :refer [unescape-html]]))

(declare load-more-replies)

(defn get-replies [comment]
  (if (str/blank? (:replies comment))
    []
    (get-in comment [:replies :data :children])))

(defn has-more-replies? [reply] (= (:kind reply) "more"))

(defn handle-chevron-click [e owner state]
  (om/set-state! owner :replies-visible? (not (:replies-visible? state))))

(defcomponent reply-view [reply owner]
  (init-state [_]
    {:replies-visible? true})
  (render-state [_ {:keys [replies-visible?] :as state}]
    (let [{:keys [author body_html id]} (:data reply)
          unescaped-body (util/unescape-html body_html)
          replies (get-replies (:data reply))
          glyph-class (str "glyphicon glyphicon-chevron-" (if replies-visible? "down" "right"))
          hide-class (if replies-visible? "" "hide")]
      (html (if (has-more-replies? reply)
          [:li {:class "more" :on-click #(load-more-replies reply)} "load more"]
          [:li {:class "reply"}
            [:div {:class "author"} [:i {:on-click #(handle-chevron-click % owner state)
                                         :class glyph-class}] author]
            [:div {:class (str "body " hide-class)
                   :dangerouslySetInnerHTML {:__html unescaped-body}}]
            [:ul {:class (str "replies " hide-class)}
              (om/build-all reply-view replies)]])))))

(defcomponent thread-view [app owner]
  (will-mount [_]
    (defn load-more-replies [reply]
      (let [reply-id (get-in @reply [:data :id])
            sub (:subreddit @app)
            post-id (:post-id @app)]
        (go (let [new-replies (<! (reddit/get-replies sub post-id reply-id))]
          (om/transact! reply
            (fn [r] (assoc r :kind "Listing" :data (:replies new-replies))))))))
    (go (let [post (<! (reddit/get-post (:post-id @app)))]
      (om/update! app :post post))))
  (render [_]
    (let [{:keys [author title selftext_html url]} (get-in app [:post :parent])
          body (if (nil? selftext_html) url (util/unescape-html selftext_html))
          replies (get-in app [:post :replies])]
      (html [:div {:id "comments-page"}
        (if (empty? author) [:span "Loading..."])
        [:div {:class "comment"}
          [:h3 {:class "title"} title]
          [:div {:class "author"} (if (not-empty author) (str "by " author))]
          [:div {:class "body" :dangerouslySetInnerHTML {:__html body}}]]
        [:ul {:class "replies"} (om/build-all reply-view replies)]]))))

