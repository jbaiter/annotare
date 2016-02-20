(ns annotare.util)

(defn indexed [coll]
  "Create an [idx itm] seq over the collection, similar to Python's `enumerate`"
  (map-indexed #(vector %1 %2) coll))
