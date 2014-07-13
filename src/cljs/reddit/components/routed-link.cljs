(ns reddit.components.routed-link
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as secretary]
            [sablono.core :as html :refer-macros [html]]
            [reddit.router :as router]))

(defcomponent routed-link [{:keys [href title]} owner]
  (render [_]
    (html [:a {:title title
               :href href
               :on-click router/handle-routed-link} title])))
