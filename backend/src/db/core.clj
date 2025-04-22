(ns db.core
  (:require [next.jdbc :as jdbc]))

(def db-spec {:dbtype "sqlite"
              :dbname "clond.db"})

(def datasource (jdbc/get-datasource db-spec))

(defn create-images-table []
  (jdbc/execute! datasource
                 ["CREATE TABLE IF NOT EXISTS images(id INTEGER PRIMARY KEY, url TEXT)"]))

(defn drop-images-table []
  (jdbc/execute! datasource
                 ["DROP TABLE IF EXISTS images"]))

(defn insert-image [url] 
  (jdbc/execute! datasource
                 ["INSERT INTO images (url) VALUES (?)" url]))

(defn create-votes-table []
  (jdbc/execute! datasource
                 ["CREATE TABLE IF NOT EXISTS votes(id INTEGER PRIMARY KEY, image_id INTEGER, vote BOOLEAN DEFAULT 1)"]))

(defn insert-vote [image_id vote] 
  (jdbc/execute! datasource
                 ["INSERT INTO votes (image_id, vote) VALUES (?, ?)" image_id vote]))

(defn drop-votes-table []
  (jdbc/execute! datasource
                 ["DROP TABLE IF EXISTS votes"]))

(defn get-images-by-vote-count []
  (jdbc/execute! datasource
                 ["SELECT images.id, images.url, COUNT(votes.image_id) AS vote_count
                   FROM images
                   LEFT JOIN votes ON images.id = votes.image_id
                   GROUP BY images.id
                   ORDER BY vote_count ASC"]))

(comment 
  (create-images-table)
  (create-votes-table)
  (drop-images-table)
  (drop-votes-table)

  ;; Insert and fetch test data:
  (insert-image "http://example.com/sandwich1.jpg")
  (insert-image "http://example.com/not-sandwich.jpg")

  ;; Assuming ID 1 and 2 were created
  (insert-vote 1 true)
  (insert-vote 1 true)
  (insert-vote 2 false)

  ;; Check vote counts
  (get-images-by-vote-count)

  ;; Check DB structure
  (jdbc/execute! datasource
                 ["SELECT * FROM sqlite_master"]))
