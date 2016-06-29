(ns qrover.db.types
  (:require [clojure.java.jdbc :as j]
            [qrover.db.schema :refer [db-master db-slave]]
            [qrover.util :as util]
            [taoensso.timbre :as logger]
            ))

(defn create-row [type]
  (let [row-data (assoc type :created_at (util/now) :is_active true )]
    (logger/debug row-data)
    (j/insert! (db-master) :types row-data)))

(defn get-all []
  (j/query (db-slave) ["select * from types"]))

(defn get-by-type [type]
  (first (j/query (db-slave) ["select * from types where type = ?" type ])))

(defn get-by-id [id]
  (first (j/query (db-slave) ["select * from types where id = ?" id ])))

(defn delete [id] (j/delete! (db-master) :types ["id = ?" id]))

;;
