(ns annotare.db.core
  (:require
    [clojure.string :as string]
    [clojure.java.jdbc :as jdbc]
    [annotare.db.queries :as q]))


;; Util
(defn- col->set [col-key row]
  (update row col-key #(set (string/split % #" "))))

(defn- set->col [col-key row]
  (update row col-key (partial string/join " ")))

(defn- get-insert-id [x]
  ((keyword "last_insert_rowid()") x))

(defn- row->sent [r]
  (reduce (fn [m k] (update-in m [k] #(string/split % #" "))) r [:tokens :tags]))

(defn- sent->row [s]
  (reduce (fn [m k] (update-in m [k] (partial string/join " "))) s [:tokens :tags]))

(defn reduce-tags [tags]
  "Reduce tags to their basic form, i.e. remove `B-` and `I-` prefixes"
  (map #(string/replace % #"B-|I-" "") tags))


;; Projects
(defn get-project [id]
  (when-let [project (first (q/get-project {:id id}))]
    project))

(def get-projects q/get-projects)

(defn get-project-documents [id]
  (q/get-project-documents {:id id}))

(defn get-random-sentence
  ([project-id]
   (first (get-random-sentence project-id 1)))
  ([project-id number]
   (when-let [rows (q/get-untagged-sentences {:id project-id
                                              :limit number})]
     (map row->sent rows))))

(defn create-project! [params]
  (-> params
      q/create-project<!
      get-insert-id
      get-project))

(defn delete-project! [id]
  (let [deleted (get-project id)]
    (q/delete-project! {:id id})
    deleted))

(defn update-project! [{:keys [id] :as params}]
  (q/update-project! params)
  (get-project id))


;; Tagsets
(defn- verify-tagset [t]
  (assert (contains? (:tags t) (:empty_tag t))
          "The chosen :empty_tag is not in the :tags!"))

(defn get-tagset [id]
  (when-let [tagset (first (q/get-tagset {:id id}))]
    (col->set :tags tagset)))

(defn get-tagsets []
  (map (partial col->set :tags) (q/get-tagsets)))

(defn create-tagset! [params]
  (verify-tagset params)
  (->> params
       (set->col :tags)
       q/create-tagset<!
       get-insert-id
       get-tagset))

(defn delete-tagset! [id]
  (let [deleted (get-tagset id)]
    (q/delete-tagset! {:id id})
    deleted))

(defn update-tagset! [{:keys [id] :as params}]
  (verify-tagset params)
  (q/update-tagset! (set->col :tags params))
  (get-tagset id))


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
(defn get-sentence-tagset [id]
  (when-let [tagset (first (q/get-sentence-tagset {:id id}))]
    (col->set :tags tagset)))

(defn get-document-tagset [id]
  (when-let [tagset (first (q/get-document-tagset {:id id}))]
    (col->set :tags tagset)))

(defn get-project-tagset [id]
  (when-let [tagset (first (q/get-project-tagset {:id id}))]
    (col->set :tags tagset)))

(defn- verify-sentence [s]
  (let [tagset (-> s :document_id get-document-tagset :tags)
        tags (-> s :tags reduce-tags)
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

(defn create-sentences! [sentences]
  (doseq [sent sentences]
    (verify-sentence sent))
  (->> sentences
       (map sent->row)
       (apply (partial jdbc/insert! q/conn :sentences))
       (count)))

(defn delete-sentence! [id]
  (let [deleted (get-sentence id)]
    (q/delete-sentence! {:id id})
    deleted))

(defn update-sentence! [{:keys [id] :as params}]
  (verify-sentence params)
  (q/update-sentence! (sent->row params))
  (get-sentence id))
