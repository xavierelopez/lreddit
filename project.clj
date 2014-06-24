(defproject reddit "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2202"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [om "0.5.0"]
                 [cljs-http "0.1.11"]
                 [prismatic/om-tools "0.2.1"]
                 [sablono "0.2.17"]
                 [secretary "1.1.1"]]

  :plugins [[lein-cljsbuild "1.0.3"]]

  :source-paths ["src/cljs"]

  :cljsbuild {
    :builds [{:id "reddit"
              :source-paths ["src/cljs"]
              :compiler {
                :output-to "dist/js/app.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}]})
