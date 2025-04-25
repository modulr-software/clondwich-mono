(ns clondwich.core
  (:require [sandwich-vote.server :as server]))


(defn -main []
  
  (server/start))

