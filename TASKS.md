# Задачи проекта: Разработка MVP приложения поиска товара в магазине (Вариант 6)

## Этап 1. Анализ и подготовка окружения
- [x] Изучить рабочую папку (`D:\projects\3187350`)
- [x] Обнаружить реальные пути Android SDK, JDK, Gradle, Git и Python
- [x] Сформировать и зафиксировать `TASKS.md`

## Этап 2. Создание и инициализация Android-проекта
- [x] Инициализировать локальный Git-репозиторий (`git init`)
- [x] Настроить `.gitignore`
- [x] Создать структуру проекта Android Studio (модуль `app`, Gradle Wrapper, `settings.gradle`, `build.gradle`, `app/build.gradle`)
- [x] Сконфигурировать `local.properties` с обнаруженным `sdk.dir`
- [x] Настроить параметры компиляции: `minSdkVersion 17`, `compileSdkVersion 34`, `targetSdkVersion 34`, Java 8 compatibility
- [x] Проверить начальную сборку (`gradlew.bat assembleDebug`)
- [x] Зафиксировать первый коммит: "Initial Android project setup"

## Этап 3. Локальное хранение данных и демо-каталог
- [x] Реализовать `DatabaseHelper` с явным включением внешних ключей `setForeignKeyConstraintsEnabled(true)`
- [x] Создать модели данных `Section` и `Product` (с поддержкой `normalizedName` и upsert)
- [x] Реализовать `StoreRepository` для изолированной работы с SQLite и SharedPreferences
- [x] Реализовать `DemoDataGenerator` с нейтральным растровым планом (PNG) и демо-каталогом (5 разделов, 10 товаров)
- [x] Зафиксировать коммит: "Add local data storage and demo catalog"

## Этап 4. Бизнес-логика, алгоритмы и модульные тесты
- [x] Создать `MapCoordinateMapper` (чистый Java-класс для пересчёта экранных и относительных координат)
- [x] Реализовать `SearchNormalizer` (очистка пробелов, перевод в нижний регистр, поддержка русского языка и «е»/«ё»)
- [x] Реализовать `ProductSearchService` (поиск по префиксу с приоритетом точных совпадений)
- [x] Реализовать `ProductImportParser` (поддержка UTF-8, UTF-8 с BOM, разделителей `\r\n` и `\n`, разделителя `;`, обработка ошибок формата)
- [x] Реализовать `ShoppingListParser` (разбор строк буфера обмена, пропуск пустых строк, очистка маркеров)
- [x] Написать автоматизированные тесты JUnit 4:
  - `SearchNormalizerTest`
  - `ProductSearchServiceTest`
  - `ProductImportParserTest`
  - `ShoppingListParserTest`
  - `StoreMapCoordinatesTest` (тестирующий `MapCoordinateMapper`)
- [x] Запустить тесты (`gradlew.bat testDebugUnitTest`) и убедиться в их успешном прохождении (19/19)
- [x] Зафиксировать коммит: "Implement core domain services and automated tests"

## Этап 5. Схема магазина и функционал оператора
- [x] Реализовать кастомный `StoreMapView` с использованием `MapCoordinateMapper`, масштабированием `fitCenter`, безопасным декодированием через `inSampleSize` и статическим выделением выбранного маркера
- [x] Создать `MainActivity` с русскоязычным интерфейсом, кнопками ролей и индикатором состояния БД
- [x] Создать `OperatorActivity` для просмотра схемы, добавления разделов по клику, списка разделов с удалением
- [x] Реализовать выбор схемы через `Intent.ACTION_GET_CONTENT` с копированием во внутреннее хранилище приложения
- [x] Обеспечить корректность работы в Portrait и Landscape ориентациях
- [x] Зафиксировать коммит: "Implement store map and section markers"

## Этап 6. Импорт списка товаров
- [x] Создать `ProductImportActivity` с описанием формата, предпросмотром, выбором файла и ручной вставкой текста
- [x] Реализовать валидацию строк и обновление существующих товаров (upsert по `normalizedName`)
- [x] Выводить статистику импорта (добавлено, обновлено, пропущено, список ошибок)
- [x] Зафиксировать коммит: "Add product import functionality"

## Этап 7. Функционал покупателя: Поиск товаров и схема
- [x] Создать `SearchActivity` с вводом названия, подсказками по префиксу, списком товаров
- [x] Реализовать отображение найденного раздела и статическую подсветку его маркера на схеме
- [x] Обработать сценарий отсутствующего товара с понятным сообщением
- [x] Зафиксировать коммит: "Implement product search and map highlighting"

## Этап 8. Функционал покупателя: Список покупок из буфера обмена
- [x] Создать `ShoppingListActivity` с многострочным полем ввода
- [x] Реализовать получение текста из `ClipboardManager` с проверкой на наличие и непустоту текста
- [x] Реализовать пакетный поиск и сопоставление товаров с отображением статусов («Найдено: [Раздел]» / «Не найден»)
- [x] Зафиксировать коммит: "Add clipboard shopping list processing"

## Этап 9. QA, стабилизация и ориентация экрана
- [x] Выполнить повторный прогон всех тестов
- [x] Проверить работу на экранах от 480×640 px
- [x] Проверить поведение интерфейса при смене ориентации экрана
- [x] Запустить эмулятор Android и захватить реальные скриншоты всех обязательных экранов
- [x] Зафиксировать коммит: "Add compatibility fixes and QA verification"

## Этап 10. Документация, логи и финальные артефакты
- [x] Создать `README.md` (все 18 обязательных разделов)
- [x] Создать Markdown-документацию в `docs/` (`index.md`, `installation.md`, `operator-guide.md`, `customer-guide.md`, `testing.md`, `limitations.md`)
- [x] Настроить и проверить `mkdocs.yml` (сборка статического сайта через `mkdocs build`)
- [x] Сформировать `report/REPORT_DATA.md` с подтверждёнными фактами
- [x] Выполнить чистую сборку `gradlew.bat clean assembleDebug`
- [x] Скопировать APK в `artifacts/apk/`
- [x] Сохранить логи в `artifacts/logs/`
- [x] Заполнить `STATUS.md`, `CHECKLIST.md`, `INPUT_REQUIRED.md`
- [x] Зафиксировать финальные коммиты
