(ns annotare.views.admin
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [subscribe dispatch]]))

(defn project-menu [projects active-project]
  [:nav.menu.project-menu
    [:p.menu-heading "Projects"]
    (for [{id :id proj-name :name} projects]
      ^{:key id}
      [:a.menu-block
       {:class (when (= id (:id active-project)) "is-active")
        :on-click #(dispatch [:set-active-project id])}
       proj-name])
    [:div.menu-block
     [:button.button.is-primary
      {:on-click #(dispatch [:toggle-form :project])}
      "New project"]]])

(defn project-toolbar [proj]
  [:div.project-toolbar
   [:a.button.is-primary
    {:on-click #(dispatch [:toggle-form :document (:id proj)])}
    "New document"]
   [:a.button.is-danger
    {:on-click #(dispatch [:toggle-modal :delete :project (:id proj)])}
    "Delete project"]])

(defn project-info [{:keys [id name description]}]
  [:div.project-info
   [:h1.title name]
   [:blockquote description]])

(defn document-table [documents]
  [:table.table
   [:thead>tr
    [:th "Name"]
    [:th "# sentences"]
    [:th "# tagged"]
    [:th "Actions"]]
   [:tbody
    ;; TODO: When clicked, display nested table of sentences below
    (for [{:keys [id name sentence_count untagged_count] :as doc} documents]
      ^{:key id}
      [:tr
        [:td name]
        [:td sentence_count]
        [:td untagged_count]
        [:td.table-link>a
          {:on-click #(dispatch [:toggle-modal :delete :document (:id doc)])}
          "Delete"]])]])

(defn admin-panel []
  (let [projects (subscribe [:projects])
        active-project (subscribe [:active-project])]
    (fn []
      [:div.container>div.columns
       [:div.column.is-3
        [project-menu (vals @projects) @active-project]]
       (if @active-project
        [:div.column
          [project-info @active-project]
          [project-toolbar @active-project]
          [document-table (:documents @active-project)]]
        [:div.column>h1.title "← Pick a project on the left."])])))
