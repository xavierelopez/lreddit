(ns reddit.components.main
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [om-tools.core :refer-macros [defcomponent]]
            [reddit.components.routed-link :refer [routed-link]]))

(defcomponent subreddit-list [subs]
  (render-state [_ {:keys [events]}]
    (let [build-li (fn [sub] (om/build routed-link {:title sub :href (str "r/" sub)}))]
      (html [:ul {:class "subreddits"} (map build-li subs)]))))

(defcomponent main [app owner]
  (render [_]
    (html [:div {:id "main"}
      [:h1 "Enter subreddit"]
      (om/build subreddit-list (:subreddits app))])))
