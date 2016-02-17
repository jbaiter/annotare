(ns annotare.handler
  (:require [compojure.core :refer [defroutes routes wrap-routes]]
            [annotare.layout :refer [error-page]]
            [annotare.routes.home :refer [home-routes]]
            [annotare.routes.project :refer [project-routes]]
            [annotare.routes.document :refer [document-routes]]
            [annotare.routes.sentence :refer [sentence-routes]]
            [annotare.middleware :as middleware]
            [clojure.tools.logging :as log]
            [compojure.route :as route]
            [config.core :refer [env]]
            [annotare.config :refer [defaults]]
            [mount.core :as mount]
            [luminus.logger :as logger]))

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

(def app-routes
  (routes
    home-routes
    project-routes
    document-routes
    sentence-routes
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))

(def app (middleware/wrap-base #'app-routes))
