(ns reddit.reddit-api
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [put! chan <!]]))


(def base-url "http://www.reddit.com")

(def post-keywords [:author :title])



(defn get-subreddit-posts [sub filter]
  (let [channel (chan)
        request-url (str base-url "/r/" sub "/" filter ".json")]
    (go (let [response (<! (http/get request-url))
              full-posts (get-in response [:body :data :children])
              summarized-posts (vec (map (fn [post] (select-keys (:data post) post-keywords)) full-posts))]
      (put! channel summarized-posts))) channel))
