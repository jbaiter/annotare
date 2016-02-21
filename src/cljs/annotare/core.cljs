(ns annotare.core
  (:require-macros [secretary.core :refer [defroute]])
  (:require [goog.events :as events]
            [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [secretary.core :as secretary]
            [annotare.handlers]
            [annotare.subs]
            [annotare.views.app :refer [annotare-app]])
  (:import [goog History]
           [goog.history EventType]))

(enable-console-print!)

(defroute "/" []
  (dispatch [:set-panel :front]))
(defroute "/admin" []
  (dispatch [:set-panel :admin]))
(defroute "/tag/:project-id" [project-id]
  (let [project-id (js/parseInt project-id)]
    (dispatch [:set-panel :tag project-id])))

(def history
  (doto (History.)
    (events/listen EventType.NAVIGATE
                   (fn [event] (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn ^:export main
  []
  (dispatch-sync [:initialise-db])
  (dispatch [:fetch-projects])
  (dispatch [:fetch-tagsets])
  (reagent/render [annotare-app]
                  (.getElementById js/document "app")))
