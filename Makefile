IMAGE_NAME = german.tebiev/wildberries-scrapping:latest
MAKEFILE_PATH := $(abspath $(lastword $(MAKEFILE_LIST)))
MAKEFILE_DIRECTORY := $(dir $(MAKEFILE_PATH))

.PHONY: build
build:
	@echo "Начинаем создание образа обходчика Wildberries."
	docker image build \
	 --tag $(IMAGE_NAME) \
	 --file Dockerfile .
	@echo "Создание образа завершено."

.PHONY: run-to-tsv
scrap-to-file:
ifndef URL
	@echo "Для запуска сборщика информации необходимо указать адрес."
	@echo "Пример: make scrap URL=\"https://www.wildberries.ru/catalog/zhenshchinam/odezhda/bryuki-i-shorty?page=1&fbrand=6780;4134;564\"."
else
	docker run --rm \
		--mount type=bind,src="$(MAKEFILE_DIRECTORY)results",dst="/usr/src/app/results" \
		$(IMAGE_NAME) --url "$(URL)"
endif

.PHONY: run-to-database
scrap:
ifndef URL
	@echo "Для запуска сборщика информации необходимо указать адрес."
	@echo "Пример: make scrap URL=\"https://www.wildberries.ru/catalog/zhenshchinam/odezhda/bryuki-i-shorty?page=1&fbrand=6780;4134;564\"."
else
	docker run --network host --rm $(IMAGE_NAME) --url "$(URL)" --save-to-database
endif