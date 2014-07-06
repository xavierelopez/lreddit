(ns reddit.components.subreddit
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [om-tools.core :refer-macros [defcomponent]]
            [cljs.core.async :refer [put! chan <! alts!]]
            [reddit.router :as router]
            [reddit.reddit-api :as reddit]
            [reddit.components.routed-link :refer [routed-link]]))

(defcomponent select-box-view [{:keys [values selected event-name]} owner]
  (render-state [_ {:keys [events]}]
    (html [:select {:class "form-control pull-left"
                    :value selected
                    :on-change #(put! events {:name event-name, :value (.. % -target -value)})}
      (map (fn [value] [:option value]) values)])))

(defcomponent header-view [app owner]
  (init-state [_]
    {:events (chan)})
  (will-mount [_]
    (let [events (om/get-state owner :events)]
      (go (loop []
        (let [api-params [(:subreddit @app) (:selected-filter @app)]
              event (<! events)]
          (condp = (:name event)
            :filter-selected (om/transact! app :selected-filter (fn [m] (assoc m :name (:value event))))
            :time-filter-selected (om/transact! app :selected-filter (fn [m] (assoc m :time (:value event)))))
          (om/update! app :posts [])
          (om/update! app :posts (<! (apply reddit/get-subreddit-posts api-params)))) (recur)))))
  (render-state [_ {:keys [events]}]
    (html [:div {:class "well clearfix" :id "header"}
      [:h2 (str "r/" (:subreddit app))]
      (om/build select-box-view {:values (:filters app)
                            :selected (get-in app [:selected-filter :name])
                            :event-name :filter-selected}
                            {:init-state {:events events}})
      (if (= "top" (get-in app [:selected-filter :name]))
        (om/build select-box-view {:values (:filter-times app)
                              :selected (get-in app [:selected-filter :time])
                              :event-name :time-filter-selected}
                              {:init-state {:events events}}))])))


(defcomponent post-item-view [{:keys [id title author subreddit]} owner]
  (render [_]
    (let [href (router/route-comments {:sub (clojure.string/lower-case subreddit) :id id})
          link (om/build routed-link {:title title, :href href})]
        (html [:li [:strong link] [:em (str " by " author)]]))))

(defcomponent post-list [posts owner]
  (render [_]
    (html [:ul {:class "posts"} (om/build-all post-item-view posts)])))

(defcomponent subreddit-view [app owner]
  (will-mount [_]
    (go (let [api-params [(:subreddit @app) (:selected-filter @app)]
              posts (<! (apply reddit/get-subreddit-posts api-params))]
      (om/update! app :posts posts))))
  (render [_]
    (html [:div {:id "subreddit"}
      (om/build header-view app)
      (if (empty? (:posts app)) [:div "Loading..."]
        (om/build post-list (:posts app)))])))
