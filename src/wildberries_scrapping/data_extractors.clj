(ns wildberries-scrapping.data-extractors
  (:require [clojure.pprint :refer [pprint]]))

(defn get-comments-number [good] (:feedbacks good))

(defn get-rating [good] (:rating good))

(defn get-price
  [good]
  (let [sale-price (:salePriceU good)]
    (if sale-price
      (/ sale-price 100)
      (do (println "У следующего товара отсутствует цена, вместо неё будет 0:")
          (pprint good)
          0))))

(defn get-discount [good] (or (:sale good) 0))

(defn get-name [good] (:name good))

(defn get-article [good] (:id good))

(defn get-brand [good] (:brand good))
