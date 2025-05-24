(ns clondwich.middleware.interface
 (:require [clondwich.middleware.auth :as auth]
           [ring.middleware.json :as ring]))

(defn apply-auth [handler]
  (-> handler 
    (auth/wrap-auth)))

(defn apply-generic [handler]
  (-> handler 
      (ring/wrap-json-response)
      (ring/wrap-json-body {:keywords? true})))

(comment 
   (let [wrapped (apply-generic (fn [request] {:status 200 :body {:some "value"}}))]
    (wrapped {:body {:param "value"}})) 
  )
