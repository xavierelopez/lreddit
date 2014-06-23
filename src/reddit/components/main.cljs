(ns reddit.components.main
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [om-tools.core :refer-macros [defcomponent]]
            [cljs.core.async :refer [put! chan <! alts!]]
            [reddit.reddit-api :as reddit]))

(defcomponent subreddit-list [subs]
  (render-state [_ {:keys [events]}]
    (let [build-li (fn [sub] [:li {
        :on-click #(put! events {:name :subreddit-selected, :value (.. % -target -innerText)})} sub])]
      (html [:ul {:class "subreddits"} (map build-li subs)]))))

(defcomponent select-box [{:keys [values selected event-name]} owner]
  (render-state [_ {:keys [events]}]
    (html [:select {:value selected
                    :on-change #(put! events {:name event-name, :value (.. % -target -value)})}
      (map (fn [value] [:option value]) values)])))

(defcomponent header [app owner]
  (init-state [_]
    {:events (chan)})
  (will-mount [_]
    (let [events (om/get-state owner :events)]
      (go (loop []
        (let [api-params [(:subreddit @app) (:selected-filter @app)]
              event (<! events)]
          (condp = (:name event)
            :subreddit-selected (om/update! app :subreddit (:value event))
            :filter-selected (om/transact! app :selected-filter (fn [m] (assoc m :name (:value event))))
            :time-filter-selected (om/transact! app :selected-filter (fn [m] (assoc m :time (:value event)))))
          (om/update! app :posts [])
          (om/update! app :posts (<! (apply reddit/get-subreddit-posts api-params)))) (recur)))))
  (render-state [_ {:keys [events]}]
    (html [:div {:id "header"}
      [:h1 "reddit cmn"]
      [:h2 (str "r/" (:subreddit app))]
      [:h2 (str "Filter: " (get-in app [:selected-filter :name]))]
      (om/build subreddit-list (:subreddits app)
                               {:init-state {:events events}})
      (om/build select-box {:values (:filters app)
                            :selected (get-in app [:selected-filter :name])
                            :event-name :filter-selected}
                            {:init-state {:events events}})
      (if (= "top" (get-in app [:selected-filter :name]))
        (om/build select-box {:values (:filter-times app)
                              :selected (get-in app [:selected-filter :time])
                              :event-name :time-filter-selected}
                              {:init-state {:events events}}))])))

(defcomponent post-list [posts]
  (render [_]
    (html [:ul {:class "posts"}
      (map (fn [p] [:li [:strong (:title p)] [:em (str " by " (:author p))]]) posts)])))

(defcomponent main [app owner]
  (will-mount [_]
    (go (let [api-params [(:subreddit @app) (:selected-filter @app)]
              posts (<! (apply reddit/get-subreddit-posts api-params))]
      (om/update! app :posts posts))))
  (render [_]
    (html [:div {:id "main"}
      (om/build header app)
      [:h2 "Posts"]
      (if (empty? (:posts app)) [:div "Loading..."]
        (om/build post-list (:posts app)))])))