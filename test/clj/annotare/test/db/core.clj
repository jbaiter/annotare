(ns annotare.test.db.core
  (:require [annotare.db.core :as db]
            [annotare.db.migrations :as migrations]
            [clojure.java.io :as io]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [config.core :refer [env]]))

(use-fixtures
  :each
  (fn [f]
    (io/delete-file (last (clojure.string/split (:database-url env) #":")))
    (migrations/migrate ["migrate"])
    (f)))

(def dummy-project {:name "test" :tagset #{"PER" "LOC" "O"}
                    :empty_tag "O" :description "test"})
(def dummy-document {:name "test-doc" :project_id 1})
(def dummy-sent {:tokens ["I" "live" "in" "Berlin"]
                 :tags   ["O" "O"    "O"  "O"]
                 :document_id 1})

(deftest test-projects
  (let [project-stored (db/create-project! dummy-project)
        project-updated (merge project-stored {:name "updated"
                                               :description "updated"
                                               :tagset #{"PER" "LOC" "NONE"}
                                               :empty_tag "NONE"})]
    (is (= (assoc dummy-project :id 1) project-stored))

    (is (= project-stored (first (db/get-projects))))

    (is (= project-updated (db/update-project! project-updated)))

    (is (= project-updated (db/delete-project! 1)))

    (is (nil? (db/get-project 1)))

    (is (thrown? AssertionError
                (db/create-project! (assoc dummy-project :empty_tag "FOO"))))))

(deftest test-documents
  (let [doc-stored (db/create-document! dummy-document)
        doc-updated (merge doc-stored {:name "foo"})]
    (is (= (merge dummy-document {:id 1 :sentence_count 0 :untagged_count 0})
           doc-stored))

    (is (= doc-stored (first (db/get-documents))))

    (is (= doc-stored (first (db/get-project-documents 1))))

    (is (= 1 (count (db/get-project-documents 1))))

    (is (= doc-updated (db/update-document! (assoc doc-updated :project_id 5))))

    (is (= doc-updated (db/delete-document! 1)))

    (is (nil? (db/get-document 1)))))

(deftest test-sentences
  (db/create-project! dummy-project)
  (db/create-document! dummy-document)
  (let [sent-stored (db/create-sentence! dummy-sent)
        sent-updated (merge sent-stored {:tags ["O" "O" "O" "LOC"]
                                         :num_edits 1})]
    (is (= (assoc dummy-sent :id 1 :num_edits 0) sent-stored))

    (is (= sent-stored (first (db/get-sentences))))

    (is (= sent-stored (first (db/get-document-sentences 1))))

    (is (= sent-stored (db/get-random-sentence 1)))

    (is (= 1 (count (db/get-document-sentences 1))))

    (is (= sent-updated (db/update-sentence!
                          (merge sent-updated {:num_edits 50
                                               :tokens ["X" "X" "X" "X"]}))))

    (is (nil? (db/get-random-sentence 1)))

    (is (= sent-updated (db/delete-sentence! 1)))

    (is (nil? (db/get-sentence 1)))

    (is (thrown? AssertionError
                 (db/create-sentence! (assoc dummy-sent :tags ["O" "O" "O"]))))

    (is (thrown? AssertionError
                 (db/create-sentence! (assoc dummy-sent :tags ["O" "O" "O" "FOO"]))))))
