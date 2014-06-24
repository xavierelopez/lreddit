(ns reddit.reddit-api
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [put! chan <!]]))


(def base-url "http://www.reddit.com")

(defn get-subreddit-posts [sub filter-info]
  (let [channel (chan)
        request-url (str base-url "/r/" sub "/" (:name filter-info) ".json")]
    (go (let [response (<! (http/get request-url {:with-credentials? false
                                                  :query-params {:t (:time filter-info) :sort "top"}}))
              full-posts (get-in response [:body :data :children])
              summarized-posts (vec (map (fn [post] (select-keys (:data post) [:author :title :id :subreddit])) full-posts))]
      (put! channel summarized-posts))) channel))

(defn get-post [id]
  (let [channel (chan)
        request-url (str base-url "/comments/" id "/.json")]
    (go (let [response (:body (<! (http/get request-url {:with-credentials? false})))
              parent-raw (:data (first (get-in (first response) [:data :children])))
              replies-raw (map :data (get-in (second response) [:data :children]))
              parent (select-keys parent-raw [:author :title :selftext])
              replies (map (fn [r] (select-keys r [:author :body])) replies-raw)]

      (put! channel {:parent parent :replies replies}))) channel))