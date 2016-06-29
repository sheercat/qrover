(ns qrover.socket
  (:require
   [qrover.handler :as handler]
   [ring.middleware.reload :as reload]
   [org.httpkit.server :as http-kit]
   [taoensso.timbre :as logger])
  (:gen-class))

(defn wshandler [request]
  (http-kit/with-channel request channel
    (http-kit/on-close channel (fn [status] (println "client close it" status)))
    (http-kit/on-receive channel (fn [data] ;; echo it back
                                   (http-kit/send! channel data)))))

(defn dev? [args] (some #{"-dev"} args))

(defn port [args]
  (if-let [port (first (remove #{"-dev"} args))]
    (Integer/parseInt port)
    3000))

(defn -main [& args]
  (handler/init)
  (http-kit/run-server
   (if (dev? args) (reload/wrap-reload wshandler) wshandler)
   {:port (port args)})
  (logger/info "server started on port"))
