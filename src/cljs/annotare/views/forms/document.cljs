(ns annotare.views.forms.document
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as reagent :refer [atom]]
            [clojure.string :as string]
            [annotare.util :refer [ev->val]]
            [annotare.views.forms.common :refer [form-field]]))

(defn document-form [document-id]
  (let [document (subscribe [:get :documents document-id])
        update-fn #(dispatch [:set [:documents document-id %1] %2])]
    (fn []
      [:div.document-form
        (case document-id
         :new [:h1.title "Create a new document"]
         [:h1.title "Edit document " [:em (:name @document)]])
        [form-field "Name"
         [:input.input {:type "text"
                        :default-value (:name @document)
                        :on-blur #(update-fn :name (ev->val %))}]]
        [:p.control
         ;; TODO: Use schema for this!
         ;; TODO: Highlight missing fields
         (let [required-filled? (contains? @document :name)]
          [:button.button.is-primary
           {:class (when (not required-filled?) "is-disabled")
            :on-click #(do (dispatch [:submit :document document-id])
                           (dispatch [:set [:active-form] nil])
                           (dispatch [:set [:active-document] nil]))}
           "Save"])]])))
