(ns annotare.schemas
  (:require #?(:cljs [schema.core :as s
                      :include-macros true]
               :clj  [schema.core :as s])))

(s/defschema Project
  {(s/optional-key :id) s/Int
   :name s/Str
   :description s/Str
   :tagset_id s/Int})

(s/defschema Document
  {(s/optional-key :id) s/Int
   :name s/Str
   (s/optional-key :project_id) s/Int
   (s/optional-key :sentence_count) s/Int
   (s/optional-key :untagged_count) s/Int})

(s/defschema Sentence
  {(s/optional-key :id) s/Int
   :tokens [s/Str]
   :tags [s/Str]
   (s/optional-key :document_id) s/Int
   (s/optional-key :num_edits) s/Int})

(s/defschema Tagset
  {(s/optional-key :id) s/Int
   :tags #{s/Str}
   :empty_tag s/Str   ;; TODO: Verify that this is a value in :tags
   :name s/Str
   :documentation (s/maybe s/Str)})
