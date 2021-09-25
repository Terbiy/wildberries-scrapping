IMAGE_NAME := german.tebiev/wildberries-scrapping:latest

.PHONY: build
build:
	@echo "Начинаем создание образа обходчика Wildberries."
	docker image build \
	 --tag $(IMAGE_NAME) \
	 --file Dockerfile .
	@echo "Создание образа завершено."


MAKEFILE_DIRECTORY := $(shell pwd)
MOUNT_CONFIG := --mount type=bind,src="$(MAKEFILE_DIRECTORY)/config.edn",dst="/usr/src/app/config.edn"

.PHONY: run-to-tsv
scrap-to-file:
ifndef URL
	@echo "Для запуска сборщика информации необходимо указать адрес."
	@echo "Пример: make scrap-to-file URL=\"https://www.wildberries.ru/catalog/zhenshchinam/odezhda/bryuki-i-shorty?page=1&fbrand=6780;4134;564\"."
else
	docker run --rm \
		--mount type=bind,src="$(MAKEFILE_DIRECTORY)/results",dst="/usr/src/app/results" \
		$(MOUNT_CONFIG) \
		$(IMAGE_NAME) --url "$(URL)"
endif


REDIRECT_TABLE_IF_PRESENT := $(if $(WB_SCRAPPING_TABLE),--env WB_SCRAPPING_TABLE="$(WB_SCRAPPING_TABLE)",)

.PHONY: run-to-database
scrap:
ifndef URL
	@echo "Для запуска сборщика информации необходимо указать адрес."
	@echo "Пример: make scrap URL=\"https://www.wildberries.ru/catalog/zhenshchinam/odezhda/bryuki-i-shorty?page=1&fbrand=6780;4134;564\"."
else
	docker run --rm --network host \
		$(MOUNT_CONFIG) \
		$(REDIRECT_TABLE_IF_PRESENT) \
		$(IMAGE_NAME) --url "$(URL)" --save-to-database
endif