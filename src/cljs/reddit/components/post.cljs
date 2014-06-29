(ns reddit.components.post
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [om-tools.core :refer-macros [defcomponent]]
            [cljs.core.async :refer [put! chan <! alts!]]
            [reddit.reddit-api :as reddit]
            [clojure.string :as str]
            [reddit.components.routed-link :refer [routed-link]]))

(defn unescape-html [html-text]
  (if (nil? html-text)
    ""
    (-> html-text
        (str/replace "&amp;" "&")
        (str/replace "&lt;" "<")
        (str/replace "&gt;" ">")
        (str/replace "&quot;" "\""))))

(defn get-replies [comment]
  (if (= (:replies comment) "")
    []
    (map :data (get-in comment [:replies :data :children]))))

(defcomponent reply-view [reply owner]
  (render [_]
    (let [{:keys [author body_html]} reply
          unescaped-body (unescape-html body_html)
          replies (get-replies reply)]
      (html [:li {:class "reply"}
        [:div {:class "author"} author]
        [:div {:class "body" :dangerouslySetInnerHTML {:__html unescaped-body}}]
        [:ul {:class "replies"} (om/build-all reply-view replies)]]))))

(defcomponent post [app owner]
  (will-mount [_]
    (go (let [post (<! (reddit/get-post (:post-id @app)))]
      (om/update! app :post post))))
  (render [_]
    (let [{:keys [author title selftext_html url]} (get-in app [:post :parent])
          body (if (nil? selftext_html) url (unescape-html selftext_html))
          replies (get-in app [:post :replies])]
      (html [:div {:id "comments-page"}
        (if (empty? author) [:span "Loading..."])
        [:div {:class "comment"}
          [:h3 {:class "title"} title]
          [:div {:class "author"} (if (not-empty author) (str "by " author))]
          [:div {:class "body" :dangerouslySetInnerHTML {:__html body}}]]
        [:ul {:class "replies"} (om/build-all reply-view replies)]]))))

