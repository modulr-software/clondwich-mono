(ns db.core
  (:require [next.jdbc :as jdbc]))

(def db-spec {:dbtype "sqlite"
              :dbname "clond.db"})

(def datasource (jdbc/get-datasource db-spec))

(defn create-users-table [datasource]
  (jdbc/execute! datasource
                 ["CREATE TABLE IF NOT EXISTS users(id INTEGER PRIMARY KEY, email TEXT, username TEXT,password TEXT)"]))

(defn drop-users-table [datasource]
  (jdbc/execute! datasource
                 ["DROP TABLE IF EXISTS users"]))

(defn create-images-table [datasource]
  (jdbc/execute! datasource
                 ["CREATE TABLE IF NOT EXISTS images(id INTEGER PRIMARY KEY, url TEXT)"]))

(defn drop-images-table [datasource]
  (jdbc/execute! datasource
                 ["DROP TABLE IF EXISTS images"]))

(defn insert-image [url] 
  (jdbc/execute! datasource
                 ["INSERT INTO images (url) VALUES (?)" url]))

(defn create-votes-table [datasource]
  (jdbc/execute! datasource
                 ["CREATE TABLE IF NOT EXISTS votes(id INTEGER PRIMARY KEY, image_id INTEGER, vote BOOLEAN DEFAULT 1)"]))

(defn insert-vote [datasource image_id vote]
  (jdbc/with-db-transaction [image_id vote datasource] 
    (jdbc/execute! datasource
                   ["INSERT INTO votes (image_id, vote) VALUES (?, ?)" image_id vote])
    
    (jdbc/execute! datasource
                   ["UPDATE images SET total_votes = total_votes + 1 WHERE id = ?" image_id])))


(defn drop-votes-table [datasource]
  (jdbc/execute! datasource
                 ["DROP TABLE IF EXISTS votes"]))

(defn get-images-by-vote-count [datasource]
  (jdbc/execute! datasource
                 ["SELECT images.id, images.url, COUNT(votes.image_id) AS vote_count
                   FROM images
                   LEFT JOIN votes ON images.id = votes.image_id
                   GROUP BY images.id
                   ORDER BY vote_count ASC"]))

(comment 
  (create-images-table datasource)
  (create-votes-table datasource)
  (drop-images-table datasource)
  (drop-votes-table datasource)

  ;; Insert and fetch test data:
  (insert-image "http://example.com/sandwich1.jpg")
  (insert-image "http://example.com/not-sandwich.jpg")

  ;; Assuming ID 1 and 2 were created
  (insert-vote datasource 1 true)
  (insert-vote datasource 1 true)
  (insert-vote datasource 2 false)

  ;; Check vote counts
  (get-images-by-vote-count datasource)

  ;; Check DB structure
  (jdbc/execute! datasource
                 ["SELECT * FROM sqlite_master"]))
