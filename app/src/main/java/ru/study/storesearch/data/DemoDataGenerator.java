package ru.study.storesearch.data;

import java.util.LinkedHashMap;
import java.util.Map;

import ru.study.storesearch.data.model.Section;
import ru.study.storesearch.domain.SearchNormalizer;

/**
 * Генератор демонстрационных данных каталога магазина.
 */
public final class DemoDataGenerator {

    private DemoDataGenerator() {
    }

    /**
     * Загружает стандартный демонстрационный набор разделов и товаров.
     * Не перезаписывает существующие данные без предварительной очистки.
     *
     * @param repository Репозиторий данных
     * @param overwrite  Если true — предварительно очищает таблицы
     * @return Количество добавленных товаров
     */
    public static int populateDemoData(StoreRepository repository, boolean overwrite) {
        if (overwrite) {
            repository.clearAllData();
            repository.clearCustomMapPath();
        } else if (repository.getSectionCount() > 0) {
            // Если данные уже есть и перезапись не запрошена, пропускаем
            return 0;
        }

        // 1. Создание 5 стандартных разделов с относительными координатами (0.0 .. 1.0)
        // Соответствуют нейтральной схеме магазина:
        // Овощи/фрукты — верхний левый блок
        // Молочные продукты — верхний правый блок
        // Хлеб — нижний левый блок
        // Напитки — нижний правый блок
        // Бытовая химия — центральный блок
        Map<String, float[]> sectionCoords = new LinkedHashMap<>();
        sectionCoords.put("Овощи и фрукты", new float[]{0.22f, 0.23f});
        sectionCoords.put("Молочные продукты", new float[]{0.78f, 0.23f});
        sectionCoords.put("Хлеб", new float[]{0.22f, 0.67f});
        sectionCoords.put("Напитки", new float[]{0.78f, 0.67f});
        sectionCoords.put("Бытовая химия", new float[]{0.50f, 0.48f});

        Map<String, Long> sectionIds = new LinkedHashMap<>();
        for (Map.Entry<String, float[]> entry : sectionCoords.entrySet()) {
            String name = entry.getKey();
            float[] coords = entry.getValue();
            Section existing = repository.getSectionByName(name);
            long id;
            if (existing != null) {
                id = existing.getId();
            } else {
                id = repository.insertSection(name, coords[0], coords[1]);
            }
            sectionIds.put(name, id);
        }

        // 2. Создание 10 стандартных товаров с привязкой к разделам
        String[][] demoProducts = new String[][]{
                {"Яблоки", "Овощи и фрукты"},
                {"Бананы", "Овощи и фрукты"},
                {"Молоко", "Молочные продукты"},
                {"Кефир", "Молочные продукты"},
                {"Батон", "Хлеб"},
                {"Хлеб ржаной", "Хлеб"},
                {"Вода", "Напитки"},
                {"Сок", "Напитки"},
                {"Мыло", "Бытовая химия"},
                {"Стиральный порошок", "Бытовая химия"}
        };

        int insertedCount = 0;
        for (String[] item : demoProducts) {
            String productName = item[0];
            String sectionName = item[1];
            Long sectionId = sectionIds.get(sectionName);
            if (sectionId != null && sectionId > 0) {
                String normalized = SearchNormalizer.normalize(productName);
                long result = repository.upsertProduct(productName, normalized, sectionId);
                if (result > 0) {
                    insertedCount++;
                }
            }
        }

        return insertedCount;
    }
}
