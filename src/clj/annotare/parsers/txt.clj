(ns annotare.parsers.txt
  [:require [clojure.string :as string]])

(defn parse-line [seqs line]
  (conj seqs (map)))

(defn parse-sentences [empty-tag fpath]
  (with-open [reader (clojure.java.io/reader fpath)]
    (->> (line-seq reader)
         (map (fn [l]
                 (let [tokens (string/split #"\s+" l)]
                    {:tokens tokens}
                    :tags (vec (repeat (count tokens) empty-tag))))))))
