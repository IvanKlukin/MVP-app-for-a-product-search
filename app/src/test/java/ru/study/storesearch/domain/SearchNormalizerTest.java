package ru.study.storesearch.domain;

import org.junit.Test;
import static org.junit.Assert.*;

public class SearchNormalizerTest {

    @Test
    public void testNormalizeRussianTextAndTrim() {
        assertEquals("молоко пастеризованное", SearchNormalizer.normalize("   Молоко   Пастеризованное   "));
        assertEquals("хлеб", SearchNormalizer.normalize("ХЛЕБ"));
    }

    @Test
    public void testNormalizeLetterYo() {
        // Проверка унификации буквы "ё" в "е"
        assertEquals("свекла", SearchNormalizer.normalize("Свёкла"));
        assertEquals("желтый сыр", SearchNormalizer.normalize("ЖЁЛТЫЙ  СЫР"));
    }

    @Test
    public void testNormalizeEmptyAndNull() {
        assertEquals("", SearchNormalizer.normalize(null));
        assertEquals("", SearchNormalizer.normalize(""));
        assertEquals("", SearchNormalizer.normalize("   "));
        assertTrue(SearchNormalizer.isBlank(null));
        assertTrue(SearchNormalizer.isBlank("   "));
        assertFalse(SearchNormalizer.isBlank("Товар"));
    }
}
