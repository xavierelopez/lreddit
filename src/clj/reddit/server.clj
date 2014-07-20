(ns reddit.server
  (:use ring.adapter.jetty
        [ring.util.response :exclude (not-found)]
        compojure.core
        compojure.route
  )
  (:require [compojure.handler :as handler]))

(defroutes app-routes
  (resources "/")
  (GET "*" [] (file-response "index.html" {:root "resources/public"})))

(def app
  (handler/site app-routes))

#_(defonce server
  (run-jetty #'app {:port 3000 :join? false}))