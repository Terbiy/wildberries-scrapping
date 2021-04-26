(ns wildberries-scrapping.core
  (:gen-class)
  (:require [clojure.string :refer [split join includes? trim] :as string]
            [lambdaisland.uri :as uri])
  (:import (org.jsoup Jsoup HttpStatusException)
           (java.text SimpleDateFormat)
           (java.util Date)))

(def ^:dynamic *brand-signature* :brand)

(def PROPERTIES_SEPARATOR "\t")

(def GOODS_SEPARATOR "\n")

(defn brand-iterated? [titles] (empty? titles))

(defn all-brands-iterated?
  [brands titles]
  (and (= (count brands) 1) (brand-iterated? titles)))

(defn try-to-select [select] (try (select) (catch NullPointerException _ "")))

(defn get-property
  [good property]
  (try-to-select #(-> good
                      (.select property)
                      .textNodes
                      first
                      .toString
                      trim)))

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

(def COLUMNS
  ["Бренд" "Артикул" "Наименование" "Размер скидки" "Стоимость со скидкой"
   "Рейтинг" "Количество отзывов"])
(def COLUMNS_PARSERS
  [get-brand get-article get-name get-discount get-price get-rating
   get-comments-number])

(defn stringify-row [data] (join PROPERTIES_SEPARATOR data))

(defn extract-good-data [good] (stringify-row (map #(% good) COLUMNS_PARSERS)))

(defn extract-goods-data [goods] (map extract-good-data goods))

(defn select-items [html] (.select html ".dtList"))

(defn get-html
  [url]
  (-> url
      Jsoup/connect
      .get))

(defn extract-brand-data
  [url]
  (try (-> url
           get-html
           select-items
           extract-goods-data)
       (catch HttpStatusException _ '())))

(defn build-url-with-page
  [url brand page]
  (-> url
      (uri/assoc-query *brand-signature* brand :page page)
      .toString))

(def INITIAL_PAGE 1)

(defn split-brands [brands-string] (split brands-string #";"))

(defn extract-brands [query-map] (get query-map *brand-signature*))

(defn extract-brands-data
  [url]
  (let [url (uri/parse url)
        brands (-> url
                   uri/query-map
                   extract-brands
                   split-brands)]
    (loop [brands brands
           page INITIAL_PAGE
           brands-data '()]
      (let [brand (first brands)
            brand-page-url (build-url-with-page url brand page)
            brand-data (extract-brand-data brand-page-url)]
        (cond
          (all-brands-iterated? brands brand-data) brands-data
          (brand-iterated? brand-data)
            (recur (rest brands) INITIAL_PAGE (concat brands-data brand-data))
          :else (recur brands (inc page) (concat brands-data brand-data)))))))

(defn define-brand-signature
  [url]
  (cond (includes? url "fbrand") :fbrand
        (includes? url "brand") :brand
        :else nil))

(defn get-url [args] (get args "--url"))

(defn print-prerequisites
  []
  (println
    (str
      "Для работы программы необходимо предоставить адрес каталога Wildberries параметром brand или fbrand."
      "\n"
      "Например, https://www.wildberries.ru/catalog/obuv/zhenskaya/sapogi?fbrand=16821;9465;20587;9974;15521;20250;11496;15852;30269.")))

(defn get-date-time-for-file-name
  []
  (.format (SimpleDateFormat. "yyyyMMdd-HHmmss") (Date.)))

(defn extract-to-file
  [data-TSV]
  (let [filename (str "./results/"
                      "wildberries-scrapping-"
                      (get-date-time-for-file-name)
                      ".tsv")]
    (spit filename data-TSV)
    (println (str "Данные о скидках сохранены в файле " filename "."))))

(defn -main
  [& args]
  (let [map-args (apply hash-map args)
        url (get-url map-args)]
    (if url
      (binding [*brand-signature* (define-brand-signature url)]
        (if *brand-signature*
          (->> url
               extract-brands-data
               (cons (stringify-row COLUMNS))
               (join GOODS_SEPARATOR)
               extract-to-file)
          (print-prerequisites)))
      (print-prerequisites))))