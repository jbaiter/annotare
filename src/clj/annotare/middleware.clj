(ns annotare.middleware
 (:require [clojure.tools.logging :as log]
           [config.core :refer [env]]
           [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
           [ring.middleware.format :refer [wrap-restful-format]]
           [annotare.config :refer [defaults]])
 (:import [javax.servlet ServletContext]))

(defn wrap-formats [handler]
  (let [wrapped (wrap-restful-format
                 handler
                 {:formats [:json-kw :transit-json :transit-msgpack]})]
   (fn [request]
      ;; disable wrap-formats for websockets
      ;; since they're not compatible with this middleware
     ((if (:websocket? request) handler wrapped) request))))

(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
      wrap-formats
      (wrap-defaults
       (-> site-defaults
           (assoc-in [:security :anti-forgery] false)
           (dissoc :session)))))
