(ns annotare.db
  (:require [cljs.reader]))

; TODO: Create schema for state

;; Initial state
(def default-value
  {:projects (sorted-map)  ;; All available projects
   :documents (sorted-map)
   :active-panel :front    ;; Currently active page/panel
   :active-form nil
   :active-modal nil
   :active-project nil
   :active-document nil
   :active-sentence nil
   :nav-collapsed? true    ;; Is the navigation bar collapsed, only relevant for mobile;
   :loading? false         ;; Are we waiting for data from the API?
   :error nil})            ;; Was there an error that needs to be displayed to the user?
