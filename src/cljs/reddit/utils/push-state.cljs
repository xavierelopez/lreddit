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

(defn handle-routed-link [e]
  (.stopPropagation e)
  (.preventDefault e)
  (let [el (.-target e)
        title (.getAttribute el "title")
        href (.getAttribute el "href")]

    (prn "setting token " href)
    (. history (setToken href title))))

(defn init-push-state []
  (.setUseFragment history false)
  (.setPathPrefix history "")
  (.setEnabled history true)

  ; set first route
  ; for some reason secretary doesn't do this automatically.
  ; maybe it's because we start handling window history
  ; events too late
  (secretary/dispatch! (.. js/window -location -pathname))

  (let [navigation (listen history EventType/NAVIGATE)]
    (go
      (loop []
         (let [token (.-token (<! navigation))]
           (secretary/dispatch! token)) (recur)))))