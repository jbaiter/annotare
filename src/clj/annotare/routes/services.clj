(ns annotare.routes.services
 (:require [ring.util.http-response :refer :all]
           [config.core :refer [env]]
           [compojure.api.sweet :refer :all]
           [compojure.api.upload :refer [TempFileUpload]]
           [ring.middleware.multipart-params :refer [wrap-multipart-params]]
           [schema.core :as s]
           annotare.auth.restructure
           [annotare.util :as util]
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
          :return       Project
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
          :authenticated
          (ok (db/update-project! (assoc project :id id))))

        (DELETE "/" []
          :return       Project
          :summary      "Delete a project"
          :authenticated
          (ok (db/delete-project! id)))

        (GET "/export" []
          (ok (util/make-bio (db/get-tagged-sentences id))))

        (POST "/documents" []
          :return       Document
          :body         [doc Document]
          :summary      "Create a new document in the project"
          :authenticated
          (ok (db/create-document! (assoc doc :project_id id))))

        (GET "/documents" []
          :return       [Document]
          :summary      "Retrieve all documents in a project"
          (ok (db/get-project-documents id)))

        (POST "/documents/import/:fmt" []
          :return       Document
          :path-params  [fmt :- (s/enum :txt :tcf :bio)]
          :multipart-params [file :- TempFileUpload]
          :middleware [wrap-multipart-params]
          :summary      "Upload a file and import all sentences contained in it
                         into a new document"
          :authenticated
          (let [{:keys [empty-tag]} (db/get-project-tagset id)
                doc                 (db/create-document! {:project_id id
                                                          :name (:filename file)})
                parser (fmt parsers)
                num-sents (->> file
                               :tempfile
                               (parser empty-tag)
                               (filter not-empty)
                               (map #(assoc % :document_id (:id doc)))
                               db/create-sentences!)]
              (ok (db/get-document (:id doc)))))))

    (context "/tagset" []
      (GET "/" []
        :return   [Tagset]
        :summary  "Retrieve all tagsets"
        (ok (db/get-tagsets)))

      (POST "/" []
        :return   Tagset
        :body     [tagset Tagset]
        :summary  "Create a new tagset"
        :authenticated
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
          :authenticated
          (ok (db/update-tagset! (assoc tagset :id id))))

        (DELETE "/" []
          :return   Tagset
          :summary  "Delete a tagset"
          :authenticated
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
        :authenticated
        (ok (db/delete-document! id)))

      (POST "/sentences" []
        :return       Sentence
        :body         [sent Sentence]
        :summary      "Create a new sentence in the document"
        :authenticated
        (ok (db/create-sentence! (assoc sent :document_id id))))


      (GET "/sentences" []
        :return       [Sentence]
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
        :return       Sentence
        :summary      "Delete a sentence"
        :authenticated
        (ok (db/delete-sentence! id))))))

