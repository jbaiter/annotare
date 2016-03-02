(ns annotare.util.core
  (:import  [goog.net Jsonp])
  (:require [clojure.string :as string]
            [ajax.core :refer [params-to-str]]))

(defn ev->val [ev]
  "Obtain the target's value from the given event."
  (-> ev .-target .-value))

(defn make-load-key [xs]
  "Generate a key for the :loading? map in the app state."
  (string/join "." xs))

(defn jsonp
  "Make a JSONP request"
  [url
   {:keys [params handler error-handler timeout keywordize-keys?]
    :or {keywordize-keys? true}}]
  (let [jsonp (Jsonp. (str url "?" (params-to-str params)) "callback")]
    (.setRequestTimeout jsonp timeout)
    (.send jsonp nil
           (fn success-callback [json]
             (-> json (js->clj :keywordize-keys keywordize-keys?) handler))
           error-handler)))
