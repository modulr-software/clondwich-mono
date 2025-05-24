(ns clondwich.jwt
  (:require [buddy.sign.jwt :as jwt]
            [buddy.core.keys :as keys]
            [clondwich.environment :as env]))

(defn generate-token [user]
  (jwt/sign {:id (:id user)
             :username (:username user)
             :role (:role user)}
            (env/read :secret)
            {:alg :hs512}))

(defn verify-token [token]
  (try
    (jwt/unsign token (env/read :secret) {:alg :hs512})
    (catch Exception e
      nil)))
