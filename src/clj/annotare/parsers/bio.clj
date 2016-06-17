(ns annotare.parsers.bio
  [:require [clojure.string :as string]])


(defn- parse-line [seqs line]
  (case line
    "" (into [{:tokens [] :tags []}] seqs)
    (let [[token tag] (string/split line  #"\t")]
      (-> seqs
          (update-in [0 :tokens] #(conj % token))
          (update-in [0 :tags] #(conj % tag))))))


(defn parse-sentences [empty-tag fpath]
  (with-open [reader (clojure.java.io/reader fpath)]
    (->> (line-seq reader)
         (reduce parse-line [{:tokens [] :tags []}])
         (reverse))))
