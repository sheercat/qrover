(ns qrover.db.domains
  (:require [clojure.java.jdbc :as j]
            [qrover.db.schema :refer [db-master db-slave]]
            [qrover.util :as util]
            [taoensso.timbre :as logger]
            ))

(defn create-row [domain]
  (let [row-data (assoc domain :created_at (util/now) :is_active true )]
    (logger/debug row-data)
    (j/insert! (db-master) :domains row-data)))

(defn get-all []
  (j/query (db-slave) ["select * from domains"]))

(defn get-by-id [id]
  (first (j/query (db-slave) ["select * from domains where id = ?" id])))

(defn delete [id] (j/delete! (db-master) :domains ["id = ?" id]))

;;
