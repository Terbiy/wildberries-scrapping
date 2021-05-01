(ns wildberries-scrapping.database-filler
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [clojure.string :refer [join]]

            [wildberries-scrapping.database-configuration :refer
             [connection-settings insertion-settings]])

  (:import (com.microsoft.sqlserver.jdbc SQLServerException)))

(defn get-config-instruction
  [missing-field]
  (format
    "Необходимо задать его в поле %s в конфигурации connection-settings в файле src/wildberries_scrapping/database_credentials.clj."
    missing-field))

(defn throw-if-connection-setting-missing
  [credential messages]
  (if (empty? (get connection-settings credential))
    (throw (Exception. (join " " messages)))))

(defn get-settings
  []
  (throw-if-connection-setting-missing
    :user
    ["Для доступа к базе не хватает имени пользователя."
     (get-config-instruction :user)])
  (throw-if-connection-setting-missing :password
                                       ["Для доступа к базе не хватает пароля."
                                        (get-config-instruction :password)])
  (throw-if-connection-setting-missing
    :dbname
    ["Для доступа к базе необходимо её название."
     (get-config-instruction :dbname)])
  (merge {:dbtype "mssql"} connection-settings))

(def mssql-connection (atom nil))

(defn connect-to-database!
  []
  (reset! mssql-connection (jdbc/get-datasource (get-settings))))

(def TOO_LONG_STRING_SQL_EXCEPTION_CODE 2628)

(defn save-to-database!
  [columns data]
  (let [max-manageable-batch-size (dec (quot (:values-for-single-insert-limit
                                               insertion-settings)
                                             (count columns)))]
    (println)
    (loop [data data]
      (let [[batch left] (split-at max-manageable-batch-size data)]
        (println (format "Начинаю выгрузку в таблицу записей в количестве %d."
                         (count batch)))
        (try
          (sql/insert-multi! @mssql-connection
                             (:table-name insertion-settings)
                             columns
                             batch)
          (catch SQLServerException exception
            (if (= TOO_LONG_STRING_SQL_EXCEPTION_CODE
                   (.getErrorNumber (.getSQLServerError exception)))
              (do
                (println (.getErrorMessage (.getSQLServerError exception)))
                (println
                  "Из-за возникшей ошибки текущий набор записей будет пропущен."))
              (throw exception))))
        (when-not (empty? left) (recur left))))))
