(ns wildberries-scrapping.data-extractors
  (:require [clojure.string :as string]))

(defn default-if-empty [default value] (if (empty? value) default value))

(def default-integer-string-if-empty (partial default-if-empty "0"))

(defn try-to-select [select] (try (select) (catch NullPointerException _ "")))

(defn get-property
  [good property]
  (try-to-select #(-> good
                      (.select property)
                      .textNodes
                      first
                      .toString
                      string/trim)))

(defn get-comments-number
  [good]
  (-> (get-property good ".dtList-comments-count")
      default-integer-string-if-empty
      Integer/parseInt))

(defn extract-rating-value
  [classes]
  (string/replace (->> classes
                       (filter #(re-matches #"star\d" %))
                       first)
                  #"star"
                  ""))

(defn get-rating
  [good]
  (let [default-float-string-value-if-empty (partial default-if-empty "0.0")]
    (-> (try-to-select #(-> good
                            (.select "[itemprop=\"aggregateRating\"]")
                            .first
                            .classNames
                            extract-rating-value))
        default-float-string-value-if-empty
        Float/parseFloat)))

(defn get-price
  [good]
  (-> (get-property good ".lower-price")
      (string/replace #"(&nbsp;|₽)" "")
      Integer/parseInt))

(defn get-discount
  [good]
  (-> (get-property good ".price-sale")
      (string/replace #"[-%]" "")
      default-integer-string-if-empty
      Integer/parseInt))

(defn get-name [good] (get-property good ".goods-name"))

(defn get-article [good] (.attr good "data-popup-nm-id"))

(defn get-brand [good] (get-property good ".brand-name"))
