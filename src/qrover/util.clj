(ns qrover.util
  (:require [noir.io :as io]
            [clj-time.local :as lt]
            [clojure.java.io :as cjio]
            [clojure.string :as cs]
            [taoensso.timbre :as logger]
            [crypto.random :as random]
            [noir.session :as session]
            [postal.core :as postal]
            [postal.support :as postal.suppor]
            [qrover.config :as config]
            [ring.util.codec :as rc]
            [qrover.ex.qrgen :as qr]
            [clojure.data.codec.base64 :as base64]
            ))

;; (defn md->html
;;   "reads a markdown file from public/md and returns an HTML string"
;;   [filename]
;;   (->>
;;    (io/slurp-resource filename)
;;    (md/md-to-html-string)))

(defn now [& fmt]
  (lt/format-local-time (lt/local-now) :mysql))

(defn load-resource-file [target]
  (load-file (.getFile (cjio/resource target))))

(defn send-email [from to subject body]
  (logger/debug from to subject )
  (postal/send-message {:from from
                        :to to
                        :subject (str (if (:is_dev config/setting) "[dev]" "") subject)
                        :body body
                        ;; :message-id #(postal.support/message-id "foo.bar.dom")
                        })
  )

(defn random-str [n] (cs/lower-case (random/url-part n)))

(defn notice-message
  ([message] (session/put! :notice-message-next message))
  ([]        (let [message (session/get :notice-message)]
               (session/remove! :notice-message)
               message)))

(defn notice-message-prepare []
  (session/put! :notice-message (session/get :notice-message-next))
  (session/remove! :notice-message-next))

(defn- range-size
  ([start size] (range-size start size 1))
  ([start size step]
     {:pre[(integer? size)]}
     (take size (range start (+ start (* size step)) step))))

(defn random-str-handmade
  ([n] (random-str n (mapcat #(apply range-size %) [[48 10] [97 26]])))
  ([n charseq] (apply str (map char (repeatedly n #(rand-nth charseq))))))


(defn url-encode
  [str]
  (rc/url-encode str))

(defn generate-qr
  [str size-vec]
  (base64/encode (qr/as-bytes (qr/from str :size size-vec))))

(defn make-email-scheme
  ([addr sub body]
     (str (format "mailto:%s?subject=%s&body=%s"
                  addr (rc/url-encode sub) (rc/url-encode body))))
  ([addr]
     (str (format "mailto:%s" addr)))
  )
