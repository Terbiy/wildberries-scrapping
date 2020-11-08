(defproject wildberries-scrapping "0.1.0"
  :description "Выгрузка данных про скидки брендов в рамках скидочных кампаний на сайте Wildberries."
  :license {:name "MIT"
            :url "https://mit-license.org/"}
  :dependencies [[org.clojure/clojure "1.10.1"] [enlive "1.1.6"] [http-kit "2.5.0"]]
  :main ^:skip-aot wildberries-scrapping.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
