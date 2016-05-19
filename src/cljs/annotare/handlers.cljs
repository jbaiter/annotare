(ns annotare.handlers
  (:require-macros [annotare.macros :refer [log]])
  (:require
    [clojure.string :as string]
    [annotare.db   :refer [default-value]]
    [annotare.util.common :refer [pluralize-kw]]
    [annotare.util.core :refer [make-load-key jsonp]]
    [annotare.util.gnd :refer [tag->gnd-type make-gnd-querystr parse-lobid]]
    [annotare.middleware :refer [debug]]
    [re-frame.core :refer [dispatch register-handler path after]]
    [ajax.core     :refer [POST GET PUT DELETE raw-response-format]]
    [goog.json     :as     goog-json]))


(def headers {"Accept" "application/transit+json"})
(def default-mw [(when ^boolean goog.DEBUG debug)])
(def min-num-sentences 10)

(extend-type js/FileList
  ISeqable
  (-seq [array] (array-seq array 0)))

(defn make-submit-handler [type is-new? load-key success-events]
  (fn [data]
    (let [db-key (pluralize-kw type)]
      (when is-new?
        (dispatch [:set [db-key :new] nil]))
      (dispatch [:set [db-key (:id data)] data]))
    (dispatch [:unset :loading? load-key])
    (doseq [ev success-events] (dispatch ev))))

(defn make-fetch-handler [type amount load-key success-events]
  (fn [data]
    (let [db-key (pluralize-kw type)
          data (if (= :single amount) [data] data)
          mapped (reduce #(assoc %1 (:id %2) %2) {} data)]
      (dispatch [:merge [db-key] mapped]))
    (dispatch [:unset :loading? load-key])
    (doseq [ev success-events] (dispatch ev))))

(defn make-endpoint
  ([type] (str "/api/" (name type)))
  ([type data]
   (let [base    "/api"
         is-new? (not (contains? data :id))]
     (if-let [id (:id data)]
       (str (make-endpoint type) "/" id)
       (case type
         :sentence (str base "/document/" (:document_id data) "/sentences")
         :document (str base "/project/" (:project_id data) "/documents")
         (make-endpoint type))))))

;; Backend communication
(register-handler
  :submit
  default-mw
  (fn [app-db [_ type id load-key success-events]]
    (let [is-new? (= :new id)
          db-key (pluralize-kw type)
          data (get-in app-db [db-key id])
          meth (if is-new? POST PUT)
          endpoint (make-endpoint type data)
          load-key (or load-key (make-load-key [:submit type id]))]
      (meth endpoint
            {:headers headers
             :params data
             :handler (make-submit-handler type is-new? load-key success-events)
             :error-handler #(dispatch [:bad-response %])})
      (assoc-in app-db [:loading? load-key] true))))

(register-handler
  :fetch
  default-mw
  (fn [app-db [_ type amount id load-key success-events]]
    (let [endpoint (case amount
                     :single (make-endpoint type {:id id})
                     :all (case type
                            :sentence (make-endpoint type {:document_id id})
                            :document (make-endpoint type {:project_id id})
                            (make-endpoint type)))
          load-key (or load-key (make-load-key [:fetch type amount id]))]
      (GET endpoint
           {:headers headers
            :handler (make-fetch-handler type amount load-key success-events)
            :error-handler #(dispatch [:bad-response %])})
      (assoc-in app-db [:loading? load-key] true))))

(register-handler
  :delete
  default-mw
  (fn [app-db [_ obj-type obj-id success-events]]
    (let [endpoint (make-endpoint obj-type {:id obj-id})]
      (DELETE endpoint
              {:headers headers
               :handler #(do (dispatch [:unset (pluralize-kw obj-type) obj-id])
                             (dispatch [:toggle-modal]))
               :error-handler #(dispatch [:bad-response %])}))
    app-db))

(register-handler
  :submit-sentence
  default-mw
  (fn [app-db _]
    (let [sent (:active-sentence app-db)
          load-key :submit-sentence]
      (PUT
        (str "/api/sentence/" (:id sent))
        {:headers headers
         :params sent
         :handler (make-submit-handler :sentence false load-key [])
         :error-handler #(dispatch [:bad-response %])})
      (-> app-db
          (update :start-time #(if (nil? %) (.getTime (js/Date.)) %))
          (assoc-in [:loading? load-key] true)))))

(register-handler
  :fetch-random-sentences
  default-mw
  (fn [app-db [_ success-events]]
    (let [proj-id (:active-project app-db)
          load-key :fetch-random-sentences]
      (GET
        (str "/api/project/" proj-id "/random-untagged")
        {:headers headers
         :params {:num (* 1.5 min-num-sentences)}
         :handler #(do (dispatch [:add-sentences %])
                       (dispatch [:unset :loading? load-key])
                       (doseq [ev success-events] (dispatch ev)))
         :error-handler #(dispatch [:bad-response %])})
      (assoc-in app-db [:loading? load-key] true))))

(register-handler
  :add-sentences
  default-mw
  (fn [app-db [_ sentences]]
    (update app-db :sentence-queue #(apply (partial conj %) sentences))))

(register-handler
  :upload-documents
  default-mw
  (fn [app-db [_ proj-id load-key]]
    (let [{:keys [files type]} (:upload app-db)]
      (if (not (empty? files))
        (do
          (dispatch [:upload-document proj-id type (first files)
                                      [[:upload-documents proj-id load-key]]])
          (-> app-db
              (update-in [:upload :files] rest)
              (assoc-in [:loading? load-key] true)))
        (-> app-db
            (assoc-in [:upload :files] nil)
            (update :loading? dissoc load-key))))))

(register-handler
  :upload-document
  default-mw
  (fn [app-db [_ proj-id fmt fobj success-events]]
    (let [form-data (doto (js/FormData.)
                          (.append "file" fobj))
          load-key (make-load-key [:upload-document proj-id (.-name fobj)])]
      (POST
        (str "/api/project/" proj-id  "/documents/import/" fmt)
        {:headers headers
         :body form-data
         :handler (make-submit-handler :document false load-key success-events)})
      (assoc-in app-db [:loading? load-key] true))))

(register-handler
  :query-gnd
  default-mw
  (fn [app-db [_ name tag load-key]]
    (let [load-key (or load-key
                       (make-load-key [:query-gnd tag name]))]
      (do
        (jsonp "http://lobid.org/subject"
              {:params {:q (make-gnd-querystr name)
                        :format "full"
                        :type (str "http://d-nb.info/standards/elementset/gnd%23"
                                  (tag->gnd-type tag))}
                :handler (fn [data]
                            (dispatch [:set [:loading? load-key] false])
                            (dispatch [:set [:gnd-queries load-key]
                                       (parse-lobid data)]))
                :error-handler #(dispatch [:bad-response %])
                :keywordize-keys? true})
        (assoc-in app-db [:loading load-key] true)))))

(register-handler
  :next-sentence
  default-mw
  (fn [app-db _]
    (let [active-sent (:active-sentence app-db)
          sent (peek (:sentence-queue app-db))
          newq (pop (:sentence-queue app-db))
          cur-cnt (count newq)]
      (when (and (not (= cur-cnt 0)) (< cur-cnt min-num-sentences))
        (dispatch [:fetch-random-sentences]))
      ;; Scroll to the top of the viewport
      (.scrollTo js/window 0 0)
      (-> app-db
          (#(if (nil? active-sent)
              %
              (-> %
                  (assoc-in [:sentences (:id active-sent)] active-sent)
                  (update :sentence-history conj (:id active-sent)))))
          (assoc :active-sentence sent)
          (assoc :sentence-queue newq)))))

(register-handler
  :previous-sentence
  default-mw
  (fn [app-db _]
    (let [cur-active (:active-sentence app-db)
          prev-active-id (-> app-db :sentence-history last)
          prev-active (-> app-db :sentences (get prev-active-id))]
      (-> app-db
          (assoc :active-sentence prev-active)
          (update :sentence-history pop)
          (update :sentences dissoc prev-active-id)
          (update :sentence-queue #(-> cljs.core/PersistentQueue.EMPTY
                                       (conj cur-active)
                                       (into %)))))))

(register-handler
  :bad-response
  default-mw
  (fn [app-db [_ error]]
    (.error js/console (str error))
    (assoc app-db :error {:message "There was a problem while communicating with the server."
                          :cause   error})))

(register-handler
  :clear-sentence-queue
  default-mw
  (fn [app-db _]
    (assoc app-db :sentence-queue cljs.core/PersistentQueue.EMPTY)))

;; UI handlers
(register-handler
  :set-panel
  default-mw
  (fn [app-db [_ new-panel & args]]
    (case new-panel
      :tag (let [[proj-id] args]
             (dispatch [:set-active-project proj-id])
             (when (not (= (:active-project app-db) proj-id))
               (dispatch [:clear-sentence-queue])
               (dispatch [:set [:loading? :initial-sentences] true])
               (dispatch [:fetch-random-sentences [[:next-sentence]
                                                   [:unset :loading? :initial-sentences]]])))
      nil)
    (-> app-db
        (assoc :active-panel new-panel))))

(register-handler
  :toggle-nav
  [(path :nav-collapsed?) default-mw]
  (fn [collapsed? [_]]
    (not collapsed?)))

(register-handler
  :set-active-project
  default-mw
  (fn  [app-db [_ new-id]]
    (when (not (= (:active-project app-db) new-id))
      (dispatch [:fetch :document :all new-id]))
    (assoc app-db :active-project new-id)))

(register-handler
  :toggle-form
  [(path :active-form) default-mw]
  (fn [active-form [_ new-form]]
    new-form))

(register-handler
  :toggle-modal
  [(path :active-modal) default-mw]
  (fn [active-modal [_ modal-type & [object-type object-id]]]
    (when modal-type
      {:type modal-type
       :object-type object-type
       :object-id object-id})))

(register-handler
  :update-tag
  default-mw
  (fn [app-db [_ idx tag]]
    (-> app-db
        (update :start-time #(if (nil? %) (.getTime (js/Date.)) %))
        (update-in [:active-sentence :tags] #(assoc % idx tag)))))

;; Miscellaneous
(register-handler
  :set
  default-mw
  (fn [db [_ ks v]]
    (assoc-in db ks v)))

(register-handler
  :unset
  default-mw
  (fn [db [_ & ks]]
    (update-in db (drop-last ks) dissoc (last ks))))

(register-handler
  :merge
  default-mw
  (fn [db [_ ks m]]
    (update-in db ks merge m)))

(register-handler
  :push
  default-mw
  (fn [db [_ ks v]]
    (update-in db ks conj v)))

(register-handler
  :initialise-db
  default-mw
  (fn [db _]
    (merge default-value db)))
