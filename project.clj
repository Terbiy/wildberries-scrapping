(defproject wildberries-scrapping "0.2.0"
  :description "Выгрузка данных про скидки брендов с сайта Wildberries."
  :license {:name "MIT", :url "https://mit-license.org/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.jsoup/jsoup "1.13.1"]
                 [lambdaisland/uri "1.4.54"]]
  :main ^:skip-aot wildberries-scrapping.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all,
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
