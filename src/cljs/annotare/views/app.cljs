(ns annotare.views.app
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [subscribe dispatch]]
            [secretary.core :as secretary]
            [pushy.core :as pushy]
            [cljs.pprint :refer [pprint]]
            [annotare.util :refer [indexed pluralize-kw]]
            [annotare.history :refer [history]]
            [annotare.views.admin :refer [admin-panel]]
            [annotare.views.tagging :refer [tagging-panel]]
            [markdown.core :refer [md->html]]))

(defn nav-link [uri title page-key active-page]
  "Entry in the navigation bar for parts of the application"
  [:span.header-item
    [:a
      {:class (when (= active-page page-key) "is-active")
        :href uri
        :on-click #(dispatch [:toggle-nav])}
      title]])

(defn navbar []
  (let [collapsed? (subscribe [:get :nav-collapsed?])]
    (fn [active-page]
       [:header.header
        [:div.container
          [:div.header-left
            [:a.header-item {:href "/"} "annotare"]]
          [:span.header-toggle
            {:on-click #(dispatch [:toggle-nav])}
            [:span] [:span] [:span]]
          [:div.header-right.header-menu
            {:class (when-not @collapsed? "is-active")}
            [nav-link "/" "Home" :front active-page]
            [nav-link "/admin" "Admin" :admin active-page]]]])))

(defn front-panel []
  (let [projects (subscribe [:get :projects])]
    (fn []
        [:section.hero>div.hero-content>div.container
          (case (count @projects)
            0 [:div
                [:h1.title "Looks like there are no projects at the moment."]
                [:h2.subtitle "Head over to the " [:a {:href "/admin"} "admin area"] " to create one."]]
            1 (do
                (pushy/set-token! history (str "/tag/" (-> @projects vals first :id)))
                [:div.loading-spinner])
            [:div
              [:h1.title "Hi there!"]
              [:h2.subtitle "Pick a project to start tagging."]
              [:ul
                (for [[idx {:keys [id name]}] (indexed (vals @projects))]
                  ^{:key idx} [:li [:a {:href (str "#/tag/" id)} name]])]])])))


(defn delete-modal [{:keys [object-type object-id]}]
  (let [obj (subscribe [:get (pluralize-kw object-type) object-id])]
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

(defn tagging-help-modal [{:keys [object-id]}]
  (let [tagset (subscribe [:get :tagsets object-id])]
    (fn []
      [:div.modal.is-active
       [:div.modal-background]
       (let [{:keys [name documentation]} @tagset]
        [:div.modal-container>div.modal-content>div.modal-box
          [:h1.title "Guidelines for tagset \"" name "\""]
          [:div {:dangerouslySetInnerHTML {:__html (md->html documentation)}}]
          [:button.modal-close {:on-click #(dispatch [:toggle-modal])}]])])))

(defn annotare-app []
  (let [panel (subscribe [:get :active-panel])
        modal-info (subscribe [:get :active-modal])
        initial-projects-loading? (subscribe [:get :loading? :initial-projects])
        initial-tagsets-loading? (subscribe [:get :loading? :initial-tagsets])]
    (fn []
      (if (and @initial-projects-loading? @initial-tagsets-loading?)
        [:section.hero.is-fullheight
          [:div.hero-header]
          [:div.hero-content>div.container
            [:div.loading-spinner]
            [:h1.title "Just a second"]
            [:h2.subtitle "Loading data..."]]]
        [:div
          (when-let [mtype (:type @modal-info)]
            (case mtype
              :delete [delete-modal (dissoc @modal-info :type)]
              :tag-help [tagging-help-modal (dissoc @modal-info :type)]
              nil))
          [navbar @panel]
          (case @panel
            :front [front-panel]
            :admin [admin-panel]
            :tag   [tagging-panel])]))))
