(ns wildberries-scrapping.core
  (:gen-class)
  (:require [clojure.string :refer [split join includes?]]
            [lambdaisland.uri :as uri])
  (:import (org.jsoup Jsoup HttpStatusException)))

(def ^:dynamic *brand-signature* :brand)

(def PROPERTIES_SEPARATOR "\t")

(def GOODS_SEPARATOR "\n")

(defn brand-iterated? [titles] (empty? titles))

(defn all-brands-iterated?
  [brands titles]
  (and (= (count brands) 1) (brand-iterated? titles)))

(defn get-property
  [good property]
  (try (-> good
           (.select property)
           .textNodes
           first
           .toString)
       (catch NullPointerException _ "")))

(defn get-discount [good] (get-property good ".price-sale"))

(defn get-name [good] (get-property good ".goods-name"))

(defn get-brand [good] (get-property good ".brand-name"))

(defn extract-discount
  [good]
  (join PROPERTIES_SEPARATOR
        [(get-brand good) (get-name good) (get-discount good)]))

(defn extract-discounts [goods] (map extract-discount goods))

(defn select-items [html] (.select html ".dtList-inner"))

(defn get-html
  [url]
  (-> url
      Jsoup/connect
      .get))

(defn get-discounts-for-brand
  [url]
  (try (-> url
           get-html
           select-items
           extract-discounts)
       (catch HttpStatusException _ '())))

(defn build-url-with-page
  [url brand page]
  (-> url
      (uri/assoc-query *brand-signature* brand :page page)
      .toString))

(def INITIAL_PAGE 1)

(defn split-brands [brands-string] (split brands-string #";"))

(defn extract-brands [query-map] (get query-map *brand-signature*))

(defn get-discounts
  [url]
  (let [url (uri/parse url)
        brands (-> url
                   uri/query-map
                   extract-brands
                   split-brands)]
    (loop [brands brands
           page INITIAL_PAGE
           all-titles '()]
      (let [brand (first brands)
            brand-page-url (build-url-with-page url brand page)
            titles (get-discounts-for-brand brand-page-url)]
        (cond (all-brands-iterated? brands titles) all-titles
              (brand-iterated? titles)
                (recur (rest brands) INITIAL_PAGE (concat all-titles titles))
              :else (recur brands (inc page) (concat all-titles titles)))))))

(defn define-brand-signature
  [url]
  (cond (includes? url "fbrand") :fbrand
        (includes? url "brand") :brand
        :else nil))

(defn get-url [args] (get args "--url"))

(defn -main
  [& args]
  (let [map-args (apply hash-map args)
        url (get-url map-args)]
    (binding [*brand-signature* (define-brand-signature url)]
      (if (and url *brand-signature*)
        (->> url
             get-discounts
             (join GOODS_SEPARATOR)
             println)
        (println
          "Для работы программы необходимо предоставить адрес каталога Wildberries. Например, https://www.wildberries.ru/catalog/obuv/zhenskaya/sapogi?fbrand=16821;9465;20587;9974;15521;20250;11496;15852;30269.")))))