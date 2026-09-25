# Фактические данные проекта (REPORT_DATA)

Данный документ содержит исключительно фактически подтвержденные сведения о реализации проекта для последующей подготовки пояснительной записки.

---

### 1. Официальная тема
«Разработка MVP приложения поиска товара в магазине»

### 2. Вариант
Вариант 6 рейтинговой работы.

### 3. Название приложения
StoreSearchApp («Поиск товаров в магазине»)

### 4. Назначение
Локальное автономное Android-приложение для ориентирования покупателей в торговом зале магазина с интерактивной привязкой товаров к разделам на схеме торгового зала и возможностью администрирования ассортимента оператором.

### 5. Фактически реализованные функции
- **Главный экран**:
  - Название и краткое описание приложения.
  - Индикатор актуального состояния локальной БД (количество разделов и товаров).
  - Выбор роли «Оператор магазина» и «Покупатель».
  - Загрузка стандартного демонстрационного каталога (5 разделов, 10 товаров, схема).
  - Полная очистка каталога с диалогом подтверждения.
- **Режим оператора**:
  - Загрузка растровой схемы торгового зала (встроенной по умолчанию либо пользовательской из памяти устройства через `ACTION_GET_CONTENT`).
  - Сохранение пользовательской схемы во внутреннее постоянное хранилище приложения (`custom_store_map.png`).
  - Сброс схемы к стандартному нейтральному растровому плану.
  - Размещение меток разделов интерактивным нажатием на схему торгового зала.
  - Преобразование экранных пикселей касания в относительные координаты `[0.0, 1.0]` и сохранение в БД.
  - Валидация названий разделов (запрет пустых строк и дубликатов).
  - Просмотр списка разделов и их удаление с каскадным удалением привязанных товаров.
  - Кнопка быстрого перехода к экрану импорта товаров.
- **Импорт товаров**:
  - Описание поддерживаемого формата строк `Товар;Раздел`.
  - Загрузка файлов `.txt` / `.csv` через системный выбор файлов `ACTION_GET_CONTENT`.
  - Поддержка кодировки UTF-8 и UTF-8 с BOM (`\uFEFF`).
  - Поддержка переводов строк Windows (`\r\n`) и Unix (`\n`).
  - Ручная вставка и редактирование строк с кнопкой «Вставить пример».
  - Валидация строк: строки без разделителя `;`, с пустыми полями или с неизвестными разделами пропускаются без сбоев.
  - Обновление связей существующих товаров при повторном импорте (upsert по `normalized_name`).
  - Детальный отчет: количество добавленных/обновленных позиций, число пропущенных строк и подробный лог ошибок.
- **Режим покупателя (поиск)**:
  - Строка ввода поискового запроса с реакцией на ввод по мере набора (TextWatcher).
  - Префиксный поиск товаров от одной введенной буквы.
  - Приоритетный вывод точного совпадения в начале списка.
  - Отображение выпадающих подсказок с названием товара и связанного раздела.
  - Выбор товара: вывод плашки «Товар: [Название] \n Раздел: [Раздел]» и статическое визуальное выделение маркера раздела на плане магазина.
  - Статическое выделение маркера: яркий контрастный цвет (красный), увеличенный радиус, жирная белая обводка и внешнее акцентное кольцо (без пульсации).
  - Информативное сообщение при отсутствии товара в каталоге.
  - Переход к экрану поиска по списку покупок.
- **Режим покупателя (список покупок)**:
  - Многострочное текстовое поле для ввода списка товаров.
  - Извлечение текста из буфера обмена через `ClipboardManager` с проверкой на наличие и непустоту данных.
  - Очистка стандартных маркеров списков (`1.`, `-`, `•`).
  - Пакетное сопоставление каждой позиции списка с каталогом магазина.
  - Наглядное разделение статусов: найденные товары («Товар — Раздел», зеленый) и ненайденные («Товар — Не найден», красный).
  - Сводка: общее число обработанных строк, число найденных и отсутствующих товаров.
- **Адаптивность интерфейса**:
  - Полная поддержка портретной и альбомной ориентаций экрана.
  - Использование `ScrollView` для предотвращения обрезания элементов на экранах от 480×640 px.
  - Сохранение относительных координат меток разделов независимо от геометрии экрана.

### 6. Функции, которые не реализованы (Non-Goals)
- Внешний HTTP/REST сервер и удаленная база данных.
- Авторизация, аутентификация и пароли для ролей.
- Несколько этажей, зон или филиалов магазинов.
- GPS, компас, геолокация и трекинг положения покупателя.
- Расчет кратчайшего пути и прокладывание пешеходных маршрутов.
- Учет цен, скидок, акций и остатков на складе.
- Корзина покупок, оплата и чеки.
- Сканирование штрихкодов / QR-кодов камерой.
- Нейросетевые модели и нечеткий поиск по расстоянию Левенштейна.
- Сложный векторный редактор схем помещений.

### 7. Использованные технологии и версии
- Язык программирования: Java (версия языка 8, OpenJDK 21.0.9 runtime).
- Среда сборки: Gradle 8.14.3.
- Android Gradle Plugin: 8.11.1.
- Тестирование: JUnit 4.13.2.
- Генерация статического сайта документации: MkDocs 1.6.1 (Python 3.13, uv).

### 8. Конфигурация Android
- `applicationId`: `ru.study.storesearch`
- `minSdkVersion`: 17 (Android 4.2 Jelly Bean)
- `compileSdkVersion`: 34 (Android 14)
- `targetSdkVersion`: 34
- `versionCode`: 1
- `versionName`: "1.0"
- Тема приложения: `@android:style/Theme.Holo.Light.DarkActionBar` (стандартная тема SDK, гарантированно доступная в API 17)

### 9. Структура проекта
- `app/src/main/AndroidManifest.xml` — манифест приложения с регистрацией 5 экранов.
- `app/src/main/java/ru/study/storesearch/data/` — модели данных (`Section`, `Product`), `DatabaseHelper`, `StoreRepository`, `DemoDataGenerator`.
- `app/src/main/java/ru/study/storesearch/domain/` — сервисы бизнес-логики (`SearchNormalizer`, `ProductSearchService`, `ProductImportParser`, `ShoppingListParser`, `MapCoordinateMapper`, `BitmapUtils`).
- `app/src/main/java/ru/study/storesearch/ui/` — экраны (`MainActivity`, `OperatorActivity`, `ProductImportActivity`, `SearchActivity`, `ShoppingListActivity`) и кастомный View (`StoreMapView`).
- `app/src/main/res/` — XML-разметки (5 экранов + элементы списков), строковые ресурсы на русском языке, цветовая палитра и нейтральный растровый план `default_store_map.png`.
- `app/src/test/java/ru/study/storesearch/domain/` — модульные тесты JUnit 4.

### 10. Основные классы и их назначение
1. `DatabaseHelper` — создание БД SQLite, таблиц `sections` и `products`, явное включение проверки внешних ключей через `setForeignKeyConstraintsEnabled(true)`.
2. `StoreRepository` — CRUD-операции с разделами и товарами, транзакционная очистка данных, работа с `SharedPreferences` для пути к схеме.
3. `DemoDataGenerator` — начальная инициализация 5 отделов и 10 товаров с безопасной проверкой наличия данных.
4. `SearchNormalizer` — очистка пробелов, перевод в нижний регистр, замена буквы «ё» на «е», проверка непустоты.
5. `ProductSearchService` — префиксный поиск (`startsWith`) с приоритетом полного точного совпадения (`equals`).
6. `ProductImportParser` — парсинг CSV/TXT строк с поддержкой UTF-8 с BOM, разделителей `\r\n` и `;`, сбор ошибок.
7. `ShoppingListParser` — разбор многострочного текста списка покупок, удаление маркеров, пакетное сопоставление.
8. `MapCoordinateMapper` — чистый Java-класс пересчета параметров `fitCenter`, взаимного преобразования координат касания и относительных величин `[0.0, 1.0]`.
9. `BitmapUtils` — безопасная загрузка растровых изображений с расчетом `inSampleSize` (степень 2) для защиты от OOM.
10. `StoreMapView` — отрисовка плана торгового зала, обычных меток и выделенного отдела с контрастным кольцом.
11. `MainActivity` — главное меню и управление данными каталога.
12. `OperatorActivity` — интерактивная расстановка меток и управление схемами магазина.
13. `ProductImportActivity` — экран пакетного импорта товаров.
14. `SearchActivity` — поиск товаров покупателем с визуальной подсветкой отдела на схеме.
15. `ShoppingListActivity` — пакетная обработка списка покупок из буфера обмена.

### 11. Способ хранения данных
Локальное хранилище устройства:
1. Реляционная база данных SQLite (`store_locator.db`) для структурированных данных отделов и товаров.
2. `SharedPreferences` (`store_search_prefs`) для сохранения пути к текущей пользовательской схеме.
3. Внутренний каталог приложения (`context.getFilesDir()`) для изолированного хранения растрового файла схемы `custom_store_map.png`.

### 12. Структура таблиц
- **Таблица `sections`**:
  - `id` INTEGER PRIMARY KEY AUTOINCREMENT
  - `name` TEXT NOT NULL UNIQUE
  - `x_percent` REAL NOT NULL (относительная координата X, 0.0 .. 1.0)
  - `y_percent` REAL NOT NULL (относительная координата Y, 0.0 .. 1.0)
- **Таблица `products`**:
  - `id` INTEGER PRIMARY KEY AUTOINCREMENT
  - `name` TEXT NOT NULL
  - `normalized_name` TEXT NOT NULL UNIQUE
  - `section_id` INTEGER NOT NULL, FOREIGN KEY (section_id) REFERENCES sections(id) ON DELETE CASCADE
  - Индекс `idx_products_normalized` по полю `normalized_name`.

### 13. Алгоритм поиска
1. Получение входной строки.
2. Проверка на непустоту через `SearchNormalizer.isBlank(query)` — при пустом запросе сразу возвращается пустой список без выполнения поисковых операций.
3. Нормализация запроса: удаление краевых пробелов, перевод в нижний регистр с русской локалью (`new Locale("ru", "RU")`), схлопывание множественных пробелов, замена «ё» на «е».
4. Проход по каталогу:
   - Если `normalizedName.equals(normalizedQuery)` — добавление в группу точных совпадений `exactMatches`.
   - Иначе если `normalizedName.startsWith(normalizedQuery)` — добавление в группу `prefixMatches`.
5. Сортировка `prefixMatches` в алфавитном порядке.
6. Объединение списков с приоритетом точных совпадений.

### 14. Алгоритм импорта
1. Извлечение текста (из файла через `ContentResolver.openInputStream` или ручного поля ввода).
2. Проверка и удаление начального символа UTF-8 BOM (`\uFEFF`).
3. Разделение текста на строки регулярным выражением `\\r?\\n`.
4. Для каждой строки:
   - Пропуск пустых строк.
   - Разделение по символу `;`.
   - Если число фрагментов меньше 2 или один из них пуст — регистрация синтаксической ошибки с номером строки.
   - Проверка наличия раздела в БД по имени (`getSectionByName`). Если раздел не найден — фиксация ошибки «Раздел отсутствует» и пропуск строки.
   - Если раздел найден — вызов `upsertProduct(name, normalizedName, sectionId)`. Если товар уже существовал, обновляется его привязка к разделу без дублирования.
5. Вывод сводки (добавлено/обновлено: N, пропущено с ошибками: M) и списка сообщений об ошибках.

### 15. Алгоритм обработки списка из буфера
1. Получение текста из `ClipboardManager` через `getPrimaryClip()`.
2. Проверка наличия и непустоты фрагмента.
3. Разделение по `\\r?\\n`.
4. Для каждой строки:
   - Очистка маркеров списков регулярным выражением `^(\\d+[.)]\\s*|[-*+•]\\s*)`.
   - Пропуск пустых строк.
   - Поиск в каталоге: сначала точное совпадение через `findExactMatch`, затем поиск по префиксу через `search`.
   - Если найден — статус «[Товар] — [Раздел]».
   - Если не найден — статус «[Товар] — Не найден».
5. Отображение списка результатов и сводного счетчика.

### 16. Способ отображения меток
- Кастомный `StoreMapView` вычисляет масштаб растра `scale = min(viewW / imgW, viewH / imgH)` и смещения центрирования `offsetX`, `offsetY` (алгоритм `fitCenter`).
- Относительные координаты раздела переводятся в пиксели экрана:
  `screenX = offsetX + (xPercent * displayedWidth)`
  `screenY = offsetY + (yPercent * displayedHeight)`
- Обычные метки: синий круг радиусом 11dp с белой обводкой 2.5dp и полупрозрачным бейджем названия.
- Выделенная метка: ярко-красный круг радиусом 16dp с белой обводкой 3dp, красным бейджем названия и внешним статическим контурным кольцом радиусом 23dp и толщиной 3.5dp (без пульсации).

### 17. Результат сборки
- Команда `gradlew.bat clean assembleDebug` завершилась с кодом 0 (`BUILD SUCCESSFUL`).
- Создан файл APK: `app/build/outputs/apk/debug/app-debug.apk` (размер 60.5 КБ).
- Копия сохранена в `artifacts/apk/app-debug.apk`.

### 18. Команды сборки
```powershell
# Полная сборка
.\gradlew.bat clean assembleDebug

# Запуск тестов
.\gradlew.bat testDebugUnitTest

# Установка на устройство/эмулятор
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 19. Перечень автоматизированных тестов
1. `SearchNormalizerTest.testNormalizeRussianTextAndTrim`
2. `SearchNormalizerTest.testNormalizeLetterYo`
3. `SearchNormalizerTest.testNormalizeEmptyAndNull`
4. `ProductSearchServiceTest.testSearchExactMatchPrioritized`
5. `ProductSearchServiceTest.testSearchByPrefixLetters`
6. `ProductSearchServiceTest.testSearchMissingProduct`
7. `ProductSearchServiceTest.testSearchEmptyQuery`
8. `ShoppingListParserTest.testParseLinesWithWindowsAndUnixNewlines`
9. `ShoppingListParserTest.testSkipEmptyAndBlankLines`
10. `ShoppingListParserTest.testStripListMarkers`
11. `ShoppingListParserTest.testProcessShoppingListFoundAndNotFound`
12. `ProductImportParserTest.testParseValidLines`
13. `ProductImportParserTest.testParseWithUtf8Bom`
14. `ProductImportParserTest.testParseInvalidFormatLines`
15. `ProductImportParserTest.testSkipEmptyLinesInImport`
16. `StoreMapCoordinatesTest.testFitCenterBoundsCalculation`
17. `StoreMapCoordinatesTest.testCoordinateRoundTrip`
18. `StoreMapCoordinatesTest.testIsInsideImageDetection`
19. `StoreMapCoordinatesTest.testClampingOutOfBounds`

### 20. Фактические результаты тестов
#### 20.1. Автоматизированные модульные тесты (JUnit 4)
- Выполнено: 19 тестов.
- Успешно: 19 тестов (100%).
- Не пройдено: 0 тестов.
- Время выполнения: 7 секунд.

#### 20.2. Ручные функциональные проверки (эмулятор API 34)
- Выполнено проверок: 11 сценариев.
- Успешно: 11 сценариев (100%).
- Не пройдено: 0.

### 21. Ручные проверки
- Запуск приложения на реальном эмуляторе Android: подтвержден.
- Открытие режима оператора и просмотр схемы: подтверждено.
- Интерактивное добавление метки нажатием на схему: подтверждено.
- Пакетный импорт ассортимента и валидация ошибок: подтверждено (10 записей импортировано).
- Поиск товара по мере ввода с подсказками: подтверждено.
- Отображение раздела и статическое выделение метки на схеме: подтверждено.
- Поиск отсутствующего товара: подтверждено.
- Пакетный разбор списка покупок из буфера: подтверждено.
- Поворот экрана (Portrait <-> Landscape): подтверждено, координаты меток сохраняются точно.
- Перезапуск приложения и сохранность данных в SQLite: подтверждено.

### 22. Проверенные версии Android
- **Android 14 (API 34, образ `system-images/android-34/google_apis_playstore/x86_64` в официальном Android Emulator)**: Фактический запуск, ручное функциональное тестирование и захват скриншотов выполнены на API 34.
- **Android 4.2 (API 17)**: Совместимость обеспечена на уровне байткода, манифеста (`minSdkVersion 17`) и применяемых API (стандартный SDK, Holo тема). Фактический запуск на физическом устройстве с API 17 не выполнялся и не является фактически проверенным.

### 23. Полученные скриншоты
Сохранены в `artifacts/screenshots/`:
- `01_main_screen.png` — Главный экран до загрузки данных.
- `01_main_screen_demo_loaded.png` — Главный экран после загрузки 5 разделов и 10 товаров.
- `02_operator_screen.png` — Экран оператора со схемой и кнопками управления.
- `03_operator_map_with_markers.png` — Схема магазина с метками отделов.
- `04_product_import.png` — Экран импорта товаров с предпросмотром данных.
- `05_customer_search_prefix.png` — Поиск товара покупателем по префиксу.
- `06_customer_search_selected_section.png` — Выбранный товар и подсвеченная метка на схеме.
- `07_shopping_list_results.png` — Результаты обработки списка покупок.
- `08_portrait_orientation.png` — Интерфейс в портретной ориентации.
- `09_landscape_orientation.png` — Интерфейс в альбомной ориентации.

### 24. Известные ограничения
- Фактический запуск и тестирование в реальном времени выполнены на эмуляторе Android 14 (API 34). Поддержка API 17 обеспечена на уровне исходного кода, манифеста (`minSdkVersion 17`) и применяемых API, однако не является фактически проверенной на физическом устройстве.
- Поддерживается импорт простых CSV/TXT без сложных многострочных кавычек.

### 25. Направления возможного развития
- Поддержка графа переходов между отделами и расчет маршрута покупателя.
- Сканирование штрихкодов камерой.
- Синхронизация каталога по Wi-Fi.

### 26. Структура документации
- `docs/index.md` — Введение и описание приложения.
- `docs/installation.md` — Руководство по сборке и развертыванию.
- `docs/operator-guide.md` — Инструкция оператора магазина.
- `docs/customer-guide.md` — Инструкция покупателя.
- `docs/testing.md` — Методика и результаты тестирования.
- `docs/limitations.md` — Границы MVP и архитектурные решения.

### 27. Результат сборки статического сайта
- Сборка документации выполнена с помощью MkDocs 1.6.1 (`uv tool run mkdocs build --clean`).
- Статический сайт успешно скомпилирован в каталог `site/` за 0.07 секунды.

### 28. Список Git-коммитов
1. `d1ad622` — Initial Android project setup
2. `068e961` — Add local data storage and demo catalog
3. `d479ef6` — Implement core domain services and automated tests
4. `5d70879` — Implement store map and section markers
5. `5bdcbc9` — Add product import functionality
6. `510e404` — Implement product search and map highlighting
7. `c8b248a` — Add clipboard shopping list processing
8. `7b1a089` — Add compatibility fixes and QA verification

### 29. Список реально использованных официальных источников
- Android Developers: Guides — SQLiteOpenHelper, Database Design and Architecture (https://developer.android.com/training/data-storage/sqlite)
- Android Developers: Reference — android.content.ClipboardManager (https://developer.android.com/reference/android/content/ClipboardManager)
- Android Developers: Documentation — Supporting different screen densities and orientations (https://developer.android.com/training/multiscreen/screensizes)
- Android Developers: Documentation — Manage Tasks and Activities (https://developer.android.com/guide/components/activities)
- Android Developers: Build System — Android Gradle Plugin 8.11 Release Notes (https://developer.android.com/build/releases/gradle-plugin)
- JUnit 4 Documentation — Getting Started with Unit Testing (https://junit.org/junit4/)
- MkDocs User Guide — Project Configuration and Themes (https://www.mkdocs.org/)
