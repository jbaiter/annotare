(ns annotare.handlers
  (:require
    [annotare.db   :refer [default-value]]
    [re-frame.core :refer [dispatch register-handler path after debug]]
    [ajax.core     :refer [GET PUT]]))


(def headers {"Accept" "application/transit+json"})
(def default-mw [(when ^boolean goog.DEBUG debug)])


;; Backend communication
(register-handler
  :fetch-random-sentence
  [default-mw]
  (fn [app-db _]
    (let [proj-id (:active-project app-db)]
      (GET
        (str "/api/project/" proj-id "/random-untagged")
        {:headers headers
         :handler       #(dispatch [:process-sentence %])
         :error-handler #(dispatch [:bad-response %])}))
    (assoc app-db :loading? true)))

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
    (assoc app-db :loading? true)))

(register-handler
  :fetch-projects
  default-mw
  (fn [app-db _]
    (GET "/api/project"
         {:headers headers
          :handler        #(dispatch [:process-projects %1])
          :error-handler  #(dispatch [:bad-response %1])})
    (assoc app-db :loading? true)))

(register-handler
  :fetch-project-documents
  default-mw
  (fn [app-db _]
    (let [proj-id (-> app-db :active-project)]
      (GET (str "/api/project/" proj-id "/documents")
           {:headers headers
            :handler        #(dispatch [:process-documents %1])
            :error-handler  #(dispatch [:bad-response %1])}))
    (assoc app-db :loading? true)))

;; Backend response handlers
(register-handler
  :process-sentence
  default-mw
  (fn [app-db [_ sentence]]
    (-> app-db
        (assoc :active-sentence sentence)
        (assoc :loading? false))))

(register-handler
  :process-projects
  default-mw
  (fn [app-db [_ projects]]
    (-> app-db
        (assoc :loading? false)
        (assoc :projects (reduce #(assoc %1 (:id %2) %2) {} projects)))))

(register-handler
  :process-documents
  default-mw
  (fn [app-db [_ docs]]
    (let [doc-map (reduce #(assoc %1 (:id %2) %2) {} docs)]
      (-> app-db
          (update :documents #(merge % doc-map))
          (assoc :loading? false)))))

(register-handler
  :submit-sent-complete
  default-mw
  (fn [app-db [_ _]]
    (dispatch [:fetch-random-sentence])
    (assoc app-db :loading? false)))

(register-handler
  :bad-response
  default-mw
  (fn [app-db [_ error]]
    (.error js/console (str error))
    (assoc app-db :error {:message "There was a problem while communicating with the server."})))

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
  [(path :active-project) default-mw]
  (fn  [old-id [_ new-id]]
    (dispatch [:fetch-project-documents])
    new-id))

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
