(ns annotare.db.queries
  (:require
    [yesql.core :refer [defqueries]]
    [config.core :refer [env]]))

(def conn
  {:classname      "org.sqlite.JDBC"
   :connection-uri (str (:database-url env) "?foreign_keys=ON")
   :naming         {:keys   clojure.string/lower-case
                    :fields clojure.string/lower-case}})

(defqueries "sql/projects.sql" {:connection conn})
(defqueries "sql/documents.sql" {:connection conn})
(defqueries "sql/sentences.sql" {:connection conn})
(defqueries "sql/tagsets.sql" {:connection conn})
