(ns annotare.core
  (:require-macros [secretary.core :refer [defroute]])
  (:require [goog.events :as events]
            [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [secretary.core :as secretary]
            [devtools.core :as devtools]
            [annotare.handlers]
            [annotare.subs]
            [annotare.views.app :refer [annotare-app]])
  (:import [goog History]
           [goog.history EventType]))

(enable-console-print!)
(devtools/enable-feature! :sanity-hints  :dirac)
(devtools/install!)

(defroute "/" []
  (dispatch [:set-panel :front]))
(defroute "/admin" []
  (dispatch [:set-panel :admin]))
(defroute "/tag/:project-id" [project-id]
  (let [project-id (js/parseInt project-id)]
    (dispatch [:set-active-project project-id])
    (dispatch [:fetch-random-sentence])
    (dispatch [:set-panel :tag])))

(def history
  (doto (History.)
    (events/listen EventType.NAVIGATE
                   (fn [event] (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn ^:export main
  []
  (dispatch-sync [:initialise-db])
  (dispatch [:fetch-projects])
  (reagent/render [annotare-app]
                  (.getElementById js/document "app")))
