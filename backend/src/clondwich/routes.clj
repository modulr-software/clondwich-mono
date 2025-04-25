(ns clondwich.routes
  (:require
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :as route]
   [ring.util.response :refer [response]]
   [ring.util.request :as req]
   [db.core :as db]))

(defroutes app-routes
  ;; Welcome route
  (GET "/" [] (response "Welcome to the Clondwich App!"))

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
    (if (and url (not (clojure.string/blank? url)))
      (do
        (db/insert-image url)
        (response {:status "image-added"}))
      (response {:error "Missing or invalid URL"})))

  ;; Catch-all for undefined routes
  (route/not-found "Not Found"))
