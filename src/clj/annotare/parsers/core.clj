(ns annotare.parsers.core
  (:require [annotare.parsers.tcf :as tcf]
            [annotare.parsers.txt :as txt]))

(def parsers
  {:tcf       tcf/parse-sentences
   :txt       txt/parse-sentences})
