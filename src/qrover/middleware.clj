(ns qrover.middleware
  (:require [taoensso.timbre :as logger]
            [selmer.parser :as parser]
            [qrover.util :as util]
            [qrover.config :as config]
            ;; [environ.core :refer [env]]
            [selmer.middleware :refer [wrap-error-page]]
            [noir-exception.core :refer [wrap-internal-error wrap-exceptions]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            ))

(defn wrap-log-request-and-response
  [handler & [{:as options}]]
  (fn [request]
    (if-not (re-find #"\.(js|css|jpg|png|ico)$" (:uri request))
      (logger/debug (str (format "!!!REQ:%s:%s:" (:request-method request) (:uri request)) (dissoc request :access-rules :uri :request-method :cookies :session))))
    (if-let [response (handler request)]
      (do
        (if-not (re-find #"\.(js|css|jpg|png|ico)$" (:uri request))
                (logger/debug (str (format "!!!RES:%s:%s:%s:" (:request-method request) (:uri request) (:status response)) (dissoc response :body :status))))
        response))))

(defn wrap-notice-prepare
  [handler & [{:as options}]]
  (fn [request]
    (util/notice-message-prepare)
    (if-let [response (handler request)]
      response)))

(defn csrf-custom-token [request] (get-in request [:headers "x-forgery-token"]))

(def csrf-error-response
  {:status 403
   :headers {"Content-Type" "text/html"}
   :body "<h1>anti-forgery</h1>"})


(def first-middleware
  [#(wrap-anti-forgery % {:error-response csrf-error-response })
   #(wrap-notice-prepare % )
   ])

(def development-middleware
  [wrap-log-request-and-response wrap-error-page wrap-exceptions])

(def production-middleware
  [#(wrap-internal-error % :log (fn [e] (logger/error e)))])

(defn load-middleware []
  (concat first-middleware (when (:is_dev config/setting) development-middleware) production-middleware))
