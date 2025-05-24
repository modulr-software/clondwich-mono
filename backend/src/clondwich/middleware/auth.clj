(ns clondwich.middleware.auth
  (:require [ring.util.response :refer [response status]]
            [clojure.string :as str]
            [clojure.data.json :as json]
            [clondwich.jwt :as jwt]))

;; Authentication middleware
(defn wrap-auth [handler]
  (fn [req]
    (let [token (some-> (get-in req [:headers "authorization"])
                        (str/replace #"^Bearer " ""))
          user (jwt/verify-token token)]
      (if user
        (handler (assoc req :user user)) ; attach whole user map
        (-> (response {:error "Unauthorized"})
            (status 401))))))


