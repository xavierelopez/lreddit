(ns reddit.components.main
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [om-tools.core :refer-macros [defcomponent]]
            [reddit.router :as router]
            [reddit.components.routed-link :refer [routed-link]]))


(defn build-sub-item [sub]
  (let [href (router/route-sub {:sub sub})]
    (html [:li (om/build routed-link {:title sub :href href})])))

(defcomponent subreddit-list-view [subs]
  (render [_]
    (html [:ul {:class "subreddits"} (map build-sub-item subs)])))

(defcomponent main-view [app owner]
  (render [_]
    (html [:div {:id "main"}
      [:h1 "Enter subreddit"]
      (om/build subreddit-list-view (:subreddits app))])))
