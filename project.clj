(defproject wildberries-scrapping "0.8.1"
  :description "Выгрузка данных товаров с сайта Wildberries."
  :license {:name "MIT", :url "https://mit-license.org/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.cli "1.0.206"]
                 [org.jsoup/jsoup "1.13.1"]
                 [lambdaisland/uri "1.4.54"]
                 [com.github.seancorfield/next.jdbc "1.1.646"]
                 [com.microsoft.sqlserver/mssql-jdbc "8.2.1.jre8"]]
  :main ^:skip-aot wildberries-scrapping.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all,
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
