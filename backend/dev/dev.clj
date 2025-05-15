(ns dev 
  (:require [clondwich.server :as server]))

(defn before-ns-unload []
  (println "sup"))

(defn after-ns-reload []
  (println "sup again")
  (server/restart))
(comment
  (server/start)
  (server/restart))

