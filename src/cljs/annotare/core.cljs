(ns annotare.core
  (:require-macros [secretary.core :refer [defroute]])
  (:require [pushy.core :as pushy]
            [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [secretary.core :as secretary]
            [annotare.handlers]
            [annotare.subs]
            [annotare.views.app :refer [annotare-app]])
  (:import [goog History]
           [goog.history EventType]))

(enable-console-print!)
(secretary/set-config! :prefix "/")

(defroute "/" []
  (dispatch [:set-panel :front]))
(defroute "/admin" []
  (dispatch [:set-panel :admin]))
(defroute "/tag/:project-id" [project-id]
  (let [project-id (js/parseInt project-id)]
    (dispatch [:set-panel :tag project-id])))

(def history
  (pushy/pushy secretary/dispatch!
               (fn [x] (when (secretary/locate-route x) x))))

(defn ^:export main
  []
  (pushy/start! history)
  (dispatch-sync [:initialise-db])
  (dispatch [:fetch :project :all])
  (dispatch [:fetch :tagset :all])
  (reagent/render [annotare-app]
                  (.getElementById js/document "app")))
