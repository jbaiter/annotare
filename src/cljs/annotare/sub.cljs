(ns annotare.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [dispatch register-sub subscribe]]))

(defn materialize-project [docs proj]
  "Retrieve the project and its documents from the db"
  (assoc proj :documents
    (filter #(= (:project_id %) (:id proj)) (vals docs))))

(register-sub
  :projects
  (fn [db _]
    (reaction (:projects @db))))


(register-sub
  :documents
  (fn [db _]
    (reaction (:documents @db))))

(register-sub
  :project
  (fn [db [_ id]]
    (let [projs (subscribe [:projects])
          docs  (subscribe [:documents])]
      (reaction (materialize-project @docs (get @projs id))))))

(register-sub
  :document
  (fn [db [_ id]]
    (let [docs (subscribe [:documents])]
      (reaction (get @docs id)))))

(register-sub
  :active-project
  (fn [db _]
    (let [docs (subscribe [:documents])
          projs (subscribe [:projects])]
      (reaction (when-let [id (:active-project @db)]
                  (materialize-project @docs (get @projs id)))))))

(register-sub
  :active-sentence
  (fn [db _]
    (reaction (:active-sentence @db))))

(register-sub
  :nav-collapsed?
  (fn [db _]
    (reaction (:nav-collapsed? @db))))

(register-sub
  :active-panel
  (fn [db _]
    (reaction (:active-panel @db))))

(register-sub
  :active-modal
  (fn [db _]
    (reaction (:active-modal @db))))
