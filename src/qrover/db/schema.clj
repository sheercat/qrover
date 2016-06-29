(ns qrover.db.schema
  (:use
   [qrover.config :as config]
   [taoensso.timbre :as logger]
   )
  (:import com.mchange.v2.c3p0.ComboPooledDataSource)
  )

(defn db-spec-master []
  (let [master (:mysql-master config/setting)]
    ;; (logger/debug master)
    master))

(defn db-spec-slave []
  (let [slave (first (shuffle (:mysql-slaves config/setting)))]
    ;; (logger/debug slave)
    slave))

(defn pool
  [spec]
  (let [cpds (doto (ComboPooledDataSource.)
               (.setDriverClass (:classname spec))
               (.setJdbcUrl (str "jdbc:" (:subprotocol spec) ":" (:subname spec)))
               (.setUser (:user spec))
               (.setPassword (:password spec))
               ;; expire excess connections after 30 minutes of inactivity:
               (.setMaxIdleTimeExcessConnections (* 30 60))
               ;; expire connections after 3 hours of inactivity:
               (.setMaxIdleTime (* 3 60 60)))]
    {:datasource cpds}))

(def pooled-db-master (delay (pool (db-spec-master))))
(def pooled-db-slave  (delay (pool (db-spec-slave))))

(defn db-master [] @pooled-db-master)
(defn db-slave  [] @pooled-db-slave)
