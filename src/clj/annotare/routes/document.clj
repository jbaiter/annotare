(ns annotare.routes.document
  (:require [annotare.db.core  :as db]
            [compojure.core :refer [defroutes GET POST PUT DELETE context]]
            [ring.util.http-response :refer [ok]]))

(defn create-sentence [doc-id {:keys [params]}]
  (ok (db/create-sentence! (assoc params :document_id doc-id))))

(defroutes document-routes
  (context "/document/:id" [id]
    (GET "/" [] (ok (db/get-document (Long. id))))
    (PUT "/" [& params] (ok (db/update-document! (assoc params :id (Long. id)))))
    (DELETE "/" [] (ok (db/delete-document! (Long. id))))
    (context "/sentences" []
      (GET "/" [] (ok (db/get-document-sentences (Long. id))))
      (POST "/" req (create-sentence (Long. id) req)))))
