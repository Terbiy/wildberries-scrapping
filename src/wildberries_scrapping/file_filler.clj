(ns wildberries-scrapping.file-filler
  (:require [clojure.string :refer [join]])

  (:import (java.text SimpleDateFormat)
           (java.util Date)))

(def PROPERTIES_SEPARATOR "\t")

(def GOODS_SEPARATOR "\n")

(def EXCEL_FRIENDLY_DATE_FORMAT "yyyyMMdd-HHmmss")

(defn stringify-row [data] (join PROPERTIES_SEPARATOR data))

(defn get-date-time-for-file-name
  []
  (.format (SimpleDateFormat. EXCEL_FRIENDLY_DATE_FORMAT) (Date.)))

(defn save-to-file!
  [titles data]
  (let [filename (str "./results/"
                      "wildberries-scrapping-"
                      (get-date-time-for-file-name)
                      ".tsv")
        data-TSV (->> data
                      (cons titles)
                      (map stringify-row)
                      (join GOODS_SEPARATOR))]
    (spit filename data-TSV)
    (println (str "\n" "Данные о скидках сохранены в файле " filename "."))))
