(ns reddit.utils.push-state
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [secretary.core :as secretary]
            [cljs.core.async :refer [put! chan <! alts!]]
            [goog.events :as events]
            [goog.history.EventType :as EventType])
  (:import goog.history.Html5History))


(def history (Html5History.))

(defn listen [el type]
  (let [out (chan)]
    (events/listen el type
      (fn [e] (put! out e) false))
    out))

(defn init-push-state []
  (.setUseFragment history false)
  (.setPathPrefix history "")
  (.setEnabled history true)

  (let [navigation (listen history EventType/NAVIGATE)]
    (go
      (loop []
         (let [token (.-token (<! navigation))]
           (secretary/dispatch! token)) (recur)))))