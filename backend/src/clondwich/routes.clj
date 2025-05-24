(ns clondwich.routes
  (:require
   [compojure.core :refer [defroutes GET POST routes]]
   [compojure.route :as route]
   [ring.util.response :as res]
   [clojure.string :as str]
   [clondwich.db :as db]
   [clondwich.jwt :as jwt]
   [clondwich.environment :as env]))


(defroutes app-routes

  ;; Welcome route
  (GET "/" [] {:status 200 :body "henlo" :headers {"Content-Type" "application/json"}})

  ;; Register new user
  (POST "/register" req []
    (try
      (let [{
             username :username
             password :password
             email :email} (:body req)
            user (db/find-user username)]
        (if (and username password (nil? user))
          (do
            (db/create-user! username password email "user")
            (let [user (db/find-user username)
                  payload {:id (:users/id user)
                           :username (:users/username user)
                           :role (:users/role user)}
                  token (jwt/generate-token payload)]
              (res/response {:status "user-created" :token token})))
          (-> (res/response {:error (if (some? user) 
                                      (str "User with the username " username " already exists.") 
                                      "Missing username or password.")})
              (res/status 400))))
      (catch Exception e
        (res/response {:error (.getMessage e)}))))

;; Login
  (POST "/login" req []
    (try
      (println (env/read :db))
      (let [{username :username 
             password :password} (:body req) 
            user (db/find-user username)]
        (if (and user (db/check-password password (:users/password user)))
          (let [payload {:id (:users/id user)
                         :username (:users/username user)
                         :role (:users/role user)} ; include more fields if needed
                token (jwt/generate-token payload)]
            (res/response {:token token}))
          (-> (res/response {:error "Invalid credentials"})
              (res/status 401))))
      (catch Exception e
        (-> (res/response {:error (str "An error occurred: " e)})
            (res/status 500))))))

(comment 
  (db/find-user "toast"))
