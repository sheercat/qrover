(ns qrover.core
  (:require
   [qrover.handler :as handler]
   [ring.middleware.reload :as reload]
   [org.httpkit.server :as http-kit]
   [taoensso.timbre :as logger]
   ;; [clojure.core.typed :refer :all]
   )
  (:gen-class))


(defn dev? [args] (some #{"-dev"} args))

(defn port [args]
  (if-let [port (first (remove #{"-dev"} args))]
    (Integer/parseInt port)
    3000))

(defn -main [& args]
  (handler/init)
  (logger/info (str "start http-kit"))
  (http-kit/run-server
   (if (dev? args) (reload/wrap-reload handler/app) handler/app)
   {:port (port args)})
  (logger/info (str  "server started on port") (port args)))
