(ns clondwich.routes
  (:require
   [compojure.core :refer [defroutes GET POST routes]]   ;; ✅ added routes
   [compojure.route :as route]
   [ring.util.response :refer [response]]
   [clojure.string :as str]
   [db.core :as db])
  (:import [java.util UUID]))


;; In-memory session store: token → user-id
(defonce session-store (atom {}))

;; Authentication middleware
(defn wrap-auth [handler]
  (fn [req]
    (let [token (get-in req [:headers "authorization"])
          user-id (@session-store token)]
      (if user-id
        (handler (assoc req :user-id user-id))
        (response {:error "Unauthorized"})))))

(defroutes app-routes

  ;; Welcome route
  (GET "/" [] (response "Welcome to the Clondwich App!"))

  ;; Register new user
  (POST "/register" [username password email]
    (try
      (if (and username password)
        (do
          (db/create-user! username password email)
          (response {:status "user-created"}))
        (response {:error "Missing username or password"}))
      (catch Exception e
        (response {:error (.getMessage e)}))))

  ;; Login
  (POST "/login" [username password]
    (try
      (let [user (db/find-user username)]
        (if (and user (db/check-password password (:users/password user)))
          (let [token (str (UUID/randomUUID))]
            (swap! session-store assoc token (:users/id user))
            (response {:token token}))
          (response {:error "Invalid credentials"})))
      (catch Exception e
        (response {:error "Login failed"}))))

  ;; Authenticated routes
  (wrap-auth
   (routes

    ;; Return images ordered by vote count
    (GET "/images" []
      (response (db/get-images-by-vote-count db/datasource)))

    ;; Cast a vote on an image
    (POST "/vote" [image_id vote]
      (try
        (let [image-id (Integer/parseInt image_id)
              vote-bool (= vote "true")]
          (db/insert-vote db/datasource image-id vote-bool)
          (response {:status "vote-recorded"}))
        (catch Exception e
          (response {:error "Invalid input"}))))

    ;; Insert a new image
    (POST "/image" [url]
      (if (and url (not (str/blank? url)))
        (do
          (db/insert-image url)
          (response {:status "image-added"}))
        (response {:error "Missing or invalid URL"})))))

  ;; Catch-all for undefined routes
  (route/not-found "Not Found"))
