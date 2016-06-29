(ns qrover.api
  (:require [qrover.handler :as handler]
            [compojure.handler :as ch]
            [compojure.core :refer [routes]]
            [qrover.routes.api :refer [api-routes]]
            [ring.middleware.reload :as reload]
            [org.httpkit.server :as http-kit]
            [qrover.middleware :refer [wrap-log-request-and-response]]
            [ring.middleware.basic-authentication :refer [wrap-basic-authentication]]
            [qrover.config :as config]
            [taoensso.timbre :as logger])
  (:gen-class))

(defn dev? [args] (some #{"-dev"} args))

(defn port [args]
  (if-let [port (first (remove #{"-dev"} args))]
    (Integer/parseInt port) 3001))


(defn authenticated? [name pass]
  (let [basic (:basic-auth (:api config/setting))]
    ;; (logger/debug basic name pass)
    (and (= name (:name basic))
         (= pass (:pass basic)))))

(def api-app
  (-> (routes api-routes)
      (wrap-log-request-and-response)
      (ch/api)
      (wrap-basic-authentication authenticated?)
      ))

(defn -main [& args]
  (handler/init-logger)
  (if (:is_dev config/setting)
    (logger/info "qrover started successfully in the dev mode.")
    (logger/info "qrover started successfully in the production mode."))
  (logger/info (str "start http-kit"))
  (http-kit/run-server
   (if (dev? args) (reload/wrap-reload api-app) api-app) {:port (port args)})
  (logger/info (str  "api server started on port") (port args)))

