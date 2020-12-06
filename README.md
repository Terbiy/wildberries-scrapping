# Wildberries Scrapping

Данное ПО необходимо для анализа скидок на бренды на сайте Wildberries.

Результатом выполнения программы является текстовая строка, которая может быть сохранена средствами командной оболочки в файл формата `tsv`. Смотри [пример](#пример).

Затем содержимое файла можно скопировать и вставить напрямую в Excel.

## Использование

Для работы программы на компьютере должны быть установлена Java. При разработке использовалась Java 8.

Для использования скомпилированной программы необходимо сохранить наиболее свежий файл из папки `target/uberjars`.

    $ java -jar wildberries-scrapping-<версия программы>-standalone.jar --url <адрес страницы с параметром brand>

## Пример

Мы хотим посмотреть скидки для брендов Befree, Mango, ZARINA и Zolla.

На сайте они располагаются по следующей ссылке: https://www.wildberries.ru/catalog/zhenshchinam/odezhda?brand=4126;2513;4130;65918.

Итоговая команда будет выглядеть следующим образом:

    $ java -jar wildberries-scrapping-0.1.0-standalone.jar --url "https://www.wildberries.ru/catalog/zhenshchinam/odezhda?brand=4126;2513;4130;65918"

Сохранение в файл можно осуществить следующим образом:

    $ java -jar wildberries-scrapping-0.1.0-standalone.jar --url "https://www.wildberries.ru/catalog/zhenshchinam/odezhda?brand=4126;2513;4130;65918" > ~/wildberries-data.tsv

Таким образом вся информация окажется в файле `wildberries-data.tsv` в домашней директории.

## Лицензия

Использована лицензия MIT.