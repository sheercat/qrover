(ns qrover.layout
  (:require [selmer.parser :as parser]
            [clojure.string :as s]
            [ring.util.response :refer [content-type response]]
            [compojure.response :refer [Renderable]]
            [noir.session :as session]
            [qrover.db.users :as db.users]
            [taoensso.timbre :as logger]
            [qrover.config :as config]
            [qrover.util :as util]
            ))

(def template-path "templates/")

(deftype
    RenderableTemplate
    [template params]
  Renderable
  (render
    [this request]
    (content-type
     (let [user (db.users/get-by-id (session/get :user-id))
           user-id (:id user)
           exists_admin (db.users/exists-admin-user)
           ]
       (->>
        (assoc params
          (keyword (s/replace template #".html" "-selected"))
          "active"
          :servlet-context (if-let [context (:servlet-context request)]
                             (.getContextPath context))
          :user-id user-id
          :user user
          :exists_admin exists_admin
          :notice-message (util/notice-message)
          :is-dev (:is_dev config/setting)
          )
        (parser/render-file (str template-path template))
        response))
       "text/html; charset=utf-8")))

(defn handle-validate-errors [params]
  (if-let [errors (:validate-errors params)]
    (if (not-empty (filter #(not (nil? %)) (vals errors))) ;; (not (nil? x)) same to some? 1.6
      (merge params {:validate-errors (assoc errors :exists-error true)})
      params)
    params))

(defn render [template & [params]]
  (let [merged-params (handle-validate-errors params)]
    (logger/debug template)
    (RenderableTemplate. template merged-params)))
