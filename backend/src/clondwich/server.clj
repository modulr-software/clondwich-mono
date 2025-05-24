(ns clondwich.server
  (:require [org.httpkit.server :as http]
            [clondwich.routes :as routes]
            [clondwich.environment :as env]
            [clondwich.middleware.interface :as mw]))

(defonce ^:private server (atom nil))

(defn handler [req]

  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Merve and KAV are the real Gaylords"})


(defn start []
  (when (nil? @server)
    (reset! server 
            (http/run-server (mw/apply-generic routes/app-routes) 
                             {:port (env/read :port)}))))


(defn running? []
  (some? @server))


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
