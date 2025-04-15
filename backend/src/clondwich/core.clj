(ns clondwich.core
  (:require [sandwich-vote.server :as server]))

(defn -main []
  (println "Server running on http://localhost:8080")
  (server/start))

