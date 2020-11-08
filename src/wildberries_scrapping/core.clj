(ns wildberries-scrapping.core
  (:gen-class)
  (:require [net.cgrand.enlive-html :as html]
            [org.httpkit.client :as http]
            [clojure.string :refer [split join]]))

(defn get-brand
  [info]
  (-> info
      (nth 3)
      :content
      first))

(defn get-name
  [info]
  (-> info
      (nth 5)
      :content
      first))

(defn get-discount
  [info]
  (-> info
      (nth 1)
      :content second
      :content second
      :content first))

(defn build-discount-row
  [dom]
  (map (comp #(join "\t" [(get-brand %) (get-name %) (get-discount %)])
             :content)
       (html/select dom [:span.i-catalog-prev-info])))

(defn get-dom
  [url]
  (-> url
      http/get
      deref
      :body
      html/html-snippet))

(defn get-discounts
  [url]
  (-> url
      get-dom
      build-discount-row))

(defn brand-iterated? [titles] (empty? titles))

(defn all-brands-iterated?
  [brands titles]
  (and (= (count brands) 1) (brand-iterated? titles)))

(defn build-url-with-page
  [url campaign brands page]
  (str url campaign "?brand=" brands "&page=" page))

(defn split-brands [brands-string] (split brands-string #";"))

(def INITIAL_PAGE 1)

(defn get-all-titles-from-all-pages
  [url campaign brands]
  (loop [brands (split-brands brands)
         page INITIAL_PAGE
         all-titles '()]
    (let [brand (first brands)
          full-url (build-url-with-page url campaign brand page)
          titles (get-discounts full-url)]
      (cond (all-brands-iterated? brands titles) all-titles
            (brand-iterated? titles)
            (recur (rest brands) INITIAL_PAGE (concat all-titles titles))
            :else (recur brands (inc page) (concat all-titles titles))))))

(defn get-campaign [args] (get args "--campaign"))

(defn get-brands [args] (get args "--brands"))

(def CAMPAIGN_FORMAT "--campaign \"vsemirniy-den-shopinga\"")

(def BRANDS_FORMAT "--brands \"14126;75084;4077;4890;14130;75918\"")

(def URL "https://www.wildberries.ru/promotions/")

(defn -main
  [& args]
  (let [map-args (apply hash-map args)
        campaign (get-campaign map-args)
        brands (get-brands map-args)]
    (cond
      (and campaign brands)
      (let [titles (get-all-titles-from-all-pages URL campaign brands)]
        (println (clojure.string/join "\n" titles)))
      (and (nil? campaign) (nil? brands))
      (println "Команда вводится в формате" CAMPAIGN_FORMAT BRANDS_FORMAT)
      (nil? campaign)
      (println
        "Не введено название компании, необходимо добавить при запуске в формате"
        CAMPAIGN_FORMAT)
      (nil? brands)
      (println
        "Не введены интересующие бренды, необходимо добавить при запуске в формате"
        BRANDS_FORMAT))))
