(ns annotare.config
  (:require [clojure.tools.logging :as log]
            [annotare.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[annotare started successfully using the development profile]=-"))
   :middleware wrap-dev})
