package ru.study.storesearch.domain;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import ru.study.storesearch.data.model.Product;

import static org.junit.Assert.*;

public class ShoppingListParserTest {

    private ShoppingListParser parser;
    private ProductSearchService searchService;
    private List<Product> catalog;

    @Before
    public void setUp() {
        parser = new ShoppingListParser();
        searchService = new ProductSearchService();
        catalog = Arrays.asList(
                new Product(1, "Молоко", SearchNormalizer.normalize("Молоко"), 1, "Молочные продукты"),
                new Product(2, "Хлеб", SearchNormalizer.normalize("Хлеб"), 2, "Хлеб"),
                new Product(3, "Яблоки", SearchNormalizer.normalize("Яблоки"), 3, "Овощи и фрукты")
        );
    }

    @Test
    public void testParseLinesWithWindowsAndUnixNewlines() {
        // Проверка смешанных переводов строк CRLF и LF
        String input = "Молоко\r\nХлеб\nЯблоки";
        List<String> lines = parser.parseLines(input);
        assertEquals(3, lines.size());
        assertEquals("Молоко", lines.get(0));
        assertEquals("Хлеб", lines.get(1));
        assertEquals("Яблоки", lines.get(2));
    }

    @Test
    public void testSkipEmptyAndBlankLines() {
        String input = "  \nМолоко\n\n   \r\nХлеб\n   ";
        List<String> lines = parser.parseLines(input);
        assertEquals(2, lines.size());
        assertEquals("Молоко", lines.get(0));
        assertEquals("Хлеб", lines.get(1));
    }

    @Test
    public void testStripListMarkers() {
        String input = "1. Молоко\n2) Хлеб\n- Яблоки\n* Сыр\n• Вода";
        List<String> lines = parser.parseLines(input);
        assertEquals(5, lines.size());
        assertEquals("Молоко", lines.get(0));
        assertEquals("Хлеб", lines.get(1));
        assertEquals("Яблоки", lines.get(2));
        assertEquals("Сыр", lines.get(3));
        assertEquals("Вода", lines.get(4));
    }

    @Test
    public void testProcessShoppingListFoundAndNotFound() {
        String input = "1. Молоко\n2. Печенье\n3. Хлеб";
        List<ShoppingListParser.ShoppingItemResult> results =
                parser.processShoppingList(input, catalog, searchService);

        assertEquals(3, results.size());

        // Молоко найдено в Молочные продукты
        assertTrue(results.get(0).isFound());
        assertEquals("Молочные продукты", results.get(0).getSectionName());
        assertEquals("Молоко — Молочные продукты", results.get(0).getDisplayStatus());

        // Печенье отсутствует в каталоге
        assertFalse(results.get(1).isFound());
        assertNull(results.get(1).getSectionName());
        assertEquals("Печенье — Не найден", results.get(1).getDisplayStatus());

        // Хлеб найден в Хлеб
        assertTrue(results.get(2).isFound());
        assertEquals("Хлеб", results.get(2).getSectionName());
    }
}
