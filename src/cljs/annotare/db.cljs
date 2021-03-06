(ns annotare.db
  (:require [cljs.reader]))

; TODO: Create schema for state

;; Initial state
(def default-value
  {:projects nil      ;; All available projects
   :documents nil     ;; All available documents
   :sentences {}      ;; Sentences that were tagged in this session,
   :sentence-history []  ;; We need to know the order in which the sentences
                         ;; were tagged, so wee keep this list of sentence ids
   :tagsets nil       ;; All available tag sets
   :start-time nil    ;; When has the user started tagging?
   :sentence-queue cljs.core/PersistentQueue.EMPTY  ;; Next sentences to be tagged
   :linking-inputs {}
   :active-panel :front
   :active-tagset nil
   :active-modal nil
   :active-project nil
   :active-document nil
   :active-sentence nil
   :gnd-queries {}
   :show-suggestions {}
   :loading? {}            ;; For which API calls are we waiting to return?
   :error nil              ;; Was there an error that needs to be displayed to the user?
   :upload {:type "tcf"    ;; Information for document importing via file upload
            :files nil}})
