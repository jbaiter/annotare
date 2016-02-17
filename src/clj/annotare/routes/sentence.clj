(ns annotare.routes.sentence
  (:require [annotare.db.core  :as db]
            [compojure.core :refer [defroutes GET PUT DELETE context]]
            [ring.util.http-response :refer [ok]]))

(defroutes sentence-routes
  (context "/sentence/:id" [id]
    (GET "/" [] (ok (db/get-sentence (Long. id))))
    (PUT "/" [& params] (ok (db/update-sentence! (assoc params :id (Long. id)))))
    (DELETE "/" [] (ok (db/delete-sentence! (Long. id))))))
