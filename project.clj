(defproject reddit "0.1.0"
  :description "Reddit for lurkers"
  :url "www.lurkreddit.com"

  :dependencies [; Server
                 [org.clojure/clojure "1.5.1"]
                 [ring "1.2.0"]
                 [compojure "1.1.8"]
                 ; Client
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [org.clojure/clojurescript "0.0-2202"]
                 [om "0.6.4"]
                 [cljs-http "0.1.11"]
                 [prismatic/om-tools "0.2.1"]
                 [sablono "0.2.17"]
                 [secretary "1.1.1"]]

  :plugins [[lein-cljsbuild "1.0.3"] [lein-ring "0.8.11"]]

  :ring {:handler reddit.server/app}

  :source-paths ["src/clj"]

  :cljsbuild {
    :builds [{:id "reddit"
              :source-paths ["src/cljs"]
              :compiler {
                :output-to "resources/public/app.js"
                :output-dir "resources/public/out"
                :optimizations :none}}]})
