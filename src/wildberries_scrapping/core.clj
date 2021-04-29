(ns wildberries-scrapping.core
  (:gen-class)
  (:require [wildberries-scrapping.urls-preparators :refer
             [prepare-urls-for-parsing]]
            [wildberries-scrapping.goods-pages-iterator :refer
             [build-all-goods-data-extractor]]
            [wildberries-scrapping.data-extractors :refer
             [get-brand get-article get-name get-discount get-price get-rating
              get-comments-number]]
            [wildberries-scrapping.file-filler :refer [save-to-file]]))

(def COLUMNS
  '("Бренд"
    "Артикул"
    "Наименование"
    "Размер скидки"
    "Стоимость со скидкой"
    "Рейтинг"
    "Количество отзывов"))
(def COLUMNS_PARSERS
  [get-brand get-article get-name get-discount get-price get-rating
   get-comments-number])

(defn print-prerequisites
  []
  (println
    (str
      "Для работы программы необходимо предоставить адрес каталога Wildberries параметром brand или fbrand."
      "\n"
      "Например, https://www.wildberries.ru/catalog/obuv/zhenskaya/sapogi?fbrand=16821;9465;20587;9974;15521;20250;11496;15852;30269.")))

(defn get-url-arguments [args] (get args "--url"))

(defn -main
  [& args]
  (let [map-args (apply hash-map args)
        url (get-url-arguments map-args)
        extract-all-goods-data (build-all-goods-data-extractor COLUMNS_PARSERS)]
    (if url
      (->> url
           prepare-urls-for-parsing
           extract-all-goods-data
           (cons COLUMNS)
           save-to-file)
      (print-prerequisites))))
