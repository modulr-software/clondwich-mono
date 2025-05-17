(ns auth.jwt
  (:require [buddy.sign.jwt :as jwt]
            [buddy.core.keys :as keys]))

(def secret "super-secret-key") 

(defn generate-token [user]
  (jwt/sign {:id (:id user)
             :username (:username user)
             :role (:role user)}
            secret
            {:alg :hs512}))

(defn verify-token [token]
  (try
    (jwt/unsign token secret {:alg :hs512})
    (catch Exception e
      nil)))
