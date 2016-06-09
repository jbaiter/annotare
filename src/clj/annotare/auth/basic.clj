(ns annotare.auth.basic
 (:require [ring.util.http-response :refer [unauthorized forbidden]]
           [clojure.tools.logging :as log]
           [config.core :refer [env]]
           [buddy.auth :refer [authenticated? throw-unauthorized]]
           [buddy.auth.backends.httpbasic :refer [http-basic-backend]]
           [buddy.auth.accessrules :refer [restrict]]
           [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]))

(def authdata
  {:admin (:admin-password env "youshouldreallychangeme")})

(defn auth-fn [req {:keys [username password]}]
  (when-let [user-pass (get authdata (keyword username))]
    (when (= password user-pass)
      (keyword username))))

(def auth-backend (http-basic-backend {:realm "annotare"
                                       :authfn auth-fn}))

(defn error-fn [req val]
  (unauthorized val))

(defn wrap-auth [handler]
  (-> handler
      (restrict {:handler authenticated?
                 :on-error error-fn})
      (wrap-authorization auth-backend)
      (wrap-authentication auth-backend)))
