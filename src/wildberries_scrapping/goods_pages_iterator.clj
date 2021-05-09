(ns wildberries-scrapping.goods-pages-iterator
  (:require [clojure.string :refer [join]]
            [clojure.data.json :as json]
            [outpace.config :refer [defconfig]]

            [wildberries-scrapping.urls-preparators :refer
             [build-goods-url-with-page]])

  (:import (org.jsoup Jsoup HttpStatusException)
           (org.jsoup.nodes Document)))

(defn- goods-pages-iterated? [goods] (empty? goods))

(defn- all-goods-pages-iterated?
  [goods-pages goods]
  (and (= (count goods-pages) 1) (goods-pages-iterated? goods)))

(defn- extract-good-data [column-parsers good] (map #(% good) column-parsers))

(defn- extract-goods-data
  [column-parsers goods]
  (let [extract-good-data (partial extract-good-data column-parsers)]
    (map extract-good-data goods)))

(defconfig good-class)
(defn- select-goods [html] (.select html good-class))

(defn- get-html
  ^Document [url]
  (-> url
      Jsoup/connect
      .get))

(defconfig good-id-attribute)
(defn- extract-good-ids [good] (.attr good good-id-attribute))

(def ^:private extract-goods-ids (comp flatten (partial map extract-good-ids)))

(defconfig data-url)
(defn- download-additional-goods-data
  [ids]
  (-> (str data-url (join ";" ids))
      get-html
      .body
      .text
      (json/read-str :key-fn keyword)
      :data
      :products))

(defn- extract-goods-page-data
  [column-parsers url]
  (println (str "Выгружаются данные со страницы " url "."))

  (let [extract-goods-data (partial extract-goods-data column-parsers)]
    (try (-> url
             get-html
             select-goods
             extract-goods-ids
             download-additional-goods-data
             extract-goods-data)
         (catch HttpStatusException _ '()))))

(def ^:private INITIAL_PAGE 1)

(defn- extract-all-goods-data
  [column-parsers urls]
  (loop [urls urls
         page INITIAL_PAGE
         all-goods-data '()]
    (let [url (first urls)
          goods-page-url (build-goods-url-with-page url page)
          goods-page-data (extract-goods-page-data column-parsers
                                                   goods-page-url)
          updated-all-goods-data (concat all-goods-data goods-page-data)]

      (cond (all-goods-pages-iterated? urls goods-page-data)
              updated-all-goods-data
            (goods-pages-iterated? goods-page-data)
              (recur (rest urls) INITIAL_PAGE updated-all-goods-data)
            :else (recur urls (inc page) updated-all-goods-data)))))

(defn build-all-goods-data-extractor
  "
  Задача columns-parsers производится извне для того, чтобы сразу дать
  разработчику представление о составе итоговой таблицы только по взгляду
  на core-файл.
  "
  [columns-parsers]
  (partial extract-all-goods-data columns-parsers))
