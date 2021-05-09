(ns twains.handler
  (:require [compojure.core :refer [defroutes context GET POST]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.util.response :refer [response]]
            [twains.db :as db]
            [twains.session :as session]
            [twains.shares :as shares]))


(defn unauthorized [body]
  {:status 401
    :headers {}
    :body body})

(defn auth? [handler]
  (fn [request]
    (if-let [user (session/valid-user? request)]
      (handler (assoc request :user-id user))
      (unauthorized "No valid token found"))))

(defn get-auth [username password req]
  ;; get from form params or from json body
  (let [username (or username (get-in req [:body "username"]))
        password (or password (get-in req [:body "password"]))]

    (if (and (= username "username") (= password "password"))
      (response {:token (session/token username)})
      (unauthorized "Forgot ur password?"))))

(defn get-quotes [user]
  (response {:user user :quotes db/quotes-db}))

(defn get-quote [quote-id]
  (if-let [quote (db/get-quote-by-id quote-id)]
    (response quote)
    (route/not-found (str "Quote " quote-id " Not Found"))))

(defn create-share [quote-id]
  (if-let [quote (db/get-quote-by-id quote-id)]
    (response {:share_url (str "share/" (shares/create-share (get quote "id")))})
    (route/not-found (str "Quote " quote-id " Not Found"))))

(defn get-share [share-id]
  (if-let [quote-id (shares/get-share share-id)]
    (get-quote quote-id)
    (route/not-found (str "Share " share-id " Not Found"))))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (POST "/auth" [username password :as req] (get-auth username password req))
  (GET "/share/:share-id" [share-id] (get-share share-id))
  (auth?
   (context "/quotes" [:as req]
    (GET "/" [] (get-quotes (:user-id req)))
    (GET "/:quote-id" [quote-id] (get-quote quote-id))
    (GET "/:quote-id/share" [quote-id] (create-share quote-id))))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      wrap-json-body
      wrap-json-response
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))))
