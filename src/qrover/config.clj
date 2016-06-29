(ns qrover.config
  (:require [environ.core :refer [env]]
            ;; [ring.middleware file-info file]
            [taoensso.timbre :as logger]
            ;; [noir.io :as io]
            ;; [qrover.util :as util]
            ))


;; (def config-path "config")

(defonce config-common {
                        :config-name "production",
                        :redis {:pool {}
                                :spec { :host "127.0.0.1" :port 6379 :timeout-ms 5000 }},
                        :session-opts { :key-prefix "qrover:session:" :expiration-secs (* 60 60 24 30)},
                        :mysql-korma {:host "localhost"
                                      :port "3306"
                                      :db "qrover"
                                      :delimiters "`"
                                      :user "diver"
                                      :password "hogehogehogehoge12345"}
                        :mysql-master {:classname "com.mysql.jdbc.Driver"
                                       :subprotocol "mysql"
                                       :subname "//localhost:3306/qrover"
                                       ;; :delimiters "`"
                                       :user "diver"
                                       :password "hogehoge110"}
                        :mysql-slaves [{:classname "com.mysql.jdbc.Driver"
                                        :subprotocol "mysql"
                                        :subname "//localhost:3306/qrover"
                                        ;; :delimiters "`"
                                        :user "diver_slave"
                                        :password "hogehoge119"}
                                       ]
                        :mail-setting { :from-default "noreply" }
                        :api { :basic-auth { :name "diver" :pass "hogehoge114" }}
                        })

(defonce config-dev {
                     :is_dev true,
                     :config-name "dev",
                     :redis {:pool {}
                             :spec { :host "127.0.0.1" :port 6379 :timeout-ms 5000 }},
                     :session-opts { :key-prefix "qrover:session:" :expiration-secs (* 60 60 24 30)},
                     :session-key "qrover-session-dev"
                     })

(defonce config-production {
                            :is_dev false,
                            :config-name "production",
                            :redis {:pool {}
                                    :spec { :host "127.0.0.1" :port 6379 :timeout-ms 5000 }},
                            :session-opts { :key-prefix "qrover:session:" :expiration-secs (* 60 60 24 30)},
                            :session-key "qrover-session"
                            })


(defonce setting
  (let [stage (:stage env)
        deploy_env (System/getenv "DEPLOY_ENV")
        ]
    (logger/info (str "!!! env stage is " stage " ") deploy_env)
    (case (if deploy_env deploy_env stage)
      "dev" (merge config-common config-dev)
      "production" (merge config-common config-production)
      (merge config-common config-production))))

;; (def setting setting-delay)

