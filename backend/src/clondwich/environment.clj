(ns clondwich.environment
  (:require [aero.core :refer [read-config]]))

(defn read [key]
  (let [vars (read-config "config.edn")]
    (vars key)))

(comment
  (read :port)
  (read :db)
  (read :secret)
  )
