(ns annotare.util
  (:require [clojure.string :as string]))

(defn indexed [coll]
  "Create an [idx itm] seq over the collection, similar to Python's `enumerate`"
  (map-indexed #(vector %1 %2) coll))

(defn ev->val [ev]
  "Obtain the target's value from the given event. Only useful for cljs"
  (-> ev .-target .-value))

(defn pluralize-kw [kw]
  "Pluralize a keyword, i.e. :keyword -> :keywords"
  (-> kw name (str "s") keyword))

(defn make-load-key [xs]
  "Generate a key for the :loading? map in the app state."
  (string/join "." xs))
