(ns wildberries-scrapping.configuration
  (:require [outpace.config :refer [defconfig]]))

(defconfig user)
(defconfig password)
(defconfig database-name)
(defconfig host)
(defconfig port)

(def connection-settings
  {:user user,
   :password password,
   :dbname database-name,
   :host host,
   :port port})


(defconfig table-name)
(defconfig values-for-single-insert-limit)

(def insertion-settings
  {:table-name table-name,
   :values-for-single-insert-limit values-for-single-insert-limit})