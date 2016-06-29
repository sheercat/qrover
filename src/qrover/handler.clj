(ns qrover.handler
  (:require [compojure.core :refer [defroutes GET]]
            [qrover.middleware :refer [load-middleware]]
            [qrover.config :as config]
            [noir.response :as resp]
            [noir.util.middleware :refer [app-handler]]
            [compojure.route :as route]
            [taoensso.timbre :as logger]
            [taoensso.timbre.appenders.rotor :as rotor]
            [selmer.parser :as parser]
            ;; [environ.core :refer [env]]
            [qrover.routes.home :refer [home-routes]]
            [qrover.routes.service :refer [service-routes]]
            [qrover.routes.auth :refer [auth-routes]]
            ;; [qrover.routes.cljsexample :refer [cljs-routes]]
            [taoensso.carmine.ring :as carmine]
            [noir.session :as session]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [qrover.db.users :as db.users]
            ))

(defroutes app-routes
  (GET "/access-denied" [] {:status 403 :headers {"Content-Type" "text/html"} :body "<h1>access denied</h1>" })
  (GET "/priv-denied" [] {:status 403 :headers {"Content-Type" "text/html"} :body "<h1>only admin</h1>" })
  (route/resources "/")
  (route/not-found "Not Found")
  )

(defn init-logger []
  (logger/set-config!
   [:appenders :rotor]
   {:min-level :info,
    :enabled? true,
    :async? false,
    :max-message-per-msecs nil,
    :fn rotor/appender-fn})
  (logger/set-config!
   [:shared-appender-config :rotor]
   {:path "qrover.log", :max-size (* 512 1024), :backlog 10})
  (if (:is_dev config/setting)
    (logger/set-config! [:appenders :rotor] {:min-level :debug })))

(defn init
  "init will be called once when app is deployed as a servlet on an app server such as Tomcat put any initialization code here"
  []

  (init-logger)

  (if (:is_dev config/setting)
    (do (parser/cache-off!)
        (logger/info "qrover started successfully in the dev mode."))
    (logger/info "qrover started successfully in the production mode."))

  ;; config
  ;; (logger/debug config/setting)

  ;; anti CSRF tag by selmar
  (parser/add-tag! :csrf-token (fn [_ _] (anti-forgery-field)))
  )

(defn destroy
  "destroy will be called when your application shuts down, put any clean up code here"
  []
  (logger/info "qrover is shutting down..."))


(defn admin-access [req]
  (let [user (db.users/get-by-id (session/get :user-id))
        exists_admin (db.users/exists-admin-user) ]
    (if (and user (:admin user)) true (if exists_admin false true))))

(defn user-access [req] (session/get :user-id))

(def app
  (let [handle (app-handler
                [ auth-routes home-routes service-routes app-routes ]
                :middleware (concat [] (load-middleware))
                :session-options {:timeout-response (resp/redirect "/")
                                  :store (carmine/carmine-store (:redis config/setting) (:session-opts config/setting))
                                  :cookie-name (:session-key config/setting)
                                  }
                :access-rules [{:uri "/register"  :rule admin-access :redirect "/priv-denied" }
                               {:uri "/service/*" :rule user-access  :redirect "/access-denied" }
                               {:uri "/service/generate/*" :rule admin-access  :redirect "/access-denied" }
                               {:uri "/service/delete/*" :rule admin-access  :redirect "/access-denied" }
                               ]
                :formats [:json-kw :edn]
                )]
    ;; (logger/debug handle)
    handle
    ))

;;
