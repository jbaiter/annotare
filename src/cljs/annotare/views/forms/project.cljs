(ns annotare.views.forms.project
  (:require [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :as string]
            [annotare.util.core :refer [ev->val]]
            [annotare.views.forms.common :refer [form-field]]))

(defn project-form [project-id]
  (let [project (subscribe [:get :projects project-id])
        tagsets (subscribe [:get :tagsets])
        update-fn #(dispatch [:set [:projects project-id %1] %2])]
    (fn []
      [:div.project-form
        (case project-id
         :new [:h1.title "Create a new project"]
         [:h1.title "Edit project " [:em (:name project)]])
        [form-field "Name"
         [:input.input {:type "text"
                        :default-value (:name @project)
                        :on-blur #(update-fn :name (ev->val %))}]]
        [form-field "Description"
          [:textarea.textarea
            {:default-value (:description @project)
             :on-blur #(update-fn :description (ev->val %))}]]
        [form-field "Tagset"
          [:span.select>select
            {:value (:tagset_id @project)
             :on-change #(update-fn :tagset_id (-> % ev->val js/parseInt))}
            [:option ""]
            (for [{:keys [id name]} (vals @tagsets)]
              ^{:key id} [:option {:value id} name])]]
        [:p.control
         ;; TODO: Use schema for this!
         ;; TODO: Highlight missing fields
         (let [required-filled? (and (every? #(contains? @project %) [:name :tagset_id])
                                     (not (js/isNaN (:tagset_id @project))))]
          [:button.button.is-primary
           {:class (when (not required-filled?) "is-disabled")
            :on-click #(do (dispatch [:submit :project project-id])
                           (dispatch [:set [:active-form] nil]))}
           "Save"])]])))
