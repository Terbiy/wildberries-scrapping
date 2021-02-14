IMAGE_NAME = german.tebiev/wildberries-scrapping:latest
BUILD_TIME_EPOCH := $(shell date +"%s")
BUILD_TIME_UTC := $(shell date -u -r $(BUILD_TIME_EPOCH) +'%Y%m%d-%H%M%S')
FILE_NAME = wildberries-scrapping-$(BUILD_TIME_UTC).tsv

.PHONY: build
build:
	@echo "Начинаем создание образа обходчика Wildberries."
	docker image build \
	 --tag $(IMAGE_NAME) \
	 --file Dockerfile .
	@echo "Создание образа завершено."

.PHONY: run-to-tsv
scrap:
ifndef URL
	@echo "Для запуска сборщика информации необходимо указать адрес."
	@echo "Пример: make scrap URL=\"https://www.wildberries.ru/catalog/zhenshchinam/odezhda?brand=4126\"."
else
	docker run $(IMAGE_NAME) --url "$(URL)" > ./results/$(FILE_NAME)
	@echo "Данные о скидках сохранены в файле $(FILE_NAME)."
endif
