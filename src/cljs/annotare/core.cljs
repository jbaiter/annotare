(ns annotare.core
  (:require [reagent.core :as r]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [annotare.tagging :as tag])
  (:import goog.History))

(def projects (r/atom {}))

(GET "/project"
     {:headers {"Accept" "application/transit+json"}
      :handler (fn [prjs]
                 (reset! projects
                         (reduce #(assoc %1 (:id %2) %2) {} prjs))
                 (.log js/console (str @projects)))})

(defn nav-link [uri title page collapsed?]
  [:span.header-item
   [:a
    {:class (when (= page (session/get :page)) "is-active")
      :href uri
      :on-click #(reset! collapsed? true)}
    title]])

(defn navbar []
  (let [collapsed? (r/atom true)]
    (fn []
      [:header.header
       [:div.container
        [:div.header-left
         [:a.header-item {:href "#/"} "annotare"]]
        [:span.header-toggle
          {:on-click #(swap! collapsed? not)}
          [:span] [:span] [:span]]
        [:div.header-right.header-menu
         {:class (when-not @collapsed? "is-active")}
         [nav-link "#/" "Home" :home collapsed?]
         [nav-link "#/admin" "Admin" :admin collapsed?]]]])))

(defn home-page []
  [:div.container
   (for [tok ["Angela" "Merkel" "lebt" "in" "Berlin"]]
    ^{:key tok} [tag/tagging-token tok "O" ["O" "PER" "LOC"]])])

(defn admin-page []
  [:div.container
   ; TODO: Create project
   ; TODO: List projects, button to edit/remove
   [:div.container
    [:h1.title "You should be able to delete/update projects, documents and sentences here"]]])

(defn tag-page []
  [:div.container])

(def pages
  {:home #'home-page
   :admin #'admin-page
   :tag #'tag-page})

(defn page []
  [(pages (session/get :page))])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :page :home))

(secretary/defroute "/project/:id" []
  (session/put! :page :tag))

(secretary/defroute "/admin" []
  (session/put! :page :admin))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
        (events/listen
          HistoryEventType/NAVIGATE
          (fn [event]
              (secretary/dispatch! (.-token event))))
        (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-components []
  (r/render [#'navbar] (.getElementById js/document "navbar"))
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-components))
