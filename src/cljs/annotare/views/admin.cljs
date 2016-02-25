(ns annotare.views.admin
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :as string]
            [cljs.pprint :refer [pprint]]
            [annotare.util :refer [ev->val]]
            [annotare.views.common :refer [icon]]
            [annotare.views.forms.tagset :refer [tagset-form]]
            [annotare.views.forms.project :refer [project-form]]
            [annotare.views.forms.document :refer [document-form]]))

(defn project-menu [projects active-project]
  [:nav.menu.project-menu
    [:p.menu-heading "Projects"
     [:a.button.is-primary.is-pulled-right
      {:on-click (fn []
                   (dispatch [:set [:active-project] :new])
                   (dispatch [:toggle-form :project]))
       :title "Create new project"}
      [icon :plus :small]]]
    (for [{id :id proj-name :name}
          (filter #(contains? % :id) projects)]
      ^{:key id}
      [:a.menu-block
       {:class (when (= id (:id active-project)) "is-active")
        :on-click #(dispatch [:set-active-project id])}
       proj-name])])

(defn tagset-menu [tagsets active-tagset]
  [:nav.menu.tagset-menu
    [:p.menu-heading "Tag sets"
     [:a.button.is-primary.is-pulled-right
      {:on-click (fn []
                   (dispatch [:set [:active-tagset] :new])
                   (dispatch [:toggle-form :tagset]))
       :title "Create new tag set"}
      [icon :plus :small]]]
    (for [{id :id proj-name :name}
          (filter #(contains? % :id) tagsets)]
      ^{:key id} [:a.menu-block
                  {:class (when (= id active-tagset) "is-active")
                   :on-click #(do (dispatch [:set [:active-tagset] id])
                                  (dispatch [:toggle-form :tagset]))}
                  proj-name])])


(defn project-toolbar [proj]
  (let [load-key :upload-doc
        upload (subscribe [:get :upload])
        uploading? (subscribe [:get :loading? load-key])]
    (fn []
      [:div.project-toolbar
        [:p.control.is-grouped
          [:input.input
           {:type "file"
            :multiple true
            :on-change #(let [files (-> % .-target .-files)]
                          (dispatch [:set [:upload :files] files]))}]
          [:span.select>select
            {:value (:type @upload)
             :on-change #(dispatch [:set [:upload :type] (ev->val %)])}
            [:option {:value "txt"} "Plaintext"]
            [:option {:value "tcf"} "TCF XML"]]
          [:a.button.is-primary
            {:on-click #(dispatch [:upload-documents (:id proj) load-key])
             :class (when @uploading? "is-loading")}
            "Import documents"]]])))

(defn project-header [{:keys [id name description] :as proj}]
  [:div.project-header
   [:h1.title name
    [:a.button.is-danger.is-pulled-right
      {:on-click #(dispatch [:toggle-modal :delete :project id])
       :title "Delete project"}
      [icon :trash :small]]]
   [:blockquote description]])

(defn document-table [documents]
  [:table.table
   [:thead>tr
    [:th "Name"]
    [:th "# sentences"]
    [:th "# untagged"]
    [:th "Actions"]]
   [:tbody
    ;; TODO: When clicked, display nested table of sentences below
    (for [{:keys [id name sentence_count untagged_count] :as doc} documents]
      ^{:key id}
      [:tr
        [:td name]
        [:td sentence_count]
        [:td untagged_count]
        [:td.table-link>a.is-danger
          {:on-click #(dispatch [:toggle-modal :delete :document (:id doc)])
           :title "Delete document"}
          [icon :trash :small]]])]])

(defn admin-panel []
  (let [projects (subscribe [:get :projects])
        tagsets (subscribe [:get :tagsets])
        active-project (subscribe [:active-project])
        active-tagset (subscribe [:get :active-tagset])
        active-document (subscribe [:get :active-document])
        active-form (subscribe [:get :active-form])]
    (fn []
      [:div.section>div.container>div.columns
       [:div.column.is-3
        [tagset-menu (vals @tagsets) @active-tagset]
        (when (not (empty? @tagsets))
          [project-menu (vals @projects) @active-project])]
       [:div.column
        (cond
          @active-form
          (case @active-form
            :tagset [tagset-form (or @active-tagset :new)]
            :project [project-form (or @active-project :new)]
            :document [document-form (or @active-document :new)]
            [:h1.title "Form" (str @active-form)])

          @active-project
          [:div
            [project-header @active-project]
            [project-toolbar @active-project]
            [document-table (:documents @active-project)]]

          :else
          [:h1.title "Pick something"])]])))
