(ns annotare.util
 (:require [clojure.string :as s]))

(defn- bio-line [[tok tag] prev-tag]
  (let [clean-prev (when prev-tag (s/replace prev-tag #"B-" ""))
        bio-tag (cond
                  (or (= tag "O")
                      (s/starts-with? tag "B-")) tag
                  (= clean-prev tag) (str "I-" tag)
                  (or (nil? prev-tag)
                      (= prev-tag "O")
                      (not= prev-tag tag)) (str "B-" tag))]
   (apply str (interpose "\t" [tok bio-tag]))))

(defn- bio-sent [{:keys [tokens tags]}]
  (let [pairs (map vector tokens tags)
        prev-tags (map-indexed (fn [idx t] (get tags (dec idx))) tags)]
   (apply str (conj (into [] (interpose "\n" (map bio-line pairs prev-tags)))
                    "\n"))))

(defn make-bio [tagged-sents]
  (apply str (interpose "\n" (map bio-sent tagged-sents))))


(bio-line ["Andrassy" "PER"] "NORP")
