(ns annotare.views.forms.tagset
  (:require [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :as string]
            [annotare.util.core :refer [ev->val]]
            [annotare.views.forms.common :refer [form-field]]))

(defn tagset-form [tagset-id]
  (let [tagset (subscribe [:get :tagsets tagset-id])
        update-fn #(dispatch [:set [:tagsets tagset-id %1] %2])]
    (fn []
      [:div.tagset-form
        (case tagset-id
         :new [:h1.title "Create a new tag set"]
         [:h1.title "Edit tag set " [:em (:name tagset)]])
        [form-field "Name"
          [:input.input {:type "text"
                         :default-value (:name @tagset)
                         :on-blur #(update-fn :name (ev->val %))}]]
        [form-field "Documentation"
          [:textarea.textarea
           {:placeholder "You can use Markdown in here"
            :default-value (:documentation @tagset)
            :on-blur #(update-fn :documentation (ev->val %))}]]
        [form-field "Tags"
         [:input.input {:type "text"
                        :placeholder "Tags must be comma-separated"
                        :default-value (->> @tagset :tags (string/join ", "))
                        :on-change #(update-fn
                                      :tags
                                      (-> %
                                          ev->val
                                          (string/split #",\s*")
                                          set))}]]
        (when (-> @tagset :tags empty? not)
          [form-field "Empty Tag"
            [:span.select>select
              {:value (:empty_tag @tagset)
               :on-change #(update-fn :empty_tag (ev->val %))}
              [:option ""]
              (for [tag (:tags @tagset)]
                ^{:key tag} [:option tag])]])
        [:p.control
         ;; TODO: Use schema for this!
         ;; TODO: Highlight missing fields
         (let [required-filled? (and (every? #(contains? @tagset %) [:name :tags :empty_tag])
                                     (not (empty? (:empty_tag @tagset))))]
          [:button.button.is-primary
           {:class (when (not required-filled?) "is-disabled")
            :on-click #(do (dispatch [:submit :tagset tagset-id])
                           (dispatch [:set [:active-form] nil]))}
           "Save"])]])))
