(ns twains.db
  (:require [clojure.data.json :refer [read-str]]))

(def quotes-db (read-str (slurp "resources/public/quotes.json")))

(defn get-quote-by-id [id]
  (first (filter #(= (get % "id") id) quotes-db)))

(comment
  (get-quote-by-id "13587c8e")
  )
