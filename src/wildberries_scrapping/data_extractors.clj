(ns wildberries-scrapping.data-extractors
  (:require [clojure.string :as string]))

(defn try-to-select [select] (try (select) (catch NullPointerException _ "")))

(defn get-property
  [good property]
  (try-to-select #(-> good
                      (.select property)
                      .textNodes
                      first
                      .toString
                      string/trim)))

(defn get-comments-number [good] (get-property good ".dtList-comments-count"))

(defn extract-rating-value
  [classes]
  (string/replace (->> classes
                       (filter #(re-matches #"star\d" %))
                       first)
                  #"star"
                  ""))

(defn get-rating
  [good]
  (try-to-select #(-> good
                      (.select "[itemprop=\"aggregateRating\"]")
                      .first
                      .classNames
                      extract-rating-value)))

(defn get-price
  [good]
  (-> (get-property good ".lower-price")
      (string/replace #"(&nbsp;|₽)" "")))

(defn get-discount [good] (get-property good ".price-sale"))

(defn get-name [good] (get-property good ".goods-name"))

(defn get-article [good] (.attr good "data-popup-nm-id"))

(defn get-brand [good] (get-property good ".brand-name"))
