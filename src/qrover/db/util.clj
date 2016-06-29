(ns qrover.db.util)

(defn read-meta
  [meta-keys row]
  (if row
    (if-let [meta-hash (read-string (:meta_data row))]
      (merge row (apply hash-map (flatten (map (fn [x] (list (keyword (str "meta-" x)) ((keyword x) meta-hash))) meta-keys))))
      row)))

;;
