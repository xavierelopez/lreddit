(ns reddit.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [cljs.core.async :refer [put! chan <! alts! timeout]]
            [secretary.core :as secretary :include-macros true :refer [defroute]]
            [sablono.core :as html :refer-macros [html]]
            [reddit.components.main :refer [main header]]
            [reddit.components.subreddit :refer [subreddit]]
            [reddit.components.post :refer [post]]
            [reddit.router :as router]
            [goog.events :as events]
            [goog.history.EventType :as EventType])
  (:import goog.History))

(enable-console-print!)

(def app (atom {:post ""
                :post-id nil
                :posts []
                :view nil
                :subreddit nil
                :subreddits ["askreddit" "asksciencefiction" "truereddit" "iama"]
                :filters ["hot" "new" "top"]
                :filter-times ["today" "week" "month" "year" "all"]
                :selected-filter {:name nil, :time nil}}))


(router/start app)

(defcomponent root [app owner]
  (render [_]
    (let [view (:view app)]
      (html [:div {:id "page"}
        (om/build
         (condp = view
           :main main
           :sub subreddit
           :post post
           main) app)]))))

(om/root
  root
  app
  {:target (. js/document (getElementById "app"))})
