package ru.study.storesearch.domain;

import java.util.Locale;

/**
 * Утилита нормализации текстовых строк для поиска и хранения.
 * Не имеет зависимостей от Android SDK и легко тестируется в JUnit 4.
 */
public final class SearchNormalizer {

    private static final Locale RUSSIAN_LOCALE = new Locale("ru", "RU");

    private SearchNormalizer() {
        // Утилитный класс
    }

    /**
     * Нормализует строку: удаляет краевые пробелы, приводит к нижнему регистру с русской локалью,
     * заменяет букву 'ё' на 'е' и схлопывает повторяющиеся пробелы.
     *
     * @param input Исходная строка
     * @return Нормализованная строка или пустая строка, если input равен null или пуст
     */
    public static String normalize(String input) {
        if (input == null) {
            return "";
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        // Замена 'ё' на 'е' для обеспечения нечувствительности поиска к диакритике
        String replaced = trimmed.replace('ё', 'е').replace('Ё', 'Е');
        // Приведение к нижнему регистру с учётом русской локали
        String lower = replaced.toLowerCase(RUSSIAN_LOCALE);
        // Схлопывание повторяющихся пробельных символов
        return lower.replaceAll("\\s+", " ");
    }

    /**
     * Проверяет, является ли строка пустой после нормализации.
     */
    public static boolean isBlank(String input) {
        return normalize(input).isEmpty();
    }
}
