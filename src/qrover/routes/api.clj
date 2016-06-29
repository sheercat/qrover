(ns qrover.routes.api
  (:require [compojure.core :refer :all]
            [liberator.core :refer [defresource resource]]
            ;; [qrover.layout :as layout]
            ;; [compojure.core :refer [defroutes GET POST]]
            [clojure.string :as cs]
            [qrover.util :as util]
            [noir.session :as session]
            [noir.response :as resp]
            [noir.validation :as vali]
            [taoensso.timbre :as logger]
            [qrover.ex.qrgen :as qr]
            [clojure.data.codec.base64 :as base64]
            [qrover.db.qrcodes :as db.qrcodes]
            [qrover.db.types :as db.types]
            [qrover.db.domains :as db.domains]
            [qrover.db.emails :as db.emails]
            ))

(defn valid? [sender ext cmd mbox from to body]
  (vali/rule (vali/max-length? sender 128) [:sender "email must be at max 128 characters"])
  (vali/rule (vali/is-email? sender)       [:sender "email must be email fmt"])
  (vali/rule (vali/has-value? ext)         [:ext "ext is required"])
  (vali/rule (vali/has-value? cmd)         [:cmd "cmd is required"])
  (not (vali/errors? :sender :ext :cmd)))

;; (defn insert-email-bulky []
;;   (let [types (db.types/get-all) qrcodes (db.qrcodes/get-all)]
;;     (layout/render "api/insert_email_bulky.html" {:types types :qrcodes qrcodes })))

(defn- send-email [sender emails_id]
  (if-let [qrcode (db.emails/get-qrcode-by-id emails_id :from-master)]
    (util/send-email (str "noreply@" (:domain_name qrcode)) sender (:meta-subject qrcode) (:meta-reply qrcode))
    ))

(defn- insert-email-impl
  [params]
  (try
    (if-let [email (db.emails/create-row params)]
      (do  (logger/debug email)
           (send-email (:email params) (first (vals (first email))))
           {:status 200 :headers {"Content-Type" "text/plain"} :body "OK" })
      (logger/debug "unknown db error"))
    (catch Exception ex
      (logger/debug (str "catch exception:" (.getMessage ex)))
      {:status 200 :headers {"Content-Type" "text/plain"} :body (str "EXCEPTION:" (.getMessage ex)) }
      ))
  )

(defn handle-insert-email-bulky [sender ext cmd mbox from to body]
  ;; (logger/debug (str "sender:" sender "ext:" ext "cmd:" cmd "mbox:" mbox "from:" from "to:" to "body:" body))
  (if (valid? sender ext cmd mbox from to body)
    (insert-email-impl {:email sender :code ext :type cmd :body body})
    (do
      (logger/debug "validation error" (vali/get-errors :sender :ext :cmd))
      (db.emails/create-bulky { :email sender, :code ext, :type cmd, :body body })
      {:status 200 :headers {"Content-Type" "text/plain"} :body "VALIDATION ERROR" })
    ))

(defn get-emails [fmt belongs_to id]
  ;; (logger/debug fmt belongs_to id)
  (if-let [rs (case belongs_to
                "qrcodes" (db.emails/get-for-tsv-by-qrcode id)
                "types" (db.emails/get-for-tsv-by-type id)
                )]
    (let [tsv (map (fn [x] (cs/join \tab x)) (map (fn [r] (vals (into (sorted-map) (dissoc r :meta_data)))) rs))]
      (if (> (count tsv) 0)
        (str (cs/join \newline tsv))
        "no data")
      )))

(defresource insert-email-resource
  :allowed-methods [:post]
  :malformed? (fn [context]
                (let [params (get-in context [:request :form-params])]
                  (or (empty? (get params "email"))
                      (empty? (get params "code"))
                      (empty? (get params "type"))
                      (empty? (get params "body")))))
  :handle-malformed "any parameter cannot be empty!"
  :post! (fn [context]
           (let [params (get-in context [:request :form-params])]
             (insert-email-impl (into {} (for [[k v] params] [(keyword k) v])))))
  :handle-created (fn [_] "OK")
  :available-media-types ["text/plain"]
  )

(defresource get-emails-resource
  [fmt belongs-to id]
  :allowed-methods [:get]
  :handle-ok (fn [_] (get-emails fmt belongs-to id))
  :available-media-types ["text/plain"])



(defroutes api-routes
  ;; (GET  "/api/insert_email_bulky" [] (insert-email-bulky))
  ;; (POST "/api/insert_email_bulky" [sender ext cmd mbox from to body] (handle-insert-email-bulky sender ext cmd mbox from to body))
  ;; (GET  "/api/get_emails/:fmt/:belongs_to/:id"  [fmt belongs_to id] (get-emails fmt belongs_to id))

  (ANY  "/" [] {:status 200 :headers { "Content-Type" "text/plain" } :body "hello"})
  (ANY "/api/insert_email" request insert-email-resource)
  (ANY "/api/get_emails/:fmt/:belongs_to/:id"  [fmt belongs_to id] (get-emails-resource fmt belongs_to id))
  )

;;
