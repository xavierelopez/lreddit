(ns lreddit.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [put! chan <! alts! timeout]]
            [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]
            [lreddit.components.main :refer [main-view]]
            [lreddit.components.subreddit :refer [subreddit-view]]
            [lreddit.components.thread :refer [thread-view]]
            [lreddit.router :as router]))

(enable-console-print!)

(def app (atom {:post nil
                :post-id nil
                :posts []
                :view nil
                :subreddit nil
                :subreddits ["askreddit" "asksciencefiction" "truereddit" "iama"]
                :filters ["hot" "new" "top" "rising"]
                :filter-times ["hour" "week" "month" "year" "all"]
                :selected-filter {:name "hot", :time nil}}))


(defcomponent root-view [app owner]
  (will-mount [_]
    (router/start app))
  (render [_]
    (html [:div {:id "page"}
      (om/build
       (condp = (:view app)
         :main main-view
         :sub subreddit-view
         :thread thread-view
         main-view) app)])))

(om/root
  root-view
  app
  {:target (. js/document (getElementById "app"))})
