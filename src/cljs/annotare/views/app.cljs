(ns annotare.views.app
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [subscribe dispatch]]
            [secretary.core :as secretary]
            [annotare.util :refer [indexed]]
            [annotare.views.admin :refer [admin-panel]]
            [annotare.views.tagging :refer [tagging-panel]]))

(defn nav-link [uri title page-key active-page]
  "Entry in the navigation bar for parts of the application"
  [:span.header-item
   [:a
    {:class (when (= active-page page-key) "is-active")
      :href uri
      :on-click #(dispatch [:toggle-nav])}
    title]])

(defn navbar [active-page]
  (let [collapsed? (subscribe [:nav-collapsed?])]
    (fn []
      [:header.header
       [:div.container
        [:div.header-left
         [:a.header-item {:href "#/"} "annotare"]]
        [:span.header-toggle
          {:on-click #(dispatch [:toggle-nav])}
          [:span] [:span] [:span]]
        [:div.header-right.header-menu
         {:class (when-not @collapsed? "is-active")}
         [nav-link "#/" "Home" :front active-page]
         [nav-link "#/admin" "Admin" :admin active-page]]]])))

(defn front-panel []
  (let [projects (subscribe [:projects])]
    (fn []
      (if (= 1 (count @projects))
        (secretary/dispatch! (str "/tag/" (-> @projects vals first :id)))
        [:section.hero>div.hero-content>div.container
          [:h1.title "Hi there!"]
          [:h2.subtitle "Pick a project to start tagging."]
          [:ul
            (for [[idx {:keys [id name]}] (indexed (vals @projects))]
              ^{:key idx} [:li [:a {:href (str "#/tag/" id)} name]])]]))))


(defn delete-modal [{:keys [object-type object-id]}]
  (let [obj (subscribe [object-type object-id])]
    (fn []
      [:div.modal.is-active
       [:div.modal-background]
       [:div.modal-container>div.modal-content>div.modal-box
        (let [typename  (str object-type)
              title     (:name  @obj)
              del-sents (case object-type
                          :project (reduce + (map :sentence_count (:documents @obj)))
                          :document (:sentence_count @obj))
              del-docs (when (= object-type :project)
                         (count (:documents @obj)))]
          [:div
           [:h1.title "Are you sure you want to delete the project "
            [:em (str "\"" title "\"")] "?"]
           [:p "This will " [:strong "irrevocably "] "delete "
            (when del-docs  [:span [:strong del-docs] " documents and "])
            (when del-sents [:span [:strong del-sents] " sentences"])
            "."]
           ; TODO: Add "Yes I am sure" checkbox and make delete button inactive
           ;       until it is clicked
           [:button.button {:on-click #(dispatch [:toggle-modal])} "Cancel"]
           [:button.button.is-danger {:on-click #(dispatch [:delete object-type object-id])} "Delete"]])]
       [:button.modal-close {:on-click #(dispatch [:toggle-modal])}]])))

(defn annotare-app []
  (let [panel (subscribe [:active-panel])
        modal-info (subscribe [:active-modal])]
    (fn []
      [:div
       (when-let [mtype (:type @modal-info)]
        (case mtype
          :delete [delete-modal (dissoc @modal-info :type)]
          nil))
       [navbar @panel]
       (case @panel
         :front [front-panel]
         :admin [admin-panel]
         :tag   [tagging-panel])])))
