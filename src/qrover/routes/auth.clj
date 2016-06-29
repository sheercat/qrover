(ns qrover.routes.auth
  (:use compojure.core)
  (:require [qrover.layout :as layout]
            [noir.session :as session]
            [noir.response :as resp]
            [noir.util.route :refer [restricted]]
            [noir.validation :as vali]
            [noir.util.crypt :as crypt]
            [qrover.db.users :as db.users]
            [qrover.util :as util]
            [taoensso.timbre :as logger]
            ))

(defn valid? [pass pass1 email]
  (vali/rule (not (db.users/get-by-email email))
             [:email "sorry duplicated email"])
  (vali/rule (vali/is-email? email)
             [:email "email must be email format"])
  (vali/rule (vali/min-length? pass 5)
             [:pass "password must be at least 5 characters"])
  (vali/rule (= pass pass1)
             [:pass1 "entered passwords do not match"])
  (vali/rule (vali/has-value? email)
             [:email "email is required"])
  (not (vali/errors? :pass :pass1 :email)))

(defn register [& [email]]
  (layout/render
   "registration.html"
   {:validate-errors {:pass (vali/on-error :pass first)
                      :pass1 (vali/on-error :pass1 first)
                      :email (vali/on-error :email first)
                      }
    }))

(defn handle-registration [ pass pass1 email & [admin]]
  (if (valid? pass pass1 email)
    (try
      (let [user (db.users/create-row { :pass (crypt/encrypt pass) :email email :admin (if admin 1 0)})]
        (logger/debug user)
        (session/put! :user-id (first (vals (first user))))
        (util/notice-message "account created and login")
        (resp/redirect "/"))
      (catch Exception ex
        (logger/debug (str (.getMessage ex)))
        (vali/rule false [:email (.getMessage ex)])
        (register)))
    (register email)))

;; (defn profile [] (layout/render "profile.html" ))

(defn handle-login [email pass]
  (let [user (db.users/get-by-email email)]
    (logger/debug user)
    (if (and user (crypt/compare pass (:pass user)))
      (session/put! :user-id (:id user))
      (do
        (util/notice-message "login failer")
        (logger/debug (str "invalid password or email:" email))
      ))
    (resp/redirect "/")))

(defn logout []
  (session/clear!)
  (util/notice-message "logout")
  (resp/redirect "/"))

(defroutes auth-routes
  (GET "/register" [] (restricted (register)))
  (POST "/register" [pass pass1 email admin] (restricted (handle-registration pass pass1 email admin)))
  ;; (GET "/profile" [] (restricted (profile)))
  (POST "/login" [email pass] (handle-login email pass))
  (GET "/logout" [] (restricted (logout)))
  )
