(ns wildberries-scrapping.database-configuration)

(def connection-settings
  {:user "SA", :password "pFo#ZvM7M#yJBy", :dbname "Wildberries"})

(def insertion-settings
  {;; Название таблицы.
   :table-name "wb_scrapping",
   ;; Ограничение на количество вставляемых за раз элементов.
   ;; Их превышение приводит к ошибке SQL.
   :values-for-single-insert-limit 2100})