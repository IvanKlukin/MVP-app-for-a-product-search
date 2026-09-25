package ru.study.storesearch.domain;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import ru.study.storesearch.data.model.Product;

import static org.junit.Assert.*;

public class ProductSearchServiceTest {

    private ProductSearchService searchService;
    private List<Product> catalog;

    @Before
    public void setUp() {
        searchService = new ProductSearchService();
        catalog = Arrays.asList(
                new Product(1, "Молоко", SearchNormalizer.normalize("Молоко"), 1, "Молочные продукты"),
                new Product(2, "Молоко топленое", SearchNormalizer.normalize("Молоко топленое"), 1, "Молочные продукты"),
                new Product(3, "Кефир", SearchNormalizer.normalize("Кефир"), 1, "Молочные продукты"),
                new Product(4, "Батон нарезной", SearchNormalizer.normalize("Батон нарезной"), 2, "Хлеб"),
                new Product(5, "Бананы", SearchNormalizer.normalize("Бананы"), 3, "Овощи и фрукты")
        );
    }

    @Test
    public void testSearchExactMatchPrioritized() {
        // Поиск по "Молоко" должен вернуть точное совпадение первым, а "Молоко топленое" — вторым
        List<Product> results = searchService.search(catalog, "Молоко");
        assertEquals(2, results.size());
        assertEquals("Молоко", results.get(0).getName());
        assertEquals("Молоко топленое", results.get(1).getName());
    }

    @Test
    public void testSearchByPrefixLetters() {
        // Поиск по начальным буквам "ба" должен найти "Бананы" и "Батон нарезной"
        List<Product> results = searchService.search(catalog, "ба");
        assertEquals(2, results.size());
        assertTrue(results.get(0).getName().startsWith("Ба"));
        assertTrue(results.get(1).getName().startsWith("Ба"));
    }

    @Test
    public void testSearchMissingProduct() {
        List<Product> results = searchService.search(catalog, "Шоколад");
        assertNotNull(results);
        assertTrue(results.isEmpty());

        Product exact = searchService.findExactMatch(catalog, "Шоколад");
        assertNull(exact);
    }

    @Test
    public void testSearchEmptyQuery() {
        List<Product> results = searchService.search(catalog, "   ");
        assertNotNull(results);
        assertTrue(results.isEmpty());

        results = searchService.search(catalog, null);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}
