(ns reddit.components.routed-link
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [secretary.core :as secretary]
            [sablono.core :as html :refer-macros [html]]
            [om-tools.core :refer-macros [defcomponent]]))


(defn handle-click [e]
  (.stopPropagation e)
  (.preventDefault e)
  (let [el (.-target e)
        title (.getAttribute el "title")
        href (.getAttribute el "href")]

    (.pushState js/window.history nil title (str "/" href))
    (secretary/dispatch! href)))

(defcomponent routed-link [{:keys [href title]} owner]
  (render [_]
    (html [:a {:title title, :href href, :on-click handle-click} title])))
