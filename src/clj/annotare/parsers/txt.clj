(ns annotare.parsers.txt
  [:require [clojure.string :as string]])

(defn parse-sentences [reader]
  (->> reader
       slurp
       string/split-lines
       (map #(string/split % #"\s+"))))
