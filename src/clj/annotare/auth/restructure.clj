(ns annotare.auth.restructure
  (:require [compojure.api.meta :refer [restructure-param]]
            [annotare.auth.basic :refer [wrap-auth]]))

(defmethod restructure-param :authenticated
  [_ _ acc]
  (update-in acc [:middleware] conj [wrap-auth]))
