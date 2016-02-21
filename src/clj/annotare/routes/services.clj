(ns annotare.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [compojure.api.upload :refer [TempFileUpload]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [schema.core :as s]
            [annotare.db.core :as db]
            [annotare.parsers.core :refer [parsers]]
            [annotare.schemas :refer [Project Document Sentence Tagset]]))

(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version "1.0.0"
                           :title "Annotare API"
                           :description "Services for Annotare"}}}}
  (context "/api" []
    (context "/project" []
      (POST "/" []
        :return       Project
        :body         [project Project]
        :summary      "Create a new project"
        (ok (db/create-project! project)))

      (GET "/" []
        :return       [Project]
        :summary      "Retrieve all projects"
        (ok (db/get-projects)))

      (context "/:id" []
        :path-params  [id :- Long]

        (GET "/" []
          :return       [Project]
          :summary      "Retrieve a specific project"
          (ok (db/get-project id)))

        (GET "/random-untagged" []
          :return       [Sentence]
          :query-params [num :- Long]
          :summary      "Retrieve a number of random, untagged sentence from
                         the project"
          (ok (db/get-random-sentence id num)))

        (PUT "/" []
          :return       Project
          :body         [project Project]
          :summary      "Update a project"
          (ok (db/update-project! (assoc project :id id))))

        (DELETE "/" []
          :return       Project
          :summary      "Delete a project"
          (ok (db/delete-project! id)))

        (POST "/documents" []
          :return       Document
          :body         [doc Document]
          :summary      "Create a new document in the project"
          (ok (db/create-document! (assoc doc :project_id id))))

        (GET "/documents" []
          :return       [Document]
          :summary      "Retrieve all documents in a project"
          (ok (db/get-project-documents id)))))
    (context "/tagset" []
      (GET "/" []
        :return   [Tagset]
        :summary  "Retrieve all tagsets"
        (ok (db/get-tagsets)))

      (POST "/" []
        :return   Tagset
        :body     [tagset Tagset]
        :summary  "Create a new tagset"
        (ok  (db/create-tagset! tagset)))
      (context "/:id" []
        :path-params [id :- Long]

        (GET "/" []
          :return   Tagset
          :summary  "Retrieve a specific tagset"
          (ok (db/get-tagset id)))

        (PUT "/" []
          :return   Tagset
          :body     [tagset Tagset]
          :summary  "Update a tagset"
          (ok (db/update-tagset! (assoc tagset :id id))))

        (DELETE "/" []
          :return   Tagset
          :summary  "Delete a tagset"
          (ok (db/delete-tagset! id)))))

    (context "/document/:id" []
      :path-params  [id :- Long]

      (GET "/" []
        :return       Document
        :summary      "Retrieve a specific document"
        (ok (db/get-document id)))

      (PUT "/" []
        :return       Document
        :body         [doc Document]
        :summary      "Update a document"
        (ok (db/update-document! (assoc doc :id id))))

      (DELETE "/" []
        :return       Document
        :summary      "Delete a document"
        (ok (db/delete-document! id)))

      (POST "/sentences" []
        :return       Sentence
        :body         [sent Sentence]
        :summary      "Create a new sentence in the document"
        (ok (db/create-sentence! (assoc sent :document_id id))))

      (POST "/sentences/import/:fmt" []
        :return       [Sentence]
        :path-params  [fmt :- (s/enum :txt :tcf)]
        :multipart-params [file :- TempFileUpload]
        :middleware [wrap-multipart-params]
        :summary      "Upload a file and import all sentences contained in it
                        into the document"
        (let [empty-tag  (-> id
                              db/get-document
                              :project_id
                              db/get-project
                              :empty_tag)
              parser (fmt parsers)]
          (ok (->> file
                    :tempfile
                    parser
                    (map (fn [tokens]
                          (db/create-sentence!
                            {:tokens tokens
                              :tags (vec (repeat (count tokens) empty-tag))
                              :document_id id})))
                    vec))))

      (GET "/sentences" []
        :return       [Document]
        :summary      "Retrieve all sentences in a document"
        (ok (db/get-document-sentences id))))

    (context "/sentence/:id" []
      :path-params  [id :- Long]
      (GET "/" []
        :return       Sentence
        :summary      "Retrieve a specific sentence"
        (ok (db/get-sentence id)))

      (PUT "/" []
        :return       Sentence
        :body         [sent Sentence]
        :summary      "Update a document"
        (ok (db/update-sentence! (assoc sent :id id))))

      (DELETE "/" []
        :return       Document
        :summary      "Delete a sentence"
        (ok (db/delete-sentence! id))))))

