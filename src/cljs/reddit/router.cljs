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

(defn setup-push-state []
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


(defn define-routes [app]
  (defroute main "/" []
    (swap! app assoc :view :main :posts [] :post-id nil :subreddit nil))

  (defroute sub-filtered "/r/:sub/:sub-filter" [sub sub-filter]
    (swap! app assoc :view :sub :subreddit sub :post "" :post-id nil)
    (swap! app assoc-in [:selected-filter :name] sub-filter))

  (defroute sub "/r/:sub" [sub]
    (swap! app assoc :view :sub :subreddit sub :post "" :post-id nil)
    (swap! app assoc-in [:selected-filter :name] "hot"))

  (defroute comments "/r/:sub/comments/:id" [sub id]
    (swap! app assoc :view :post :subreddit sub :post-id id))

  [main sub-filtered sub comments])


(defn start [app]
  (let [named-routes (define-routes app)]
    (setup-push-state)
    (swap! app assoc :named-routes named-routes)))

(defn handle-routed-link [e]
  (.stopPropagation e)
  (.preventDefault e)
  (let [el (.-target e)
        title (.getAttribute el "title")
        href (.getAttribute el "href")]

    (. history (setToken href title))))

