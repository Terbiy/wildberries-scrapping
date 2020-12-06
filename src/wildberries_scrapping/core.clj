(ns wildberries-scrapping.core
  (:gen-class)
  (:require [clojure.string :refer [split join]]
            [lambdaisland.uri :as uri])
  (:import (org.jsoup Jsoup HttpStatusException)))

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
      (uri/assoc-query :brand brand :page page)
      .toString))

(def INITIAL_PAGE 1)

(defn split-brands [brands-string] (split brands-string #";"))

(defn get-discounts
  [url]
  (let [url (uri/parse url)
        brands (-> url
                   uri/query-map
                   :brand
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

(defn get-url [args] (get args "--url"))

(defn -main
  [& args]
  (let [map-args (apply hash-map args)
        url (get-url map-args)]
    (if url
      (->> url
           get-discounts
           (join GOODS_SEPARATOR)
           println)
      (println
        "Команда вводится в формате --url \"https://www.wildberries.ru/promotions/cyber-monday-odezhda-obuv-i-aksessuary?brand=14126;4077;4890;14130;75918\""))))