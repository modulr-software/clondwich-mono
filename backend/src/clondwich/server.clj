(ns clondwich.server
  (:require [org.httpkit.server :as http]
            [clojure.data.json :as json]
            [clondwich.routes :as routes]))

(defonce ^:private server (atom nil))

(defn handler [req]

  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Merve and KAV are the real Gaylords"})

(defn mw [req]
  (let [parsed-body (-> (:body req)
                        slurp
                        (json/read-str :key-fn keyword))
        response    (handler (assoc req :body parsed-body))]
    (assoc response
           :body (json/write-str (:body response))
           :headers {"Content-Type" "application/json"})))

(defn wrap-json [handler]
  (fn [req]
    (let [parsed-body (try
                        (when-let [body (:body req)]
                          (json/read-str (slurp body) :key-fn keyword))
                        (catch Exception _ nil))
          request-with-body (if parsed-body
                              (assoc req :body parsed-body)
                              req)
          response (handler request-with-body)]
      (assoc response
             :body (json/write-str (:body response))
             :headers {"Content-Type" "application/json"}))))

(defn start []
  (when (nil? @server)

    (reset! server (http/run-server (wrap-json routes/app-routes) {:port 8080}))))

(defn stop []
  (when-not (nil? @server)

    (@server)
    (reset! server nil)))

(defn restart []
  (stop)
  (start))

(comment
  (start)
  (stop)
  (restart)

  (let [stopTest (start)]
    (println @server)
    (stopTest)))

;;@server

;; (json/read-str (slurp (:body req) :key-fn keyword))
;;(def test-str "{\"name\":\"taku\"}")
;;(def pm (json/read-str test-str :key-fn keyword))
;;(reset! server (http/run-server (wrap-json handler) {:port 8080}))))
