(ns annotare.parsers.tcf
  [:require [clojure.xml :as cxml]
            [clojure.string :as string]
            [clojure.zip :as zip :refer [xml-zip]]
            [clojure.java.io :as jio]
            [clojure.data.xml :refer [parse] :as xml]
            [clojure.data.zip.xml :refer [xml-> xml1-> attr attr= text] :as zip-xml]])

;; See http://weblicht.sfs.uni-tuebingen.de/weblichtwiki/index.php/The_TCF_Format
;; for a description of the format

(defn- part-seq [sizes coll]
  (lazy-seq
   (when-let [s (seq coll)]
     (if-let [size (first sizes)]
       (let [run (take size s)]
         (cons run (part-seq (rest sizes) (drop size s))))
       (list coll)))))

(defn- get-tokens [root]
  (->> root
       :content second ;; <TextCorpus>
       :content first  ;; <tokens>
       :content
       (map (fn [elem]
              (let [z (xml-zip elem)]
                (xml1-> z text))))))

(defn- get-sentence-lens [root]
  (->> root
       :content second    ;; <TextCorpus>
       :content second    ;; <sentences>
       :content
       (map (fn [elem]
              (-> elem
                  (get-in [:attrs :tokenIDs])
                  (string/split #" ")
                  count)))))

(defn parse-sentences [file]
  (with-open [rdr (jio/reader file)]
    (doall
      (let [root (parse rdr)
            tokens (get-tokens root)
            sent-lens (get-sentence-lens root)]
        (part-seq sent-lens tokens)))))
