(ns annotare.util.gnd
  (:require-macros [annotare.macros :refer [log]])
  (:require [clojure.string :as string]))

(defn extract-year [datestr]
  (re-find #"\d{4}" datestr))

(defn make-lifespan [{:keys [dateOfBirth dateOfDeath]}]
  (let [val-kw (keyword "@value")]
    (cond-> nil
      dateOfBirth             (str " (" (extract-year
                                          (if (map? dateOfBirth)
                                            (val-kw dateOfBirth)
                                            dateOfBirth)) "-")
      (and (not dateOfBirth)
           dateOfDeath)       (str " (?-")
      dateOfDeath             (str (extract-year (if (map? dateOfDeath)
                                                   (val-kw dateOfDeath)
                                                   dateOfDeath)) ")")
      (and dateOfBirth
           (not dateOfDeath)) (str "?)"))))

(defn make-info [{info :biographicalOrHistoricalInformation}]
  (let [val-kw (keyword "@value")]
    (cond
      (vector? info) (val-kw (first info))
      (map? info)    (val-kw info))))

(defn tag->gnd-type
  [tag]
  (case tag
    "PER"   "Person"
    "ORG"   "CorporateBody"
    "LOC"   "PlaceOrGeographicName"))

(defn make-gnd-querystr [name]
  (-> name
    (string/replace #"ſ" "s")))

(defn parse-lobid [resp]
  (let [graph-kw (keyword "@graph")
        graphs (map #(-> % graph-kw first) (filter #(contains? % graph-kw) resp))]
    (log graphs)
    (doall
      (map (fn [{:keys [gndIdentifier preferredName variantName]
                 :as graph}]
             (let [info (make-info graph)
                   lifespan (make-lifespan graph)]
               (cond-> {:url ((keyword "@id") graph)
                        :id gndIdentifier
                        :name (:preferredName graph)}
                info  (assoc :info info))))
          graphs))))
