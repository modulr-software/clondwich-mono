(ns clondwich.routes
  (:require
   [compojure.core :refer [defroutes GET POST routes]]
   [compojure.route :as route]
   [ring.util.response :refer [response]]
   [clojure.string :as str]
   [db.core :as db]
   [auth.jwt :as jwt])
  (:import [java.util UUID]))

;; Authentication middleware
(defn wrap-auth [handler]
  (fn [req]
    (let [token (some-> (get-in req [:headers "authorization"])
                        (str/replace #"^Bearer " ""))
          user (jwt/verify-token token)]
      (if user
        (handler (assoc req :user user)) ; attach whole user map
        (response {:error "Unauthorized"})))))

(defroutes app-routes

  ;; Welcome route
  (GET "/" [] {:status 200 :body "henlo" :headers {"Content-Type" "application/json"}})

  ;; Register new user
  (POST "/register" [username password email]
    (try
      (if (and username password)
        (do
          (db/create-user! username password email)
          (let [user (db/find-user username)
                payload {:id (:users/id user)
                         :username (:users/username user)
                         :role (:users/role user)}
                token (jwt/generate-token payload)]
            (response {:status "user-created" :token token})))
        (response {:error "Missing username or password"}))
      (catch Exception e
        (response {:error (.getMessage e)}))))

;; Login
  (POST "/login" [username password]
    (try
      (let [user (db/find-user username)]
        (if (and user (db/check-password password (:users/password user)))
          (let [payload {:id (:users/id user)
                         :username (:users/username user)
                         :role (:users/role user)} ; include more fields if needed
                token (jwt/generate-token payload)]
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
