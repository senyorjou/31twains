(ns twains.shares)

(def shares (atom {}))

(defn create-share [quote-id]
  (let [share-token (.toString (java.util.UUID/randomUUID))]
    (swap! shares assoc share-token quote-id)
    share-token))

(defn get-share [share-id]
  (get @shares share-id))
