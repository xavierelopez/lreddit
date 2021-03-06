(ns lreddit.reddit-api
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [put! chan <!]]
            [cljs-http.client :as http]))


(def base-url "http://www.reddit.com")

(defn get-subreddit-posts [sub filter-info]
  (let [channel (chan)
        filter-name (:name filter-info)
        filter-time (:time filter-info)
        request-url (str base-url "/r/" sub "/" (:name filter-info) ".json")]
    (go (let [response (<! (http/get request-url {:with-credentials? false
                                                  :query-params (if (= "top" filter-name) {:t filter-time :sort "top"})}))
              full-posts (get-in response [:body :data :children])
              summarized-posts (vec (map (fn [post] (select-keys (:data post) [:author :title :id :subreddit])) full-posts))]
      (put! channel summarized-posts))) channel))

(defn get-post [id]
  (let [channel (chan)
        request-url (str base-url "/comments/" id "/.json")]
    (go (let [response (:body (<! (http/get request-url {:with-credentials? false})))
              parent (:data (first (get-in (first response) [:data :children])))
              replies (get-in (second response) [:data :children])]

      (put! channel {:parent parent :replies replies}))) channel))

(defn get-replies [sub parent-id id]
  (let [channel (chan)
        request-url (str base-url "/r/" sub "/comments/" parent-id "/lol/" id "/.json")]
    (go (let [response (:body (<! (http/get request-url {:with-credentials? false})))
              replies (first (map :data (get-in (second response) [:data :children])))]
      (put! channel {:replies replies}))) channel))