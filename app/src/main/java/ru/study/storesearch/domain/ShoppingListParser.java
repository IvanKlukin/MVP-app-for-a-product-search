package ru.study.storesearch.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import ru.study.storesearch.data.model.Product;

/**
 * Парсер строк списка покупок (из буфера обмена или поля ввода).
 * Безопасно очищает маркеры списка и сопоставляет позиции с каталогом товаров.
 */
public class ShoppingListParser {

    // Шаблон для безопасного удаления маркеров списков:
    // "1. Товар", "2) Товар", "- Товар", "* Товар", "+ Товар", "• Товар"
    private static final Pattern LIST_MARKER_PATTERN =
            Pattern.compile("^(\\d+[.)]\\s*|[-*+•]\\s*)");

    public static class ShoppingItemResult {
        private final String originalQuery;
        private final String cleanedQuery;
        private final boolean found;
        private final String matchedProductName;
        private final String sectionName;

        public ShoppingItemResult(String originalQuery, String cleanedQuery, boolean found,
                                  String matchedProductName, String sectionName) {
            this.originalQuery = originalQuery;
            this.cleanedQuery = cleanedQuery;
            this.found = found;
            this.matchedProductName = matchedProductName;
            this.sectionName = sectionName;
        }

        public String getOriginalQuery() {
            return originalQuery;
        }

        public String getCleanedQuery() {
            return cleanedQuery;
        }

        public boolean isFound() {
            return found;
        }

        public String getMatchedProductName() {
            return matchedProductName;
        }

        public String getSectionName() {
            return sectionName;
        }

        public String getDisplayStatus() {
            if (found) {
                return (matchedProductName != null ? matchedProductName : cleanedQuery) +
                        " — " + (sectionName != null ? sectionName : "");
            } else {
                return cleanedQuery + " — Не найден";
            }
        }

        @Override
        public String toString() {
            return getDisplayStatus();
        }
    }

    /**
     * Очищает строку от краевых пробелов и стандартных маркеров списков.
     */
    public String cleanLine(String line) {
        if (line == null) {
            return "";
        }
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        // Удаляем маркеры вроде "1. ", "- ", "• "
        String stripped = LIST_MARKER_PATTERN.matcher(trimmed).replaceFirst("").trim();
        return stripped;
    }

    /**
     * Разбирает произвольный многострочный текст на отдельные очищенные позиции.
     * Пропускает пустые строки.
     */
    public List<String> parseLines(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        // Разделение по Windows (\r\n) и Unix (\n)
        String[] rawLines = text.split("\\r?\\n");
        List<String> result = new ArrayList<>();

        for (String raw : rawLines) {
            String cleaned = cleanLine(raw);
            if (!cleaned.isEmpty()) {
                result.add(cleaned);
            }
        }
        return result;
    }

    /**
     * Сопоставляет каждую непустую строку из текста со списком товаров каталога.
     *
     * @param text     Многострочный текст (из буфера или поля ввода)
     * @param products Доступный каталог товаров магазина
     * @return Список результатов с информацией о статусе нахождения и названии раздела
     */
    public List<ShoppingItemResult> processShoppingList(String text, List<Product> products, ProductSearchService searchService) {
        List<String> lines = parseLines(text);
        if (lines.isEmpty()) {
            return Collections.emptyList();
        }

        List<ShoppingItemResult> results = new ArrayList<>(lines.size());
        for (String query : lines) {
            Product exact = searchService.findExactMatch(products, query);
            if (exact != null) {
                results.add(new ShoppingItemResult(query, query, true, exact.getName(), exact.getSectionName()));
            } else {
                // Если точного совпадения нет, проверяем поиск по префиксу
                List<Product> matches = searchService.search(products, query);
                if (!matches.isEmpty()) {
                    Product bestMatch = matches.get(0);
                    results.add(new ShoppingItemResult(query, query, true, bestMatch.getName(), bestMatch.getSectionName()));
                } else {
                    results.add(new ShoppingItemResult(query, query, false, null, null));
                }
            }
        }
        return results;
    }
}
