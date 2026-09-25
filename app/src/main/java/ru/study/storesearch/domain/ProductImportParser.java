package ru.study.storesearch.domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Парсер строк импорта товаров вида:
 * "Название товара;Название раздела"
 * Поддерживает UTF-8, UTF-8 с BOM, переводы строк \r\n и \n.
 * Совместим с API 17 (использует Charset.forName("UTF-8")).
 */
public class ProductImportParser {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public static class ParsedItem {
        private final int lineNumber;
        private final String productName;
        private final String sectionName;

        public ParsedItem(int lineNumber, String productName, String sectionName) {
            this.lineNumber = lineNumber;
            this.productName = productName;
            this.sectionName = sectionName;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public String getProductName() {
            return productName;
        }

        public String getSectionName() {
            return sectionName;
        }
    }

    public static class ImportError {
        private final int lineNumber;
        private final String rawLine;
        private final String message;

        public ImportError(int lineNumber, String rawLine, String message) {
            this.lineNumber = lineNumber;
            this.rawLine = rawLine;
            this.message = message;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public String getRawLine() {
            return rawLine;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class ParseResult {
        private final List<ParsedItem> items;
        private final List<ImportError> errors;
        private final int totalLines;

        public ParseResult(List<ParsedItem> items, List<ImportError> errors, int totalLines) {
            this.items = items;
            this.errors = errors;
            this.totalLines = totalLines;
        }

        public List<ParsedItem> getItems() {
            return items;
        }

        public List<ImportError> getErrors() {
            return errors;
        }

        public int getTotalLines() {
            return totalLines;
        }

        public int getValidCount() {
            return items.size();
        }

        public int getSkippedCount() {
            return errors.size();
        }
    }

    /**
     * Считывает весь исходный текст из InputStream в кодировке UTF-8,
     * сохраняя все строки (включая некорректные) для предпросмотра пользователю.
     */
    public String readRawText(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        boolean first = true;
        while ((line = reader.readLine()) != null) {
            if (first) {
                // Если присутствует BOM, удаляем его из отображаемого текста
                if (line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }
                first = false;
            } else {
                sb.append("\n");
            }
            sb.append(line);
        }
        return sb.toString();
    }

    /**
     * Разбирает текст со списком товаров для импорта.
     */
    public ParseResult parse(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ParseResult(Collections.<ParsedItem>emptyList(), Collections.<ImportError>emptyList(), 0);
        }

        // Удаление метки порядка байтов (UTF-8 BOM), если она присутствует в начале текста
        String cleanText = text;
        if (cleanText.startsWith("\uFEFF")) {
            cleanText = cleanText.substring(1);
        }

        // Разделение по стандартным переводам строк Windows (\r\n) и Unix (\n), с поддержкой экранированных \n
        String normalized = cleanText.contains("\n") ? cleanText : cleanText.replace("\\n", "\n");
        String[] lines = normalized.split("\\r?\\n");
        List<ParsedItem> items = new ArrayList<>();
        List<ImportError> errors = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            int lineNumber = i + 1;
            String rawLine = lines[i];
            String trimmed = rawLine.trim();

            if (trimmed.isEmpty()) {
                // Пустые строки безопасно пропускаются
                continue;
            }

            // Разделение по точке с запятой (требуется ровно два непустых поля: Товар;Отдел)
            String[] parts = trimmed.split(";", -1);
            if (parts.length < 2) {
                errors.add(new ImportError(lineNumber, rawLine, "Отсутствует разделитель ';' (ожидается ровно два поля 'Товар;Отдел')"));
                continue;
            }
            if (parts.length > 2) {
                errors.add(new ImportError(lineNumber, rawLine, "Лишние разделители ';' (найдено полей: " + parts.length + ", ожидается ровно два 'Товар;Отдел')"));
                continue;
            }

            String productName = parts[0].trim();
            String sectionName = parts[1].trim();

            if (productName.isEmpty() && sectionName.isEmpty()) {
                errors.add(new ImportError(lineNumber, rawLine, "Пустое название товара и раздела"));
            } else if (productName.isEmpty()) {
                errors.add(new ImportError(lineNumber, rawLine, "Пустое название товара"));
            } else if (sectionName.isEmpty()) {
                errors.add(new ImportError(lineNumber, rawLine, "Пустое название раздела"));
            } else {
                items.add(new ParsedItem(lineNumber, productName, sectionName));
            }
        }

        return new ParseResult(items, errors, lines.length);
    }

    /**
     * Считывает и разбирает содержимое InputStream в кодировке UTF-8.
     */
    public ParseResult parse(InputStream inputStream) throws IOException {
        String text = readRawText(inputStream);
        return parse(text);
    }
}
