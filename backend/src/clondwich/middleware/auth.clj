(ns clondwich.middleware.auth
  (:require [ring.util.response :refer [response]]
            [clojure.string :as str]
            [clojure.data.json :as json]))

(defn decode-jwt [token]
  ;; Replace with real decoding (e.g. buddy-sign or manually decode)
  ;; For now assume payload is base64 json in middle part
  (try
    (let [[_ payload _] (str/split token #"\.")]
      (-> payload
          (.getBytes "UTF-8")
          java.util.Base64/getDecoder
          .decode
          (String. "UTF-8")
          (json/read-str :key-fn keyword)))
    (catch Exception _ nil)))

(defn wrap-auth [handler]
  (fn [request]
    (let [auth-header (get-in request [:headers "authorization"])
          token (some-> auth-header (clojure.string/replace #"^Bearer " ""))]
      (if-let [decoded (my-jwt/verify-token token)]
        (handler (assoc request :identity decoded))
        {:status 401 :body "Unauthorized"}))))

(defn wrap-admin-auth [handler]
  (wrap-auth
   (fn [request]
     (if (= "admin" (get-in request [:identity :role]))
       (handler request)
       {:status 403 :body "Forbidden - Admins only"}))))

