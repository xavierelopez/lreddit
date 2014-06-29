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
          replies (get-replies reply)]
      (html [:li {:class "reply"}
        [:div {:class "reply-author"} author]
        [:p {:class "reply-body" :dangerouslySetInnerHTML {:__html (unescape-html body_html)}}]
        [:ul {:class "reply-replies"} (om/build-all reply-view replies)]]))))

(defcomponent post [app owner]
  (will-mount [_]
    (go (let [post (<! (reddit/get-post (:post-id @app)))]
      (om/update! app :post post))))
  (render [_]
    (let [{:keys [author title selftext_html]} (get-in app [:post :parent])
          replies (get-in app [:post :replies])]
      (html [:div {:id "post"}
        [:div {:class "main-parent"}
          [:h3 {:class "post-title"} title]
          [:span {:class "post-author"} (str "by " author)]
          [:p {:class "post-body" :dangerouslySetInnerHTML {:__html (unescape-html selftext_html)}}]]
        [:ul {:class "replies"} (om/build-all reply-view replies)]]))))

