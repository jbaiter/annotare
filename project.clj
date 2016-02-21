(defproject annotare "0.1.0-SNAPSHOT"

  :description "Annotate linguistic data."
  :url "http://github.com/jbaiter/annotare"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [selmer "1.0.0"]  ;; TODO: Remove
                 [markdown-clj "0.9.85"]  ;; TODO: Remove
                 [luminus/config "0.5"]
                 [ring-middleware-format "0.7.0"]
                 [metosin/ring-http-response "0.6.5"]
                 [bouncer "1.0.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [compojure "1.4.0"]
                 [ring-webjars "0.1.1"]
                 [ring/ring-defaults "0.1.5"]
                 [ring "1.4.0" :exclusions [ring/ring-jetty-adapter]]
                 [mount "0.1.8"]
                 [luminus-nrepl "0.1.2"]
                 [migratus "0.8.9"]
                 [conman "0.2.9"]
                 [org.xerial/sqlite-jdbc "3.8.11.1"]
                 [org.clojure/clojurescript "1.7.228" :scope "provided"]
                 [reagent "0.6.0-alpha"]
                 [reagent-utils "0.1.7"]
                 [secretary "1.2.3"]
                 [org.clojure/core.async "0.2.374"]
                 [cljs-ajax "0.5.3"]
                 [luminus-http-kit "0.1.1"]
                 [luminus-log4j "0.1.2"]
                 [re-frame "0.7.0-alpha"]
                 [prismatic/schema "1.0.3"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/data.zip "0.1.1"]
                 [metosin/compojure-api "1.0.0"]]

  :min-lein-version "2.0.0"
  :uberjar-name "annotare.jar"
  :jvm-opts ["-server"]
  :resource-paths ["resources" "target/cljsbuild"]
  :source-paths ["src/clj", "src/cljc"]
  :test-paths ["test/clj"]

  :main annotare.core
  :migratus {:store :database}

  :plugins [[lein-environ "1.0.1"]
            [migratus-lein "0.2.1"]
            [lein-cljsbuild "1.1.1"]
            [lein-sassc "0.10.4"]
            [lein-auto "0.1.2"]
            [lein-npm "0.6.1"]]

  :npm {:dependencies [[bulma "0.0.14"]]
        :root "resources/public/js"}

  :sassc
  [{:src "resources/scss/screen.scss"
    :output-to "resources/public/css/screen.css"
    :style "nested"
    :import-path "resources"}]

  :auto
  {"sassc" {:file-pattern #"\.(scss|sass)$" :paths ["resources/scss"]}} 

  :hooks [leiningen.sassc]
  :clean-targets ^{:protect false} [:target-path [:cljsbuild :builds :app :compiler :output-dir] [:cljsbuild :builds :app :compiler :output-to]]
  :cljsbuild
  {:builds
   {:app
    {:source-paths ["src/cljs", "src/cljc"]
     :compiler
     {:output-to "target/cljsbuild/public/js/app.js"
      :output-dir "target/cljsbuild/public/js/out"
      :externs ["react/externs/react.js"]
      :pretty-print true}}}}

  :profiles
  {:uberjar {:omit-source true
             :env {:production true}
              :prep-tasks ["compile" ["cljsbuild" "once"]]
              :cljsbuild
              {:builds
               {:app
                {:source-paths ["env/prod/cljs"]
                 :compiler
                 {:optimizations :advanced
                  :pretty-print false
                  :closure-defines {"goog.DEBUG" false}
                  :closure-warnings
                  {:externs-validation :off :non-standard-jsdoc :off}}}}} 

             :aot :all
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}
   :dev           [:project/dev :profiles/dev]
   :test          [:project/test :profiles/test]
   :project/dev  {:dependencies [[prone "1.0.1"]
                                 [ring/ring-mock "0.3.0"]
                                 [ring/ring-devel "1.4.0"]
                                 [pjstadig/humane-test-output "0.7.1"]
                                 [com.cemerick/piggieback "0.2.2-SNAPSHOT"]
                                 [lein-doo "0.1.6"]
                                 [lein-figwheel "0.5.0-6"]
                                 [mvxcvi/puget "1.0.0"]]
                  :plugins [[lein-figwheel "0.5.0-6"] [lein-doo "0.1.6"] [org.clojure/clojurescript "1.7.228"]]
                   :cljsbuild
                   {:builds
                    {:app
                     {:source-paths ["env/dev/cljs"]
                      :compiler
                      {:main "annotare.app"
                       :asset-path "/js/out"
                       :optimizations :none
                       :source-map true}}
                     :test
                     {:source-paths ["src/cljs" "test/cljs"]
                      :compiler
                      {:output-to "target/test.js"
                       :main "annotare.doo-runner"
                       :optimizations :whitespace
                       :pretty-print true}}}} 

                  :figwheel
                  {:http-server-root "public"
                   :server-port 3449
                   :nrepl-port 7002
                   :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"]
                   :css-dirs ["resources/public/css"]
                   :ring-handler annotare.handler/app}
                  :doo {:build "test"}
                  :source-paths ["env/dev/clj"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]
                  ;;when :nrepl-port is set the application starts the nREPL server on load
                  :env {:dev        true
                        :port       3000
                        :max-body-size (* 1024 1024 32)
                        :nrepl-port 7000}}
   :project/test {:env {:test       true
                        :port       3001
                        :max-body-size (* 1024 1024 32)
                        :nrepl-port 7001}}
   :profiles/dev {}
   :profiles/test {}})
