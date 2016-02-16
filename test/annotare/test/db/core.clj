(ns annotare.test.db.core
  (:require [annotare.db.core :as db]
            [annotare.db.migrations :as migrations]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [config.core :refer [env]]))

(use-fixtures
  :each
  (fn [f]
    (migrations/migrate ["rollback"])
    (migrations/migrate ["migrate"])
    (f)))

(def dummy-project {:name "test" :tagset #{"PER" "LOC" "O"}
                    :empty_tag "O" :description "test"})
(def dummy-document {:name "test-doc" :project_id 1})
(def dummy-sent {:tokens ["I" "live" "in" "Berlin"]
                 :tags   ["O" "O"    "O"  "O"]
                 :document_id 1})

(deftest test-projects
  (let [project-stored (db/create-project! dummy-project)]
    (is (= (assoc dummy-project :id 1) project-stored))

    (is (= project-stored (first (db/get-projects))))

    (is (= "updated"
           (:description
             (db/update-project!
              (assoc project-stored :description "updated")))))

    (is (= (assoc project-stored :description "updated")
           (db/delete-project! 1)))

    (is (nil? (db/get-project 1)))

    (is (thrown? AssertionError
                (db/create-project! (assoc dummy-project :empty_tag "FOO"))))))

(deftest test-documents
  (let [doc-stored (db/create-document! dummy-document)]
    (is (= (assoc dummy-document :id 1) doc-stored))

    (is (= doc-stored (first (db/get-documents))))

    (is (= doc-stored (first (db/get-project-documents 1))))

    (is (= 1 (count (db/get-project-documents 1))))

    (is (= "foo" (:name (db/update-document!
                          (assoc doc-stored :name "foo")))))

    (is (= (assoc doc-stored :name "foo") (db/delete-document! 1)))

    (is (nil? (db/get-document 1)))))

(deftest test-sentences
  (db/create-project! dummy-project)
  (db/create-document! dummy-document)
  (let [sent-stored (db/create-sentence! dummy-sent)]
    (is (= (assoc dummy-sent :id 1) sent-stored))

    (is (= sent-stored (first (db/get-sentences))))

    (is (= sent-stored (first (db/get-document-sentences 1))))

    (is (= 1 (count (db/get-document-sentences 1))))

    (is (= "LOC" (last (:tags (db/update-sentence!
                                (assoc sent-stored :tags ["O" "O" "O" "LOC"]))))))

    (is (= (assoc sent-stored :tags ["O" "O" "O" "LOC"])
           (db/delete-sentence! 1)))

    (is (nil? (db/get-sentence 1)))

    (is (thrown? AssertionError
                 (db/create-sentence! (assoc dummy-sent :tags ["O" "O" "O"]))))

    (is (thrown? AssertionError
                 (db/create-sentence! (assoc dummy-sent :tags ["O" "O" "O" "FOO"]))))))
