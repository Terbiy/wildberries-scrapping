(ns wildberries-scrapping.goods-pages-iterator
  (:require [wildberries-scrapping.urls-preparators :refer
             [build-goods-url-with-page]])

  (:import (org.jsoup Jsoup HttpStatusException)))

(defn goods-pages-iterated? [goods] (empty? goods))

(defn all-goods-pages-iterated?
  [goods-pages goods]
  (and (= (count goods-pages) 1) (goods-pages-iterated? goods)))

(defn extract-good-data [column-parsers good] (map #(% good) column-parsers))

(defn extract-goods-data
  [column-parsers goods]
  (let [extract-good-data (partial extract-good-data column-parsers)]
    (map extract-good-data goods)))

(defn select-items [html] (.select html ".dtList"))

(defn get-html
  [url]
  (-> url
      Jsoup/connect
      .get))

(defn extract-goods-page-data
  [column-parsers url]
  (println (str "Выгружаются данные со страницы " url "."))

  (let [extract-goods-data (partial extract-goods-data column-parsers)]
    (try (-> url
             get-html
             select-items
             extract-goods-data)
         (catch HttpStatusException _ '()))))

(def INITIAL_PAGE 1)

(defn extract-all-goods-data
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
