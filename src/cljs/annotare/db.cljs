(ns annotare.db
  (:require [cljs.reader]))

; TODO: Create schema for state

;; Initial state
(def default-value
  {:projects {}  ;; All available projects
   :documents {}  ;; All available documents
   :sentences (array-map)  ;; Sentences that were tagged in this session,
                           ;; we need to know the insertion order, so we use array-map
   :tagsets {}  ;; All available tag sets
   :start-time nil  ;; When has the user started tagging?
   :sentence-queue cljs.core/PersistentQueue.EMPTY  ;; Next sentences to be tagged
   :active-panel :front
   :active-tagset nil
   :active-modal nil
   :active-project nil
   :active-document nil
   :active-sentence nil
   :loading? {}            ;; For which API calls are we waiting to return?
   :error nil              ;; Was there an error that needs to be displayed to the user?
   :upload {:type "tcf"    ;; Information for document importing via file upload
            :files nil}})
