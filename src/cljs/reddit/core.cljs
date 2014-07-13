(ns reddit.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [put! chan <! alts! timeout]]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]
            [secretary.core :as secretary :include-macros true :refer [defroute]]
            [reddit.components.main :refer [main-view]]
            [reddit.components.subreddit :refer [subreddit-view]]
            [reddit.components.thread :refer [thread-view]]
            [reddit.router :as router])
  (:import goog.History))

(enable-console-print!)

(def app (atom {:post nil
                :post-id nil
                :posts []
                :view nil
                :subreddit nil
                :subreddits ["askreddit" "asksciencefiction" "truereddit" "iama"]
                :filters ["hot" "new" "top"]
                :filter-times ["week" "month" "year" "all"]
                :selected-filter {:name nil, :time nil}}))


(router/start app)

(defcomponent root [app owner]
  (render [_]
    (let [view (:view app)]
      (html [:div {:id "page"}
        (om/build
         (condp = view
           :main main-view
           :sub subreddit-view
           :thread thread-view
           main-view) app)]))))

(om/root
  root
  app
  {:target (. js/document (getElementById "app"))})
