IMAGE_NAME = german.tebiev/wildberries-scrapping:latest

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
	docker run $(IMAGE_NAME) --url "$(URL)"
endif
