(ns annotare.config
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[annotare started successfully]=-"))
   :middleware identity})
