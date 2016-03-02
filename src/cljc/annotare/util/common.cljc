(ns annotare.util.common
  (:require [clojure.string :as string]))

(defn indexed [coll]
  "Create an [idx itm] seq over the collection, similar to Python's `enumerate`"
  (map-indexed vector coll))

(defn pluralize-kw [kw]
  "Pluralize a keyword, i.e. :keyword -> :keywords"
  (-> kw name (str "s") keyword))

(defn pair-seq
  "Generate a seq of pairs from a given seq"
  ([xs n] (pair-seq xs n true))
  ([xs n pad?]
   (if (>= (count xs) n)
    (let [pad-val (when (sequential? (first xs))
                    (vec (repeat (count (first xs)) nil)))
          xs-pad (if pad? (into (vec (repeat (dec n) pad-val)) xs) xs)]
      (lazy-seq (cons (vec (take n xs-pad)) (pair-seq (rest xs-pad) n false)))))))

(defn has-entities? [tags empty-tag]
  (not (every? (partial = empty-tag) tags)))

;; FIXME: This is really not elegant
(defn extract-entities [tokens tags empty-tag]
  "Get all entities in the given tagged sequence of tokens.
   Returns a seq of {:tag \"TAG\" :tokens [...]} maps"
  (let [itms (map vector tokens tags)]
   (remove nil?
    (reduce
      (fn [[last-run & runs] [tok tag]]
        (cond
          ;; New run
          (and (not= tag empty-tag) (or (nil? last-run)
                                        (not= (:tag last-run) tag)))
          (-> runs
              (conj last-run)
              (conj {:tokens [tok]
                     :tag (string/replace tag #"B-" "")}))

          ;; Continue previous run
          (= tag (:tag last-run))
          (-> last-run
              (update :tokens conj tok)
              (#(conj runs %)))

          ;; Just the empty tag
          :else
          (-> runs
              (conj last-run)
              (conj nil))))
     '()  (map vector tokens tags)))))
