(ns reddit.router
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [put! chan <! alts!]]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [om.core :as om]
            [secretary.core :as secretary :include-macros true :refer [defroute]])
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

    (. history (setToken href title))))

(defn setup-push-state []
  (let [navigation (listen history EventType/NAVIGATE)]
    (go
      (loop []
         (let [token (.-token (<! navigation))]
           (secretary/dispatch! token)) (recur))))

  (.setUseFragment history false)
  (.setPathPrefix history "")
  (.setEnabled history true))

(defn navigate [route]
  (. history (setToken route)))

(defn define-routes [app]
  (defroute route-main "/" []
    (om/transact! app #(assoc % :view :main, :posts [], :post-id nil, :subreddit nil)))

  (defroute route-sub "/r/:sub" [sub]
    (om/transact! app #(assoc % :view :sub, :subreddit sub, :post nil, :post-id nil,
                         {:selected-filter {:name "hot" :time "hour"}})))

  (defroute route-comments "/r/:sub/comments/:id" [sub id]
    (om/transact! app #(assoc % :view :thread, :subreddit sub, :post-id id)))

  (defroute route-sub-filtered "/r/:sub/:sub-filter/:sub-filter-time"
    [sub sub-filter sub-filter-time]
    (om/transact! app #(assoc % :view :sub, :subreddit sub, :post nil, :post-id nil,
                     :selected-filter {:name sub-filter, :time sub-filter-time}))))

(defn start [app]
    (define-routes app)
    (setup-push-state))


