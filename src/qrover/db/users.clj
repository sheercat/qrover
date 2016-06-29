(ns qrover.db.users
  (:require [clojure.java.jdbc :as j]
            [qrover.db.schema :refer [db-master db-slave]]
            [qrover.util :as util]
            [taoensso.timbre :as logger]
            ))

(defn create-row [user]
  (let [row-data (assoc user :created_at (util/now) :is_active true )]
    (logger/debug row-data)
    (j/insert! (db-master) :users row-data)))

(defn get-by-email [email]
  (first (j/query (db-slave) ["select * from users where email = ? LIMIT 1" email])))

(defn get-by-id [id]
  (first (j/query (db-slave) ["select * from users where id = ? LIMIT 1" id])))

(defn exists-admin-user []
  (first (j/query (db-slave) ["select 1 from users where admin = 1 LIMIT 1"])))

;;
