(ns annotare.db
  (:require [cljs.reader]))

; TODO: Create schema for state

;; Initial state
(def default-value
  {:projects {}  ;; All available projects
   :documents {}
   :tagsets {}
   :sentence-queue cljs.core/PersistentQueue.EMPTY
   :active-panel :front    ;; Currently active page/panel
   :active-tagset nil
   :active-modal nil
   :active-project nil
   :active-document nil
   :active-sentence nil
   :nav-collapsed? true    ;; Is the navigation bar collapsed, only relevant for mobile;
   :loading? {}            ;; For which API calls are we waiting to return?
   :error nil              ;; Was there an error that needs to be displayed to the user?
   :upload {:type "tcf"
            :files nil}})
