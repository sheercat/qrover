(ns qrover.db.qrcodes
  (:require [clojure.java.jdbc :as j]
            [qrover.db.schema :refer [db-master db-slave]]
            [qrover.util :as util]
            [qrover.db.util :as db.util]
            [taoensso.timbre :as logger]
            ;; [clj.qrgen :as qr]
            ))

(defn create-row [qrcode]
  (let [row-data (assoc qrcode
                   :code (util/random-str 10) :created_at (util/now) :is_active true
                   :meta_data (pr-str (select-keys qrcode [:reply :desc :subject])))]
    (logger/debug row-data)
    (j/insert! (db-master) :qrcodes (dissoc row-data :desc :reply :subject))
    ))

;; (defn create-qrcode [qrcode])

(defn get-joined-all []
  (let [rows (j/query (db-slave) ["
    SELECT qr.*, dm.domain domain_name, ts.type type_type, ts.name type_name FROM qrcodes qr
INNER JOIN domains dm ON dm.id = qr.domains_id
INNER JOIN types   ts ON ts.id = qr.types_id
  ORDER BY qr.created_at DESC
"])]
    (if rows (map #(db.util/read-meta ["subject" "reply" "desc"] %) rows))))

(defn get-all []
  (j/query (db-slave) ["select * from qrcodes"]))

(defn get-by-code [code]
  (db.util/read-meta ["subject" "reply" "desc"]
                     (first (j/query (db-slave) ["select * from qrcodes where code = ? LIMIT 1" code ]))))

(defn get-by-id [id & [from-master]]
  (db.util/read-meta ["subject" "reply" "desc"]
                     (first (j/query (if from-master (db-master) (db-slave)) ["select * from qrcodes where id = ? LIMIT 1" id]))))

(defn get-by-id-with [id & [from-master]]
  (db.util/read-meta ["subject" "reply" "desc"]
                     (first (j/query (if from-master (db-master) (db-slave)) ["
    SELECT qr.*, dm.domain domain_name FROM qrcodes qr
INNER JOIN domains dm ON dm.id = qr.domains_id
WHERE qr.id = ? LIMIT 1" id]))))

(defn delete [id] (j/delete! (db-master) :qrcodes ["id = ?" id]))

;;
