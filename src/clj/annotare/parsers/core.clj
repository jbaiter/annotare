(ns annotare.parsers.core
  (:require [annotare.parsers.tcf :as tcf]
            [annotare.parsers.txt :as txt]
            [annotare.parsers.bio :as bio]))

(def parsers
  {:tcf       tcf/parse-sentences
   :txt       txt/parse-sentences
   :bio       bio/parse-sentences})
