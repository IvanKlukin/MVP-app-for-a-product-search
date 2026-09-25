package ru.study.storesearch.domain;

import org.junit.Before;
import org.junit.Test;

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
}
