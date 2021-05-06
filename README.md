# Wildberries Scrapping

Данный инструмент предназначен для сбора данных товаров с сайта Wildberries.

При использовании команды `make scrap` данные будут сохранены в базу. По умолчанию предполагается, что она работает локальной машине (IP-адрес `127.0.0.1`) на порту `1433`.

При использовании команды `make scrap-to-file` результатом работы инструмента будет текстовый файл формата `tsv` в папке `results`. Смотри [пример](#пример).

Содержимое файла можно скопировать в Excel с сохранением форматирования.

## Настройка

После выгрузки данного репозитория на компьютер необходимо доработать файл с настройками подключения к базе: `config.edn`:
```clojure
{
 ;; Настройки для подключения к базе данных.
 wildberries-scrapping.configuration/user "<Имя пользователя>"
 wildberries-scrapping.configuration/password "<Пароль>"
 wildberries-scrapping.configuration/database-name "<Имя базы>"
 wildberries-scrapping.configuration/host "<Имя или адрес машины с SQL Server>"
 wildberries-scrapping.configuration/port "<Порт SQL Server на целевой машине>"

 ;; Настройки для проведения операции вставки в таблицу.
 wildberries-scrapping.configuration/table-name #config/or [#config/env "WB_SCRAPPING_TABLE"
                                                            ;; Имя таблицы.
                                                            "<Имя таблицы>"]
 ;; Ограничение на количество вставляемых за раз элементов.
 ;; Их превышение приводит к ошибке SQL.
 wildberries-scrapping.configuration/values-for-single-insert-limit 2100
}
```

Пример конфигурации можно найти в файле `config.edn`.

## Использование

Для работы программы на компьютере должен быть установлен и запущен [Docker Desktop](https://www.docker.com/products/docker-desktop).

В командной строке в корневой папке этого инструмента нужно запустить команду сборки:

```shell
make build
```

Эту команду нужно запускать также после каждого обновления инструмента.

Запуск инструмента для сохранения результатов в базу осуществляется после сборки следующей командой:
```shell
make scrap URL="<адрес страницы Wildberries или адреса через пробел>"
```

Если необходимо «на лету» изменить таблицу, в которой будут сохранены результаты, это можно сделать следующим образом:
```shell
WB_SCRAPPING_TABLE="<Имя таблицы>" make scrap URL="<адрес страницы Wildberries или адреса через пробел>"
```

Для сохранения результатов в файл используйте следующую команду:
```shell
make scrap-to-file URL="<адрес страницы Wildberries или адреса через пробел>"
```

## Примеры

Мы хотим посмотреть скидки для брендов Befree, Mango, ZARINA и Zolla и сохранить результаты в файл.

На сайте товары располагаются по следующей ссылке: https://www.wildberries.ru/catalog/zhenshchinam/odezhda?fbrand=4126;2513;4130;65918.

Команда запуска инструмента для сохранения результатов в файл будет выглядеть следующим образом:
```shell
make scrap-to-file URL="https://www.wildberries.ru/catalog/zhenshchinam/odezhda?fbrand=4126;2513;4130;65918"
```

Результат сбора данных будет доступен в папке `results` в самом свежем файле.

Запуск программы для нескольких адресов выглядит следующим образом:
```shell
make scrap-to-file URL="https://www.wildberries.ru/catalog/zhenshchinam/odezhda?fbrand=4126;2513;4130;65918 https://www.wildberries.ru/catalog/obuv/zhenskaya?page=1&fbrand=74915;7363;246"
```

Запуск программы для сохранения результатов в базу выглядит так:
```shell
make scrap URL="https://www.wildberries.ru/catalog/zhenshchinam/odezhda?fbrand=4126;2513;4130;65918 https://www.wildberries.ru/catalog/obuv/zhenskaya?page=1&fbrand=74915;7363;246"
```

Если необходимо сохранить результаты в таблицу `wb_scrapping_men`, то необходимо запустить следующую команду:
```shell
WB_SCRAPPING_TABLE="wb_scrapping_men" make scrap URL="https://www.wildberries.ru/catalog/muzhchinam/odezhda/bryuki-i-shorty?sort=popular&page=1&fbrand=65918%3B4126"
```

## Лицензия

Использована лицензия MIT.
