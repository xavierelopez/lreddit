(ns reddit.components.routed-link
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [secretary.core :as secretary]
            [sablono.core :as html :refer-macros [html]]
            [om-tools.core :refer-macros [defcomponent]]
            [reddit.utils.push-state :refer [handle-routed-link]]))

(defcomponent routed-link [{:keys [href title]} owner]
  (render [_]
    (html [:a {:title title, :href (str "/" href), :on-click handle-routed-link} title])))
