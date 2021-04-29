(ns wildberries-scrapping.urls-preparators
  (:require [clojure.string :refer [split]]
            [lambdaisland.uri :as uri]))

(defn split-brands [brands-string] (split brands-string #";"))

(defn split-url-by-brands
  "
  Иногда в качестве обозначения брендов в URL используется параметр brand,
  а иногда fbrand.
  "
  [query-map]
  (cond (:brand query-map) {:key :brand,
                            :brands (split-brands (:brand query-map))}
        (:fbrand query-map) {:key :fbrand,
                             :brands (split-brands (:fbrand query-map))}
        :else {:key "", :brands []}))

(defn build-url-with-single-brand
  [url key brand]
  (.toString (uri/assoc-query url key brand)))

(defn build-urls-with-single-brand
  [url brands-data]
  (map (partial build-url-with-single-brand url (:key brands-data))
    (:brands brands-data)))

(defn prepare-urls-for-parsing
  "
  Разбиение полного адреса на отдельные адреса по каждому бренду необходимо
  из-за того, что выдача Wildberries ограничена 10 000 результатами.
  Наличие отдельных адресов для каждого бренда не исключает полностью
  проблему неучёта товаров, но существенно снижает риск её появления.
  Бренды с количеством элементов более 10 000 пока не были замечены.
  "
  [url]
  (let [build-urls-with-single-brand (partial build-urls-with-single-brand url)
        brands-urls (-> url
                        uri/query-map
                        split-url-by-brands
                        build-urls-with-single-brand)]
    (if (not-empty brands-urls) brands-urls [url])))

(defn build-goods-url-with-page
  [url page]
  (-> url
      (uri/assoc-query :page page)
      .toString))
