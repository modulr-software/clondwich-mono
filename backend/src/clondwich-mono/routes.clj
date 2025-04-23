(ns routes
  (:require
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :as route]
   [ring.util.response :refer [response]]
   [db.core :as db]))


(defroutes app-routes
  (GET "/" [] (response "Welcome to the Sandwich App!"))

  (GET "/images" []
    (response (db/get-images-by-vote-count)))

  (POST "/vote" [image_id vote]
    (db/insert-vote (Integer/parseInt image_id)
                    (= vote "true"))
    (response {:status "vote-recorded"}))

  (route/not-found "Not Found"))