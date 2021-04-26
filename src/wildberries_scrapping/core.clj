(ns wildberries-scrapping.core
  (:gen-class)
  (:require [clojure.string :refer [split join includes? trim] :as string]
            [lambdaisland.uri :as uri])
  (:import (org.jsoup Jsoup HttpStatusException)
           (java.text SimpleDateFormat)
           (java.util Date)))

(def PROPERTIES_SEPARATOR "\t")

(def GOODS_SEPARATOR "\n")

(defn goods-pages-iterated? [goods] (empty? goods))

(defn all-goods-pages-iterated?
  [goods-pages goods]
  (and (= (count goods-pages) 1) (goods-pages-iterated? goods)))

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

(defn extract-goods-page-data
  [url]
  (try (-> url
           get-html
           select-items
           extract-goods-data)
       (catch HttpStatusException _ '())))

(def INITIAL_PAGE 1)

(defn split-brands [brands-string] (split brands-string #";"))

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

(defn build-goods-url-with-page
  [url page]
  (-> url
      (uri/assoc-query :page page)
      .toString))

(defn extract-all-goods-data
  [urls]
  (loop [urls urls
         page INITIAL_PAGE
         all-goods-data '()]
    (let [url (first urls)
          goods-page-url (build-goods-url-with-page url page)
          goods-page-data (extract-goods-page-data goods-page-url)]

      (cond (all-goods-pages-iterated? urls goods-page-data) all-goods-data
            (goods-pages-iterated? goods-page-data)
              (recur (rest urls)
                     INITIAL_PAGE
                     (concat all-goods-data goods-page-data))
            :else (recur urls
                         (inc page)
                         (concat all-goods-data goods-page-data))))))

(defn build-url-with-single-brand
  [url key brand]
  (.toString (uri/assoc-query url key brand)))

(defn build-urls-with-single-brand
  [url brands-data]
  (map (partial build-url-with-single-brand url (:key brands-data))
    (:brands brands-data)))

(defn split-url-by-brands
  [query-map]
  (cond (:brand query-map) {:key :brand,
                            :brands (split-brands (:brand query-map))}
        (:fbrand query-map) {:key :fbrand,
                             :brands (split-brands (:fbrand query-map))}
        :else {:key "", :brands []}))

;; Разбиение полного адреса на отдельные адреса по каждому бренду необходимо
;; из-за того, что выдача Wildberries ограничена 10 000 результатами.
;; Наличие отдельных адресов для каждого бренда не исключает полностью
;; проблему неучёта товаров, но существенно снижает риск её появления.
;; Бренды с количеством элементов более 10 000 пока не были замечены.
(defn split-into-separate-brands-urls
  [url]
  (let [build-urls-with-single-brand (partial build-urls-with-single-brand url)
        brands-urls (-> url
                        uri/query-map
                        split-url-by-brands
                        build-urls-with-single-brand)]
    (if (not-empty brands-urls) brands-urls [url])))

(defn -main
  [& args]
  (let [map-args (apply hash-map args)
        url (get-url map-args)]
    (if url
      (->> url
           split-into-separate-brands-urls
           extract-all-goods-data
           (cons (stringify-row COLUMNS))
           (join GOODS_SEPARATOR)
           extract-to-file)
      (print-prerequisites))))
