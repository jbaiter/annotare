(ns annotare.handler
  (:require [compojure.core :refer [defroutes routes wrap-routes GET]]
            [annotare.routes.services :refer [service-routes]]
            [annotare.middleware :as middleware]
            [clojure.tools.logging :as log]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [config.core :refer [env]]
            [annotare.config :refer [defaults]]
            [mount.core :as mount]
            [luminus.logger :as logger]
            [hiccup.page :refer [include-js include-css html5]]))

(defn init []
  (logger/init env)
  (doseq [component (:started (mount/start))]
    (log/info component "started"))
  ((:init defaults)))

(defn destroy []
  (log/info "annotare is shutting down...")
  (doseq [component (:stopped (mount/stop))]
    (log/info component "stopped"))
  (log/info "shutdown complete!"))


(def mount-target
  [:div#app
   [:section.hero.is-fullheight
    [:div.hero-header]
    [:div.hero-content>div.container
      [:div.loading-spinner]
      [:h1.title "Just a second"]
      [:h2.subtitle "Loading the application..."]]
    [:div.hero-footer]]])

(def loading-page
  (html5
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content  "width=device-width, initial-scale=1"}]
     (include-css "https://maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css")
     (include-css "/css/screen.css")
     [:body
      mount-target
      (include-js "/js/app.js")]]))

(defroutes app-routes
  (GET "*" [] loading-page))

(def app-routes
  (routes
    service-routes
    app-routes))

(def app (middleware/wrap-base #'app-routes))
