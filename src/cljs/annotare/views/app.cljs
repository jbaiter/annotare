(ns annotare.views.app
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [subscribe dispatch]]
            [secretary.core :as secretary]
            [pushy.core :as pushy]
            [annotare.util.common :refer [indexed pluralize-kw]]
            [annotare.history :refer [history]]
            [annotare.views.common :refer [icon]]
            [annotare.views.admin :refer [admin-panel]]
            [annotare.views.tagging :refer [tagging-panel]]
            [markdown.core :refer [md->html]]))

(defn nav-link [uri title page-key active-page title]
  "Entry in the navigation bar for parts of the application"
    [:a
      {:class (when (= active-page page-key) "is-active")
       :title title
       :href uri}
      title])

(defn navbar [active-page]
    [:footer.footer>div.container.is-text-centered
      [:a.icon {:title "Home" :href "/"} [icon :home]]
      [:a.icon {:title "Admin" :href "/admin"} [icon :cog]]
      [:a.icon {:on-click #(dispatch [:toggle-modal :tag-help])
                :title "View tag set documentation"}
        [icon :question-circle]]])

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
              [:div.columns
                (for [[idx {:keys [id name description]}] (indexed (vals @projects))]
                  ^{:key idx} [:div.column
                               [:div.card>div.card-content
                                [:div.media>div.media-content>p.title.is-5
                                 [:a {:href (str "/tag/" id)} name]]
                                [:div.content description]]])]])])))


(defn delete-modal [{:keys [object-type object-id]}]
  (let [obj (subscribe [:get (pluralize-kw object-type) object-id])]
    (fn [{:keys [object-type object-id]}]
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
  (let [project (subscribe [:active-project])]
    (fn [{:keys [object-id]}]
      [:div.modal.is-active
       [:div.modal-background]
       (let [{:keys [name documentation]} (:tagset @project)]
        [:div.modal-container>div.modal-content>div.modal-box
          [:h1.title "Guidelines for tagset \"" name "\""]
          [:div.doc-text.content
           {:dangerouslySetInnerHTML {:__html (md->html documentation)}}]
          [:button.modal-close {:on-click #(dispatch [:toggle-modal])}]])])))

(defn annotare-app []
  (let [panel (subscribe [:get :active-panel])
        modal-info (subscribe [:get :active-modal])
        projects (subscribe [:get :projects])]
    (fn []
      (if (nil? @projects)
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
          (case @panel
            :front [front-panel]
            :admin [admin-panel]
            :tag   [tagging-panel])
          [navbar @panel]]))))
