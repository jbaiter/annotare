(ns annotare.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as rf :refer [dispatch subscribe]]))


;; When running in debug mode, use a custom `register-sub` implementation that
;; records each subscription's runtime and offers functions to get an overview
;; TODO: Try to make these available in the `cljs/user` namespace
(if goog.DEBUG
  (defn register-sub [& args]
    (apply rf/register-sub args))
  (do
    (defonce subcounts (atom {}))

    (aset js/document "subcounts_table" #(.table js/console (clj->js
                                                              (for [[k vs]
                                                                    (reverse (sort-by :total-time @subcounts))]
                                                                (assoc vs :subscription (str k))))))

    (aset js/document "subcounts" #(.log js/console (into (sorted-map-by :total-time) @subcounts)))


    (aset js/document "subcountsclear" #(reset! subcounts {}))

    (aset js/document "subcounttotal" #(pr (apply + 0 (map :total-time (vals @subcounts)))))

    (defn update-sub-count
      [subcount duration]
      (let [call-count (inc (:call-count subcount 0))
            total-time (+ duration (:total-time subcount 0))
            average-time (/ total-time call-count)
            min-time     (min (:min-time subcount js/Infinity) duration)
            max-time     (max (:max-time subcount 0) duration)
            first-time   (:first-time subcount duration)
            last-time    duration]
        {:call-count call-count
         :total-time total-time
         :average-time average-time
         :first-time first-time
         :last-time last-time
         :min-time min-time
         :max-time max-time}))

    (defn register-sub
      ([k _ f] (register-sub k f))
      ([k f]   (rf/register-sub
                 k
                 (fn [db v] (let [child (f db v)]
                              (reaction
                                (let [before (.getTime (js/Date.))
                                      child @child
                                      after (.getTime (js/Date.))]
                                  (swap! subcounts update k update-sub-count (- after before))
                                  child)))))))))


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
