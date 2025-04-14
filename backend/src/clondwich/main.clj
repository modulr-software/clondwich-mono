(ns clondwich.main
  (:require [clojure.data.json :as json]))

(defn parse-json
  "Parses a JSON string into a Clojure data structure."
  [json-str]
  (json/read-str json-str :key-fn keyword))

(parse-json "{\"name\": \"John\", \"age\": 30}")