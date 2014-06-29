(ns reddit.router
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [secretary.core :as secretary :include-macros true :refer [defroute]]
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

    (. history (setToken href title))))

(defn setup-push-state []
  (.setUseFragment history false)
  (.setPathPrefix history "")
  (.setEnabled history true)

  ; set first route
  (secretary/dispatch! (.. js/window -location -pathname))

  (let [navigation (listen history EventType/NAVIGATE)]
    (go
      (loop []
         (let [token (.-token (<! navigation))]
           (secretary/dispatch! token)) (recur)))))

(defn define-routes [app]
  (defroute route-main "/" []
    (swap! app assoc :view :main :posts [] :post-id nil :subreddit nil))

  (defroute route-sub-filtered "/r/:sub/:sub-filter" [sub sub-filter]
    (swap! app assoc :view :sub :subreddit sub :post "" :post-id nil)
    (swap! app assoc-in [:selected-filter :name] sub-filter))

  (defroute route-sub "/r/:sub" [sub]
    (swap! app assoc :view :sub :subreddit sub :post "" :post-id nil)
    (swap! app assoc-in [:selected-filter :name] "hot"))

  (defroute route-comments "/r/:sub/comments/:id" [sub id]
    (swap! app assoc :view :thread :subreddit sub :post-id id)))

(defn navigate [named-route params]
  (secretary/dispatch! (named-route params)))

(defn start [app]
    (define-routes app)
    (setup-push-state))


