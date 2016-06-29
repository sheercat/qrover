(ns qrover.db.emails
  (:require [clojure.java.jdbc :as j]
            ;; [java-jdbc.sql :as s]
            [qrover.db.schema :refer [db-master db-slave]]
            [qrover.util :as util]
            [qrover.db.util :as db.util]
            [taoensso.timbre :as logger]
            [qrover.db.qrcodes :as db.qrcodes]
            [qrover.db.types :as db.types]
            [honeysql.core :as sql]
            [honeysql.helpers :refer :all]
            ))

(defn create-row [rowhash]
  (logger/debug rowhash)
  (let [qrcode (db.qrcodes/get-by-code (:code rowhash)),
        type (db.types/get-by-type (:type rowhash))
        row-data {:created_at (util/now)
                  :qrcodes_id (if qrcode (:id qrcode) nil)
                  :types_id (if type (:id type) nil)
                  :email (:email rowhash)
                  :meta_data (pr-str (select-keys rowhash [:body]))}]
        ;; (logger/debug row-data)
        (j/insert! (db-master) :emails row-data)))

(defn create-bulky [rowhash]
  (let [row-data {:created_at (util/now) :email (:email rowhash) :meta_data (pr-str (select-keys rowhash [:body]))}]
    (logger/debug row-data)
    (j/insert! (db-master) :emails row-data)))

(defn sql-get-list-by []
  (let [sqlgen (-> (select :em.* [:ts.name "type_name"])
                   (from [:emails :em])
                   (order-by [:em.created_at :desc])
                   (limit (sql/param :limit))
                   (offset (sql/param :offset))
                   )
        ]
    ;; (logger/debug sqlgen)
    sqlgen))

(defn get-list-by-qrcode [ qrcodes_id opts ]
  (let [sql (-> (sql-get-list-by)
                (join [:qrcodes :qr] [:= :qr.id :em.qrcodes_id])
                (merge-join [:types   :ts] [:= :ts.id :qr.types_id])
                (where [:= :em.qrcodes_id (sql/param :qrcodes_id)])
                (sql/format {:qrcodes_id qrcodes_id :limit (get opts :limit 0) :offset (get opts :offset 0)} :quoting :mysql)
                )]
    (logger/debug sql)
    (j/query (db-slave) sql)))

(defn get-list-by-type [ types_id opts ]
  (let [sql (-> (sql-get-list-by)
                (join [:types   :ts] [:= :ts.id :em.types_id])
                (where [:= :em.types_id (sql/param :types_id)])
                (sql/format {:types_id types_id :limit (get opts :limit 0) :offset (get opts :offset 0)} :quoting :mysql)
                )]
    (logger/debug sql)
    (j/query (db-slave) sql)))

;; (defn get-list-by-qrcode-old [ qrcodes_id opts ]
;;   (let [sqlstr (str "
;;     SELECT em.*, ts.name type_name FROM emails em
;; INNER JOIN qrcodes qr ON qr.id = em.qrcodes_id
;; INNER JOIN types   ts ON ts.id = qr.types_id
;;      WHERE em.qrcodes_id = ? ORDER BY em.created_at DESC LIMIT " (if opts (:offset opts) 0) ", " (if opts (:limit opts) 20))]
;;     (j/query (db-slave) [sqlstr qrcodes_id])))
;; 
;; (defn get-list-by-type-old [ types_id opts ]
;;   (let [sqlstr (str "
;;     SELECT em.*, ts.name type_name FROM emails em
;; INNER JOIN types ts ON ts.id = em.types_id
;;      WHERE em.types_id = ? ORDER BY em.created_at DESC LIMIT " (if opts (:offset opts) 0) ", " (if opts (:limit opts) 20))]
;;     (j/query (db-slave) [sqlstr types_id])))

(defn sql-get-for-tsv [id]
  (let [sqlgen (-> (select id [:em.email "1x"] [:em.created_at "2x"] [:ts.name "3x"])
                   (from [:emails :em])
                   )
        ]
    ;; (logger/debug sqlgen)
    sqlgen))

(defn get-for-tsv-by-qrcode [ qrcodes_id ]
  (let [sql (-> (sql-get-for-tsv [:qr.id "0x"])
                (join [:qrcodes :qr] [:= :qr.id :em.qrcodes_id])
                (merge-join [:types :ts] [:= :ts.id :qr.types_id])
                (where [:= :em.qrcodes_id (sql/param :qrcodes_id)])
                (sql/format {:qrcodes_id qrcodes_id} :quoting :mysql)
                )]
    (logger/debug sql)
    (j/query (db-slave) sql)))

(defn get-for-tsv-by-type [ types_id ]
  (let [sql (-> (sql-get-for-tsv [:ts.id "0x"])
                (join [:types :ts] [:= :ts.id :em.types_id])
                (where [:= :em.types_id (sql/param :types_id)])
                (sql/format {:types_id types_id} :quoting :mysql)
                )]
    (logger/debug sql)
    (j/query (db-slave) sql)))

;; (defn get-for-tsv-by-qrcode [ qrcodes_id ]
;;   (let [sqlstr (str "
;;     SELECT qr.id 0qrid, em.email 1email, em.created_at 3cat, ts.name 4tname FROM emails em
;; INNER JOIN qrcodes qr ON qr.id = em.qrcodes_id
;; INNER JOIN types   ts ON ts.id = qr.types_id
;;      WHERE em.qrcodes_id = ?")]
;;     (j/query (db-slave) [sqlstr qrcodes_id])))
;; 
;; (defn get-for-tsv-by-type [ types_id ]
;;   (let [sqlstr (str "
;;     SELECT ts.id 0tsid, em.email 1email, em.created_at 3cat, ts.name 4tname FROM emails em
;; INNER JOIN types ts ON ts.id = em.types_id
;;      WHERE em.types_id = ?")]
;;     (j/query (db-slave) [sqlstr types_id])))

(defn get-by-id [id]
  (db.util/read-meta [ "body" ] (first (j/query (db-slave) ["select * from emails where id = ?" id]))))

(defn get-qrcode-by-id [id & [from-master]]
  (if-let [email (first (j/query (if from-master (db-master) (db-slave)) ["select * from emails where id = ? LIMIT 1" id]))]
    (if-let [qrcode (db.qrcodes/get-by-id-with (:qrcodes_id email))]
      qrcode)))

(defn count-by-qrcodes-id [id]
  (if-let [count (j/query (db-master) ["SELECT COUNT(*) count FROM emails WHERE qrcodes_id = ?" id])]
    (:count (first count))
    ))

(defn count-by-types-id [id]
  (if-let [count (j/query (db-master) ["SELECT COUNT(*) count FROM emails WHERE types_id = ?" id])]
    (:count (first count))
    ))

(defn delete [id] (j/delete! (db-master) :emails ["id = ?" id]))

;;
