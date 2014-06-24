(ns reddit.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [cljs.core.async :refer [put! chan <! alts! timeout]]
            [reddit.components.main :refer [main]]))

(enable-console-print!)

(def app (atom {:comment ""
                :post-id nil
                :posts []
                :view :main
                :subreddit "askreddit"
                :subreddits ["askreddit" "asksciencefiction" "truereddit" "iama"]
                :filters ["hot" "new" "top"]
                :filter-times ["week" "month" "year" "all"]
                :selected-filter {:name "hot", :time "week"}}))


(defcomponent root [app owner]
  (render [_]
    (let [view (:view app)]
      (om/build
       (condp = view
         :main main
         (om/div nil "Empty")) app))))

(om/root
  root
  app
  {:target (. js/document (getElementById "app"))})
