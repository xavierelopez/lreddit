(ns reddit.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [reddit.components.main :refer [main]]))

(enable-console-print!)

(def app (atom {:comment ""
                :post-id nil
                :posts []
                :subreddit "askreddit"
                :subreddits ["askreddit" "asksciencefiction" "truereddit" "iama"]
                :filters ["hot" "new" "top"]
                :filter-times ["week" "month" "year" "all"]
                :selected-filter {:name "hot", :time "week"}}))

(om/root
  main
  app
  {:target (. js/document (getElementById "app"))})
