IMAGE_NAME = german.tebiev/wildberries-scrapping:latest

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
	# TODO: Добавить автоматическую очистку контейнера (--rm) после восстановления сохранения файла.
	# docker run $(IMAGE_NAME) --url "$(URL)"
	@echo "В настоящий момент сохранение в файл не работает и будет восстановлено в следующих версиях программы."
endif

.PHONY: run-to-database
scrap:
ifndef URL
	@echo "Для запуска сборщика информации необходимо указать адрес."
	@echo "Пример: make scrap URL=\"https://www.wildberries.ru/catalog/zhenshchinam/odezhda/bryuki-i-shorty?page=1&fbrand=6780;4134;564\"."
else
	docker run --network host --rm $(IMAGE_NAME) --url "$(URL)" --save-to-database
endif