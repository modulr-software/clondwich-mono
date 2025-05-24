(ns clondwich.environment
  (:require [aero.core :refer [read-config]]))

(defn read [& keys]
  (let [vars (read-config "config.edn")]
    (get-in vars (vec keys))))

(comment
  (read :port)
  (read :db)
  (read :secret)
  )
