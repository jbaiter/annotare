(ns annotare.routes.project
  (:require [annotare.db.core  :as db]
            [compojure.core :refer [defroutes GET POST PUT DELETE context]]
            [ring.util.http-response :refer [ok]]))

(defn create-project [{:keys [params]}]
  (ok (db/create-project! (update params :tagset set))))

(defn update-project [id params]
  (ok (db/update-project! (-> params
                            (assoc :id id)
                            (update :tagset set)))))

(defn create-document [project-id {:keys [params]}]
  (ok (db/create-document! (assoc params :project_id project-id))))

(defroutes project-routes
  (context "/project" []
    (POST   "/"     req             (create-project req))
    (GET    "/"     []              (db/get-projects))
    (GET    "/:id"  [id]            (ok (db/get-project (Long. id))))
    (PUT    "/:id"  [id & params]   (update-project (Long. id) params))
    (DELETE "/:id"  [id]            (ok (db/delete-project! (Long. id))))
    (GET "/:id/random-untagged" [id] (ok (db/get-random-sentence (Long. id))))
    (context "/:project-id/documents" [project-id]
      (POST "/" req (create-document (Long. project-id) req))
      (GET  "/" [] (ok (db/get-project-documents (Long. project-id)))))))
