(ns lreddit.components.subreddit
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [put! chan <! alts!]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]
            [secretary.core :as secretary]
            [lreddit.router :as router]
            [lreddit.reddit-api :as reddit]
            [lreddit.components.routed-link :refer [routed-link]]))

(defn refresh-posts [app]
  (go (let [sub (:subreddit @app)
            selected-filter (:selected-filter @app)
            posts (<! (reddit/get-subreddit-posts sub selected-filter))]
    (om/update! app :posts posts))))

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
        (let [event (<! events)
              sub (:subreddit @app)
              selected-filter (:selected-filter @app)]
          (condp = (:name event)
            :filter-selected (router/navigate (router/route-sub-filtered {:sub sub
                                                         :sub-filter (:value event)}))
            :time-filter-selected (router/navigate (router/route-sub-filtered-time {:sub sub
                                                         :sub-filter (:name selected-filter)
                                                         :sub-filter-time (:value event)}))))
         (recur)))))
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
        (html [:li link [:em {:class "muted"} (str " by " author)]]))))

(defcomponent post-list [posts owner]
  (render [_]
    (html [:ul {:class "posts"} (om/build-all post-item-view posts)])))

(defcomponent subreddit-view [app owner]
  (will-mount [_]
    (refresh-posts app))
  (will-update [_ next-props _]
    (if (not= (:selected-filter next-props) (:selected-filter om/get-props owner))
      (refresh-posts app)))
  (render [_]
    (html [:div {:id "subreddit"}
      (om/build header-view app)
      (if (empty? (:posts app)) [:div "Loading..."]
        (om/build post-list (:posts app)))])))
