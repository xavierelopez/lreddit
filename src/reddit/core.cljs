(ns reddit.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [om-tools.core :refer-macros [defcomponent]]
            [cljs.core.async :refer [put! chan <! alts!]]
            [reddit.reddit-api :as reddit]))

(enable-console-print!)

(def app (atom {:comment ""
                :posts []
                :subreddit "askreddit"
                :subreddits ["askreddit" "asksciencefiction" "truereddit" "iama"]
                :filters ["hot" "new" "top"]
                :selected-filter "hot"}))

(defn fetch-posts []
  (reddit/get-subreddit-posts (:subreddit @app) (:selected-filter @app)))

(defcomponent subreddit-list [subs]
  (render-state [_ {:keys [select-channel]}]
    (let [build-li (fn [sub] [:li {:on-click #(put! select-channel (.. % -target -innerText))} sub])]
      (html [:ul {:class "subreddits"} (map build-li subs)]))))

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
    {:sub-select-ch (chan)
     :filter-select-ch (chan)})
  (will-mount [_]
    (let [sub-select-ch (om/get-state owner :sub-select-ch)
          filter-select-ch (om/get-state owner :filter-select-ch)]
      (go (loop []
        (let [[value channel] (alts! [sub-select-ch filter-select-ch])]
          (condp = channel
            sub-select-ch (om/update! app :subreddit value)
            filter-select-ch (om/update! app :selected-filter value))
          (om/update! app :posts [])
          (om/update! app :posts (<! (fetch-posts)))) (recur)))))
  (render-state [_ {:keys [sub-select-ch filter-select-ch]}]
    (html [:div {:id "header"}
      [:h1 "reddit cmn"]
      [:h2 (str "r/" (:subreddit app))]
      [:h2 (str "Filter: " (:selected-filter app))]
      (om/build subreddit-list (:subreddits app)
                               {:init-state {:select-channel sub-select-ch}})
      (om/build select-box {:values (:filters app)
                            :selected (:selected-filter app)}
                            {:init-state {:select-channel filter-select-ch}})])))

(defcomponent main [app owner]
  (will-mount [_]
    (go (om/update! app :posts (<! (fetch-posts)))))
  (render [_]
    (html [:div {:id "main"}
      (om/build header app)
      [:h2 "Posts"]
      (if (empty? (:posts app)) [:div "Loading..."]
        (om/build post-list (:posts app)))])))

(om/root
  main
  app
  {:target (. js/document (getElementById "app"))})
