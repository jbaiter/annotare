(ns annotare.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [dispatch register-sub subscribe]]
            [cljs.pprint :refer [pprint]]))

;; Very simple 'getter' subscription for unmaterialised  views on the
;; application state
(register-sub
  :get
  (fn [db [_ & ks]]
    (reaction (get-in @db ks))))


;; Pull in tagset and documents along with the project
(register-sub
  :project
  (fn [db [_ id]]
    (let [projs (subscribe [:get :projects])
          docs  (subscribe [:project-documents id])
          tagsets (subscribe [:get :tagsets])]
      (reaction (-> (get @projs id)
                    (assoc :documents docs)
                    (#(assoc % :tagset (get @tagsets (:tagset_id %)))))))))


;; All documents for a given projectt
(register-sub
  :project-documents
  (fn [db [_ id]]
    (let [docs (subscribe [:get :documents])]
      (reaction (filter #(= id (:project_id %)) (vals @docs))))))


;; TODO: Isn't there a way to make this use the :project subscription?
(register-sub
  :active-project
  (fn [db _]
    (let [docs (subscribe [:get :documents])
          projs (subscribe [:get :projects])
          tagsets (subscribe [:get :tagsets])]
      (reaction (when-let [proj (get @projs (:active-project @db))]
                  (-> proj
                      (assoc :documents
                        (filter #(= (:project_id %) (:id proj)) (vals @docs)))
                      (assoc :tagset (get @tagsets (:tagset_id proj)))))))))

(register-sub
  :num-tagged-sentences
  (fn [db _]
    (let [sentences (subscribe [:get :sentences])]
      (reaction (count (filter #(> (:num_edits %) 0) (vals @sentences)))))))
