(ns twains.session
  (:require [clojure.string :as string]))

(def user-session (atom {}))

(defn valid-time? [test-time]
  ;; compareTo returns -1, 0, 1 on T1 vs T2
  (let [greater? (.compareTo test-time (java.time.LocalDateTime/now))]
    (> 0 greater?)))

(defn get-auth-from-request [request]
  (if-let [auth (get-in request [:headers "authorization"])]
    (second (string/split auth #" "))))

(defn valid-user? [request]
  (let [token (get-auth-from-request request)
        props (get @user-session token)
        user (:user props)
        uses (:uses props)
        expire-time (:expire-time props)]
    ;; if token exists and is used > 5 times or token is expired
    (if (or (not props) (>= uses 5) (valid-time? expire-time))
      nil
      (do
        (swap! user-session assoc-in [token :uses] (inc (:uses props)))
        user))))

(defn token [user]
  (let [token (.toString (java.util.UUID/randomUUID))
        expire-time (.plusMinutes (java.time.LocalDateTime/now) 1)]
    (swap! user-session assoc token {:user user :uses 0 :expire-time expire-time})
    token))

(defn user []
  (:user @user-session))
