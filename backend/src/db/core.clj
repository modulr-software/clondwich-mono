(ns db.core
  (:require [next.jdbc :as jdbc]))

(def db-spec {:dbtype "sqlite"
              :dbname "clond.db"})

(def datasource (jdbc/get-datasource db-spec))

(defn create-images []
  (jdbc/execute! datasource
                 ["CREATE TABLE IF NOT EXISTS images(id INTEGER PRIMARY KEY, url TEXT)"]))

(defn drop-images []
  (jdbc/execute! datasource
                 ["DROP TABLE IF EXISTS images"]))

(defn insert-image[url]
  (jdbc/execute! datasource
                 ["INSERT INTO images (url) VALUE(?)"url]))

;;stuffs for votes now

(defn create-votes [image_id, vote]
  (jdbc/execute! datasource
                 ["CREATE TABLE IF NOT EXISTS votes(id INTEGER PRIMARY KEY,image_id ,vote BOOLEAN DEFAULT 1)"]))

(defn insert-VOTE [image_id vote]
  (jdbc/execute! datasource
                 ["INSERT INTO votes (image_id, vote) VALUE(?,?)" image_id vote]))

(comment
  (create-images)
  (drop-images)
  (jdbc/execute! datasource
                 ["SELECT * FROM sqlite_master"])
  )
  

