(ns wildberries-scrapping.data-extractors)

(defn get-comments-number [good] (:feedbacks good))

(defn get-rating [good] (:rating good))

(defn get-price [good] (:salePriceU good))

(defn get-discount [good] (:sale good))

(defn get-name [good] (:name good))

(defn get-article [good] (:id good))

(defn get-brand [good] (:brand good))
