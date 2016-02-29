(ns annotare.middleware
  (:require-macros [annotare.macros :refer [log group]])
  (:require [cljs.pprint :refer [pprint]]
            [clojure.data :as data]))

(defn- groupEnd []
  (when (.-groupEnd js/console)
    (.groupEnd js/console)))

(defn debug
  "Pretty much identical to the re-frame debug middleware, except for:
    - prints values directly to the console (use binaryage/dirac in Chrome)
    - prints old and new application state in addition to diffs"
  [handler]
  (fn debug-handler
    [db v]
    (group "re-frame event: " v)
    (let [new-db  (handler db v)
          diff    (data/diff db new-db)]
      (.log js/console "old state: " db)
      (.log js/console "only before: " (first diff))
      (.log js/console "new state: " new-db)
      (.log js/console "only after: " (second diff))
      (groupEnd)
      new-db)))
