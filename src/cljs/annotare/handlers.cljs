(ns annotare.handlers
  (:require
    [annotare.db   :refer [default-value]]
    [re-frame.core :refer [dispatch register-handler path after debug]]
    [ajax.core     :refer [GET PUT]]))


(def headers {"Accept" "application/transit+json"})
(def default-mw [(when ^boolean goog.DEBUG debug)])
(def min-num-sentences 10)


;; Backend communication
(register-handler
  :fetch-random-sentences
  [default-mw]
  (fn [app-db [_ & success-events]]
    (let [proj-id (:active-project app-db)]
      (GET
        (str "/api/project/" proj-id "/random-untagged")
        {:headers headers
         :params {:num (* 1.5 min-num-sentences)}
         :handler       #(dispatch [:process-sentences % success-events])
         :error-handler #(dispatch [:bad-response %])}))
    (update app-db :loading? inc)))

(register-handler
  :next-sentence
  [default-mw]
  (fn [app-db _]
    (let [sent (peek (:sentence-queue app-db))
          newq (pop (:sentence-queue app-db))
          cur-cnt (count newq)]
      (when (and (not (= cur-cnt 0)) (< cur-cnt min-num-sentences))
        (dispatch [:fetch-random-sentences]))
      (-> app-db
          (assoc :active-sentence sent)
          (assoc :sentence-queue newq)))))

(register-handler
  :submit-sentence
  [default-mw]
  (fn [app-db _]
    (let [sent (:active-sentence app-db)]
      (PUT
        (str "/api/sentence/" (:id sent))
        {:headers headers
         :params sent
         :handler       #(dispatch [:submit-sent-complete %])
         :error-handler #(dispatch [:bad-response %])}))
    (update app-db :loading? inc)))

(register-handler
  :fetch-projects
  default-mw
  (fn [app-db _]
    (GET "/api/project"
         {:headers headers
          :handler        #(dispatch [:process-projects %1])
          :error-handler  #(dispatch [:bad-response %1])})
    (update app-db :loading? inc)))

(register-handler
  :fetch-project-documents
  default-mw
  (fn [app-db _]
    (let [proj-id (-> app-db :active-project)]
      (GET (str "/api/project/" proj-id "/documents")
           {:headers headers
            :handler        #(dispatch [:process-documents %1])
            :error-handler  #(dispatch [:bad-response %1])}))
    (update app-db :loading? inc)))

(register-handler
  :fetch-tagsets
  default-mw
  (fn [app-db _]
    (GET "/api/tagset"
         {:headers headers
          :handler        #(dispatch [:process-tagsets %])
          :error-handler  #(dispatch [:bad-response %])})
    (update app-db :loading? inc)))

;; Backend response handlers
(register-handler
  :process-sentences
  default-mw
  (fn [app-db [_ sentences success-events]]
    (doseq [ev success-events] (dispatch ev))
    (-> app-db
        (update :loading? dec)
        (update :sentence-queue #(apply (partial conj %) sentences)))))

(register-handler
  :process-projects
  default-mw
  (fn [app-db [_ projects]]
    (-> app-db
        (assoc :projects (reduce #(assoc %1 (:id %2) %2) {} projects))
        (update :loading? dec))))

(register-handler
  :process-documents
  default-mw
  (fn [app-db [_ docs]]
    (let [doc-map (reduce #(assoc %1 (:id %2) %2) {} docs)]
      (-> app-db
          (update :documents #(merge % doc-map))
          (update :loading? dec)))))

(register-handler
  :process-tagsets
  default-mw
  (fn [app-db [_ tagsets]]
    (let [ts-map (reduce #(assoc %1 (:id %2) %2) {} tagsets)]
      (-> app-db
          (update :tagsets #(merge % ts-map))
          (update :loading? dec)))))

(register-handler
  :submit-sent-complete
  default-mw
  (fn [app-db [_ _]]
    (dispatch [:next-sentence])
    (update app-db :loading? dec)))

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
  (fn [app-db [_ new-panel]]
    (-> app-db
        (assoc :active-panel new-panel))))

(register-handler
  :toggle-nav
  [(path :nav-collapsed?) default-mw]
  (fn [collapsed? [_]]
    (not collapsed?)))

(register-handler
  :set-active-project
  [default-mw]
  (fn  [app-db [_ new-id]]
    (when (not (= (:active-project app-db) new-id))
      (dispatch [:clear-sentence-queue])
      (dispatch [:fetch-project-documents]))
    (assoc app-db :active-project new-id)))

(register-handler
  :toggle-form
  [(path :active-form) default-mw]
  (fn [active-form [_ new-form]]
    new-form))

(register-handler
  :toggle-modal
  [(path :active-modal) default-mw]
  (fn [active-modal [_ modal-type object-type object-id]]
    (when modal-type
      {:type modal-type
       :object-type object-type
       :object-id object-id})))

(register-handler
  :update-tag
  [(path :active-sentence) default-mw]
  (fn [sent [_ idx tag]]
    (update sent :tags #(assoc % idx tag))))

;; Miscellaneous
(register-handler
  :initialise-db
  default-mw
  (fn [db _]
    (merge default-value db)))
