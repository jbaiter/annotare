(ns annotare.app
  (:require [annotare.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/main)
