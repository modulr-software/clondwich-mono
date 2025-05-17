(ns db.core
  (:require
   [next.jdbc :as jdbc]
   [buddy.hashers :as hashers]))

(def db-spec {:dbtype "sqlite"
              :dbname "clond.db"})

(def datasource (jdbc/get-datasource db-spec))

;; --- TABLE CREATION ---
(defn create-users-table [ds]
  (jdbc/execute! ds
                 ["CREATE TABLE IF NOT EXISTS users (
       id INTEGER PRIMARY KEY AUTOINCREMENT,
       email TEXT NULL,
       role TEXT NOT NULL,
       username TEXT NOT NULL UNIQUE,
       password TEXT NOT NULL)"]))

(defn create-images-table [ds]
  (jdbc/execute! ds
                 ["CREATE TABLE IF NOT EXISTS images (
       id INTEGER PRIMARY KEY AUTOINCREMENT,
       url TEXT)"]))

(defn create-votes-table [ds]
  (jdbc/execute! ds
                 ["CREATE TABLE IF NOT EXISTS votes (
       id INTEGER PRIMARY KEY AUTOINCREMENT,
       image_id INTEGER,
       vote BOOLEAN DEFAULT 1)"]))

;; --- TABLE DROPS ---
(defn drop-users-table [ds]
  (jdbc/execute! ds ["DROP TABLE IF EXISTS users"]))

(defn drop-images-table [ds]
  (jdbc/execute! ds ["DROP TABLE IF EXISTS images"]))

(defn drop-votes-table [ds]
  (jdbc/execute! ds ["DROP TABLE IF EXISTS votes"]))

;; --- IMAGE + VOTE OPERATIONS ---
(defn insert-image [url]
  (jdbc/execute! datasource
                 ["INSERT INTO images (url) VALUES (?)" url]))

(defn insert-vote [ds image-id vote]
  (jdbc/execute! ds
                 ["INSERT INTO votes (image_id, vote) VALUES (?, ?)" image-id vote]))

(defn get-images-by-vote-count [ds]
  (jdbc/execute! ds
                 ["SELECT images.id, images.url, COUNT(votes.image_id) AS vote_count
      FROM images
      LEFT JOIN votes ON images.id = votes.image_id
      GROUP BY images.id
      ORDER BY vote_count ASC"]))

;; --- USER AUTHENTICATION ---
(defn create-user! [username password email]
  (let [hashed (hashers/derive password)]
    (jdbc/execute! datasource
                   ["INSERT INTO users (username, password, email) VALUES (?, ?, ?)"
                    username hashed email])))

(defn find-user [username]
  (jdbc/execute-one! datasource
                     ["SELECT * FROM users WHERE username = ?" username]))

(defn check-password [input hashed]
  (hashers/check input hashed))

;; --- TESTING ---
(comment
  (create-users-table datasource)
  (create-images-table datasource)
  (create-votes-table datasource)

  (drop-users-table datasource)
  (drop-images-table datasource)
  (drop-votes-table datasource)

  (create-user! "admin" "password123" "admin@example.com")
  (find-user "admin")
  (check-password "password123" (:password (find-user "admin")))

  (insert-image "http://example.com/sandwich1.jpg")
  (insert-vote datasource 1 true)
  (get-images-by-vote-count datasource))
