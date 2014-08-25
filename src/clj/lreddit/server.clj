(ns lreddit.server
  (:use compojure.core
        compojure.route
        ring.adapter.jetty
        [ring.util.response :exclude (not-found)])
  (:require [compojure.handler :as handler]))

(defroutes app-routes
  (resources "/")
  (GET "*" [] (resource-response "index.html" {:root "public"})))

(def app
  (handler/site app-routes))

(defn -main [port]
  (run-jetty app {:port (Integer. port) :join? false}))