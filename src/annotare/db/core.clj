(ns annotare.db.core
  (:require
    [clojure.string :as s]
    [annotare.db.queries :as q]))

(defn- col->set [col-key row]
  (update row col-key #(set (s/split % #" "))))

(defn- set->col [col-key row]
  (update row col-key (partial s/join " ")))

(defn- get-insert-id [x]
  ((keyword "last_insert_rowid()") x))

(defn- row->sent [r]
  (reduce (fn [m k] (update-in m [k] #(s/split % #" "))) r [:tokens :tags]))

(defn- sent->row [s]
  (reduce (fn [m k] (update-in m [k] (partial s/join " "))) s [:tokens :tags]))

;; Projects
(defn- verify-project [p]
  (assert (contains? (:tagset p) (:empty_tag p))
          "The chosen :empty_tag is not in the :tagset!"))

(defn get-project [id]
  (when-let [project (first (q/get-project {:id id}))]
    (col->set :tagset project)))

(defn get-projects []
  (map (partial col->set :tagset) (q/get-projects)))

(defn get-project-documents [id]
  (q/get-project-documents {:id id}))

(defn create-project! [params]
  (verify-project params)
  (get-project (get-insert-id (q/create-project<! (set->col :tagset params)))))

(defn delete-project! [id]
  (let [deleted (get-project id)]
    (q/delete-project! {:id id})
    deleted))

(defn update-project! [{:keys [id] :as params}]
  (verify-project params)
  (q/update-project! (set->col :tagset params))
  (get-project id))


;; Documents
(defn get-document [id]
  (first (q/get-document {:id id})))

(def get-documents q/get-documents)

(defn get-document-sentences [id]
  (map row->sent (q/get-document-sentences {:id id})))

(defn create-document! [params]
  (get-document (get-insert-id (q/create-document<! params))))

(defn delete-document! [id]
  (let [deleted (get-document id)]
    (q/delete-document! {:id id})
    deleted))

(defn update-document! [{:keys [id] :as params}]
  (q/update-document! params)
  (get-document id))

;; Sentences
(defn get-sentence-tagset [s]
  (-> s
      :document_id
      get-document
      :project_id
      get-project
      :tagset))

(defn- verify-sentence [s]
  (let [tagset (get-sentence-tagset s)
        extra-tags (clojure.set/difference (set (:tags s)) tagset)]
    (assert (= (count (:tags s)) (count (:tokens s)))
            ":tags and :tokens must be of equal length!")
    (assert (empty? extra-tags)
            (str ":tags must only contain tags that are in the project's tagset.\n"
                 "Tags not in tagset: " extra-tags))))

(defn get-sentence [id]
  (when-let [row (first (q/get-sentence {:id id}))]
    (row->sent row)))

(defn get-sentences []
  (map row->sent (q/get-sentences)))

(defn create-sentence! [params]
  (verify-sentence params)
  (-> params
      sent->row
      q/create-sentence<!
      get-insert-id
      get-sentence))

(defn delete-sentence! [id]
  (let [deleted (get-sentence id)]
    (q/delete-sentence! {:id id})
    deleted))

(defn update-sentence! [{:keys [id] :as params}]
  (verify-sentence params)
  (q/update-sentence! (sent->row params))
  (get-sentence id))
