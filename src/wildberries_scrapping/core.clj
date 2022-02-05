(ns wildberries-scrapping.core
  (:gen-class)
  (:require [clojure.tools.cli :as cli]

            [wildberries-scrapping.urls-preparators :refer
             [prepare-urls-for-parsing]]
            [wildberries-scrapping.goods-pages-iterator :refer
             [build-all-goods-data-extractor]]
            [wildberries-scrapping.data-extractors :refer
             [get-brand get-article get-name get-discount get-price get-rating
              get-comments-number get-stocks get-promo get-new]]
            [wildberries-scrapping.file-filler :refer [save-to-file!]]
            [wildberries-scrapping.database-filler :refer
             [connect-to-database! save-to-database!]]))

(def COLUMNS_TITLES
  '("Бренд"
    "Артикул"
    "Наименование"
    "Размер скидки"
    "Стоимость со скидкой"
    "Рейтинг"
    "Количество отзывов"
    "Остаток"
    "Промо"
    "Новинка"))
(def COLUMNS
  '(:brand :article
           :name :discount
           :price :rating
           :comments_number :stocks
           :promo :new))
(def COLUMNS_PARSERS
  [get-brand get-article get-name get-discount get-price get-rating
   get-comments-number get-stocks get-promo get-new])

(defn print-prerequisites
  []
  (println
    (str
      "Для работы программы необходимо предоставить адрес каталога Wildberries параметром brand или fbrand."
      "\n"
      "Например, https://www.wildberries.ru/catalog/obuv/zhenskaya/sapogi?fbrand=16821;9465;20587;9974;15521;20250;11496;15852;30269.")))

(defn download-and-extract-data
  ([url connect-to-destination! save-to-destination!]
   (try (connect-to-destination!)

        (let [extract-all-goods-data (build-all-goods-data-extractor
                                       COLUMNS_PARSERS)]
          (->> url
               prepare-urls-for-parsing
               extract-all-goods-data
               save-to-destination!))

        (catch Exception exception (println exception))))
  ([url save-to-destination]
   (download-and-extract-data url #() save-to-destination)))

(def cli-options
  [[nil "--url URL" "URL для обхода"]
   [nil "--save-to-database" "Флаг о необходимости сохранять в базу"

    :id :save-to-database?]])

(defn -main
  [& args]
  (let [{:keys [options]} (cli/parse-opts args cli-options)
        {:keys [url save-to-database?]} options]

    (cond (not url) (print-prerequisites)
          save-to-database? (download-and-extract-data
                              url
                              connect-to-database!
                              (partial save-to-database! COLUMNS))
          :else (download-and-extract-data url
                                           (partial save-to-file!
                                                    COLUMNS_TITLES)))))
