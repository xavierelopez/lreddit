(ns reddit.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [cljs.core.async :refer [put! chan <! alts! timeout]]
            [secretary.core :as secretary :include-macros true :refer [defroute]]
            [reddit.components.main :refer [main]]
            [goog.events :as events]
            [goog.history.EventType :as EventType])
  (:import goog.History))

(enable-console-print!)

(def app (atom {:comment ""
                :post-id nil
                :posts []
                :view nil
                :subreddit "askreddit"
                :subreddits ["askreddit" "asksciencefiction" "truereddit" "iama"]
                :filters ["hot" "new" "top"]
                :filter-times ["week" "month" "year" "all"]
                :selected-filter {:name "hot", :time "week"}}))

; Router setup

(defroute "/" [] (swap! app assoc :view :main))

(let [h (History.)]
  (goog.events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
  (doto h (.setEnabled true)))

; End router setup

(defcomponent root [app owner]
  (render [_]
    (let [view (:view app)]
      (om/build
       (condp = view
         :main main) app))))

(om/root
  root
  app
  {:target (. js/document (getElementById "app"))})
