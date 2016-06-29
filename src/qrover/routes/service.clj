(ns qrover.routes.service
  (:use compojure.core)
  (:require [qrover.layout :as layout]
            [qrover.util :as util]
            [noir.session :as session]
            [noir.response :as resp]
            [noir.validation :as vali]
            [noir.util.route :refer [restricted]]
            [taoensso.timbre :as logger]
            [clojure.string :as cs]
            [qrover.config :as config]
            [qrover.db.qrcodes :as db.qrcodes]
            [qrover.db.types :as db.types]
            [qrover.db.domains :as db.domains]
            [qrover.db.emails :as db.emails]
            ;; [clj-http.client :as client]
            [org.httpkit.client :as client]
            ))

(defn valid-type? [type name ]
  (vali/rule (not (db.types/get-by-type type)) [:type "duplicated type"])
  (vali/rule (vali/max-length? type 8)         [:type "type must be at max 8 characters"])
  (vali/rule (vali/has-value? type)            [:type "type is required"])
  (vali/rule (vali/has-value? name)            [:name "name is required"])
  (not (vali/errors? :type :name)))

(defn valid-qrcode? [type domain & [reply]]
  (vali/rule (vali/has-value? type)  [:type "type is required"])
  (vali/rule (vali/has-value? domain)[:domain "domain is required"])
  (if reply (vali/rule (vali/max-length? reply 1000)
                       [:reply "reply is must be at max 1000 chars"]))
  (not (vali/errors? :type :reply)))

(defn valid-domain? [name]
  (vali/rule (vali/has-value? name)          [:name "name is required"])
  (vali/rule (< (.indexOf (seq name) \@) 0)  [:name "plz not contains @"])
  (vali/rule (>= (.indexOf (seq name) \.) 0) [:name "plz valid domain"])
  (not (vali/errors? :name)))

(defn generate-qrcode []
  (let [types (db.types/get-all)
        domains (db.domains/get-all)
        ]
    (logger/debug types)
    (layout/render
     "service/generate/qrcode.html"
     {:types types :domains domains
      :validate-errors {:unknown (vali/on-error :unknown first)
                        :type (vali/on-error :type first)
                        :domain (vali/on-error :domain first)
                        :reply (vali/on-error :reply first)}
      })))

(defn handle-generate-qrcode [type domain & [subject reply desc]]
  (if (valid-qrcode? type domain reply)
    (try
      (let [row (db.qrcodes/create-row {:users_id (session/get :user-id) :types_id type :domains_id domain
                                        :subject subject :reply reply :desc desc })]
        (logger/debug row)
        (util/notice-message "generated")
        (resp/redirect (str "/service/qrcode/" (first (vals (first row))))))
      (catch Exception ex
        (logger/error (str "exception on create qrcode") (.getMessage ex))
        (vali/rule false [:unknown (.getMessage ex)])
        (generate-qrcode)))
    (do
      (logger/debug "validate error")
      (generate-qrcode))))

(defn generate-type []
  (layout/render
   "service/generate/type.html"
   {:validate-errors {:unknown (vali/on-error :unknown first)
                      :type (vali/on-error :type first)
                      :name (vali/on-error :name first)}
    }
   ))

(defn handle-generate-type [ type name ]
  (if (valid-type? type name)
    (try
      (let [row (db.types/create-row { :users_id (session/get :user-id) :type (cs/lower-case type) :name name })]
        (logger/debug row)
        (util/notice-message "generated")
        ;; (resp/redirect (str "/service/types/" (first (vals (first row)))))
        (resp/redirect (str "/service/types"))
        )
      (catch Exception ex
        (logger/error (str "exception on create type") (.getMessage ex))
        (vali/rule false [:unknown (.getMessage ex)])
        (generate-type)))
    (do
      (logger/debug "validate error")
      (generate-type))))

(defn generate-domain []
  (layout/render
   "service/generate/domain.html"
   {:validate-errors {:unknown (vali/on-error :unknown first) :name (vali/on-error :name first)}}
   ))

(defn handle-generate-domain [ name ]
  (if (valid-domain? name)
    (try
      (let [row (db.domains/create-row { :users_id (session/get :user-id) :domain name })]
        (logger/debug row)
        (util/notice-message "generated")
        ;; (resp/redirect (str "/service/domains/" (first (vals (first row)))))
        (resp/redirect (str "/service/domains"))
        )
      (catch Exception ex
        (logger/error (str "exception on create domain") (.getMessage ex))
        (vali/rule false [:unknown (.getMessage ex)])
        (generate-domain)))
    (do
      (logger/debug "validate error")
      (generate-domain))))

(defn render-qrcode
  [id & [x y diffy]]
  (let [qrcode  (db.qrcodes/get-by-id id)
        type    (:type   (db.types/get-by-id (:types_id qrcode)))
        domain  (:domain (db.domains/get-by-id (:domains_id qrcode)))
        email   (str type "-" (:code qrcode) "@" domain)
        email1  (util/make-email-scheme email)
        email2  (util/make-email-scheme email "このまま送信して下さい" "このまま送信して下さい")
        sizex   (if x x 100)
        sizey   (if y y 100)
        size-vec [(bigint sizex) (bigint sizey)]
        image_data1 (util/generate-qr email1 size-vec)
        image_data2 (util/generate-qr email2 size-vec)
        ]
    (layout/render
     "service/qrcode.html"
     {:image_data1 (apply str (map char image_data1))
      :image_data2 (apply str (map char image_data2))
      :qrcode qrcode
      :email-count (db.emails/count-by-qrcodes-id id)
      :email email
      :sizes (map (fn [x] {:x (* x 100) :y (* x 100 )}) (range 1 10))
      })))

(defn render-list
  ([target]
     (let [rs (case target
                "qrcodes" (db.qrcodes/get-joined-all)
                "types"   (db.types/get-all)
                "domains" (db.domains/get-all)
                )]
       (layout/render (str "service/" target ".html") {:rs rs})))
  ([target id]
     (logger/debug ":" target id)
     (let [rs (case target
                "qrcodes" [(db.qrcodes/get-by-id id)]
                "types"   [(db.types/get-by-id id)]
                "domains" [(db.domains/get-by-id id)]
                "email"     [(db.emails/get-by-id id)]
                )]
       ;; (logger/debug rs)
       (layout/render (str "service/" target ".html") {:rs rs})))
  ([belongs_to tmpl id]
     ;; (logger/debug belongs_to tmpl id)
     (let [rs (case belongs_to
                "qrcodes"  (db.emails/get-list-by-qrcode id {:offset 0 :limit 10})
                "types"  (db.emails/get-list-by-type id {:offset 0 :limit 10})
                )
           count (case belongs_to
                   "qrcodes"  (db.emails/count-by-qrcodes-id id)
                   "types"  (db.emails/count-by-types-id id)
                   )
           ]
       (layout/render (str "service/" tmpl ".html") {:rs rs :count count :belongs_to belongs_to :belongs_to_id id }))))

(defn delete-any
  [target id]
  (logger/debug target id)
  (case target
    "qrcode" (db.qrcodes/delete id)
    "type"   (db.types/delete id)
    "domain" (db.domains/delete id)
    "email"  (db.emails/delete id)
    )
  (util/notice-message (str target "deleted"))
  (resp/redirect (str "/service/" (if (= "email" target) target (str target "s")))))

(defn get-emails
  [fmt belongs-to id]
  (logger/debug fmt belongs-to id)
  (let [uri (format "http://localhost:3001/api/get_emails/%s/%s/%s" fmt belongs-to id)
        basic (:basic-auth (:api config/setting))]
    (let [res (client/get uri {:basic-auth [(:name basic) (:pass basic)]})]
      (logger/debug @res )
      (if (<= 200 (:status @res) 299)
        {:status 200 :headers { "Content-disposition" (str "attachment; filename=" belongs-to ".tsv"), "Content-Type" "text/tab-separated-values" } :body (:body @res)}
        {:status 200 :headers { "Content-Type" "text/plain" } :body "no data"})
      )))

(defroutes service-routes
  (GET  "/service/generate/qrcode" [] (restricted (generate-qrcode)))
  (POST "/service/generate/qrcode" [type domain subject reply desc] (restricted (handle-generate-qrcode type domain subject reply desc)))

  (GET  "/service/generate/type" [] (restricted (generate-type)))
  (POST "/service/generate/type" [type name] (restricted (handle-generate-type type name)))

  (GET  "/service/generate/domain" [] (restricted (generate-domain)))
  (POST "/service/generate/domain" [name] (restricted (handle-generate-domain name)))

  (GET  ["/service/delete/:target/:id" :target #"type|email|qrcode|domain" :id #"\d+"] [target id] (restricted (delete-any target id)))

  (GET  ["/service/qrcode/:id" :id #"\d+"] [id x y] (restricted (render-qrcode id x y)))

  (GET  ["/service/:target" :target #"qrcodes|types|domains"]
        [target] (restricted (render-list target)))

  (GET  ["/service/:target/:id" :id #"\d+" :target #"email|qrcodes|types|domains"]
        [target id] (render-list target id))

  (GET  ["/service/:belongs_to/emails/:id" :id #"\d+" :belongs_to #"qrcodes|types" ]
        [belongs_to id] (render-list belongs_to "emails" id))

  (GET  ["/service/get_emails/:fmt/:belongs_to/:id" :id #"\d+" :belongs_to #"qrcodes|types"]
        [fmt belongs_to id] (get-emails fmt belongs_to id))

  )
