package ru.study.storesearch.domain;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class ProductImportParserTest {

    private ProductImportParser parser;

    @Before
    public void setUp() {
        parser = new ProductImportParser();
    }

    @Test
    public void testParseValidLines() {
        String input = "Молоко;Молочные продукты\r\nБатон;Хлеб\nВода;Напитки";
        ProductImportParser.ParseResult result = parser.parse(input);

        assertEquals(3, result.getValidCount());
        assertEquals(0, result.getSkippedCount());
        assertEquals("Молоко", result.getItems().get(0).getProductName());
        assertEquals("Молочные продукты", result.getItems().get(0).getSectionName());
        assertEquals("Батон", result.getItems().get(1).getProductName());
        assertEquals("Хлеб", result.getItems().get(1).getSectionName());
        assertEquals("Вода", result.getItems().get(2).getProductName());
        assertEquals("Напитки", result.getItems().get(2).getSectionName());
    }

    @Test
    public void testParseWithUtf8Bom() {
        // Проверка корректной обработки UTF-8 BOM (\uFEFF) в начале файла
        String input = "\uFEFFКефир;Молочные продукты\nЯблоки;Овощи и фрукты";
        ProductImportParser.ParseResult result = parser.parse(input);

        assertEquals(2, result.getValidCount());
        assertEquals(0, result.getSkippedCount());
        assertEquals("Кефир", result.getItems().get(0).getProductName());
    }

    @Test
    public void testParseInvalidFormatLines() {
        String input = "Молоко;Молочные продукты\n" +
                "Невалидная строка без разделителя\n" +
                ";Пустой товар\n" +
                "Товар без раздела;\n" +
                ";\n" +
                "Сок;Напитки";

        ProductImportParser.ParseResult result = parser.parse(input);

        assertEquals(2, result.getValidCount()); // "Молоко" и "Сок"
        assertEquals(4, result.getSkippedCount()); // 4 ошибочные строки
        assertEquals(6, result.getTotalLines());

        // Проверяем, что ошибки зафиксированы с номерами строк
        assertEquals(2, result.getErrors().get(0).getLineNumber());
        assertEquals(3, result.getErrors().get(1).getLineNumber());
        assertEquals(4, result.getErrors().get(2).getLineNumber());
        assertEquals(5, result.getErrors().get(3).getLineNumber());
    }

    @Test
    public void testSkipEmptyLinesInImport() {
        String input = "\n   \nМолоко;Молочные продукты\n\n\r\n";
        ProductImportParser.ParseResult result = parser.parse(input);

        assertEquals(1, result.getValidCount());
        assertEquals(0, result.getSkippedCount());
        assertEquals("Молоко", result.getItems().get(0).getProductName());
    }

    @Test
    public void testParseLineWithExtraFields() {
        // Строки с лишними разделителями должны отклоняться как ошибочные
        String input = "Молоко;Молочные продукты;100 руб;Склад 1\n" +
                "Хлеб;Хлебобулочные;Лишнее поле\n" +
                "Вода;Напитки";

        ProductImportParser.ParseResult result = parser.parse(input);

        assertEquals(1, result.getValidCount()); // Только "Вода;Напитки"
        assertEquals(2, result.getSkippedCount()); // Две строки с лишними разделителями
        assertEquals(3, result.getTotalLines());

        ProductImportParser.ImportError err1 = result.getErrors().get(0);
        assertEquals(1, err1.getLineNumber());
        assertTrue(err1.getMessage().contains("Лишние разделители"));

        ProductImportParser.ImportError err2 = result.getErrors().get(1);
        assertEquals(2, err2.getLineNumber());
        assertTrue(err2.getMessage().contains("Лишние разделители"));

        assertEquals("Вода", result.getItems().get(0).getProductName());
        assertEquals("Напитки", result.getItems().get(0).getSectionName());
    }

    @Test
    public void testParseMixedSetOfValidAndInvalidLines() {
        // Комплексный смешанный набор строк
        String input = "\uFEFFСыр;Молочные продукты\r\n" +
                "\r\n" + // пустая строка (пропускается)
                "Строка без точки с запятой\n" + // ошибка: нет разделителя (строка 3)
                "Колбаса;Мясной отдел;Лишний параметр\n" + // ошибка: лишний разделитель (строка 4)
                ";Только раздел\n" + // ошибка: пустое название товара (строка 5)
                "Только товар;\n" + // ошибка: пустое название раздела (строка 6)
                "   \n" + // пустая строка с пробелами (пропускается)
                "Чай;Бакалея\n" + // валидная строка (строка 8)
                "Кофе;Бакалея"; // валидная строка (строка 9)

        ProductImportParser.ParseResult result = parser.parse(input);

        assertEquals(3, result.getValidCount()); // Сыр, Чай, Кофе
        assertEquals(4, result.getSkippedCount()); // 4 ошибки
        assertEquals(9, result.getTotalLines());

        assertEquals("Сыр", result.getItems().get(0).getProductName());
        assertEquals("Чай", result.getItems().get(1).getProductName());
        assertEquals("Кофе", result.getItems().get(2).getProductName());

        assertEquals(3, result.getErrors().get(0).getLineNumber());
        assertEquals(4, result.getErrors().get(1).getLineNumber());
        assertEquals(5, result.getErrors().get(2).getLineNumber());
        assertEquals(6, result.getErrors().get(3).getLineNumber());
    }

    @Test
    public void testReadRawTextFromInputStream() throws IOException {
        String original = "\uFEFFСтрока 1;Раздел 1\r\nНевалидная строка\nСтрока 3;Раздел 3;Лишнее";
        ByteArrayInputStream bais = new ByteArrayInputStream(original.getBytes(Charset.forName("UTF-8")));

        String rawText = parser.readRawText(bais);

        // Проверяем, что исходный текст сохранен полностью для предпросмотра
        assertTrue(rawText.contains("Строка 1;Раздел 1"));
        assertTrue(rawText.contains("Невалидная строка"));
        assertTrue(rawText.contains("Строка 3;Раздел 3;Лишнее"));
        // BOM должен быть удален из начала отображаемого текста
        assertFalse(rawText.startsWith("\uFEFF"));
    }
}
