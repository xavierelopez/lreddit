(ns reddit.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [om-tools.core :refer-macros [defcomponent]]
            [cljs.core.async :refer [put! chan <!]]
            [reddit.reddit-api :as reddit]))

(enable-console-print!)



(def app (atom {:comment ""
                :posts []
                :subreddit "askreddit"
                :filters ["hot" "new" "top"]
                :selected-filter "hot"}))

(defn fetch-posts []
  (reddit/get-subreddit-posts (:subreddit @app) (:selected-filter @app)))

(defcomponent post-list [posts]
  (render [_]
    (html [:ul {:class "posts"}
      (map (fn [p] [:li [:strong (:title p)] [:em (str " by " (:author p))]]) posts)])))

(defcomponent select-box [{:keys [values selected]} owner]
  (render-state [_ {:keys [select-channel]}]
    (html [:select {:value selected
                    :on-change #(put! select-channel (.. % -target -value))}
      (map (fn [value] [:option value]) values)])))

(defcomponent header [app owner]

  (init-state [_]
    {:filter-select-ch (chan)})

  (will-mount [_]
    (go (om/update! app :posts (<! (fetch-posts))))
    (let [filter-select-ch (om/get-state owner :filter-select-ch)]
      (go (loop []
        (let [filter (<! filter-select-ch)]
          (om/update! app :selected-filter filter)
          (om/update! app :posts (<! (fetch-posts)))) (recur)))))

  (render-state [_, {:keys [filter-select-ch]}]
    (html [:div {:id "header"}
      [:h1 "reddit cmn"]
      [:h2 (str "Subreddit: " (:subreddit app))]
      [:h2 (str "Filter: " (:selected-filter app))]
      (om/build select-box {:values (:filters app)
                            :selected (:selected-filter app)}
                            {:init-state {:select-channel filter-select-ch}})
      [:h2 "Posts"]
      (om/build post-list (:posts app))])))

(defcomponent main [app owner]
  (render [_]
    (om/build header app)))

(om/root
  main
  app
  {:target (. js/document (getElementById "app"))})
