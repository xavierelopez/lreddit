(ns reddit.components.post
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [om-tools.core :refer-macros [defcomponent]]
            [cljs.core.async :refer [put! chan <! alts!]]
            [reddit.reddit-api :as reddit]
            [reddit.components.routed-link :refer [routed-link]]))


(defcomponent post [app owner]
  (will-mount [_]
    (go (let [post (<! (reddit/get-post (:post-id @app)))]
      (om/update! app :post post))))
  (render [_]
    (let [{:keys [author title selftext]} (get-in app [:post :parent])]
      (html [:div {:id "post"}
        [:h2 "Post"]
        [:h3 title]
        [:span (str "by " author)]
        [:p selftext]]))))
