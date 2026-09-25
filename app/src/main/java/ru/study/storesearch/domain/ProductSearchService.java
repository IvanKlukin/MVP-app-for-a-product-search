package ru.study.storesearch.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ru.study.storesearch.data.model.Product;

/**
 * Сервис поиска товаров в каталоге.
 * Реализует поиск по префиксу с приоритетом точных совпадений.
 * Не имеет зависимостей от Android UI и тестируется на чистой JVM.
 */
public class ProductSearchService {

    /**
     * Выполняет поиск по списку товаров по строке запроса.
     * Точные совпадения выводятся в начале списка, затем следуют совпадения по префиксу.
     *
     * @param products Список товаров для поиска
     * @param query    Поисковый запрос
     * @return Список найденных товаров (пустой при отсутствии совпадений или пустом запросе)
     */
    public List<Product> search(List<Product> products, String query) {
        if (products == null || products.isEmpty() || SearchNormalizer.isBlank(query)) {
            return Collections.emptyList();
        }

        String normalizedQuery = SearchNormalizer.normalize(query);
        List<Product> exactMatches = new ArrayList<>();
        List<Product> prefixMatches = new ArrayList<>();

        for (Product product : products) {
            if (product == null || product.getNormalizedName() == null) {
                continue;
            }
            String normName = product.getNormalizedName();
            if (normName.equals(normalizedQuery)) {
                exactMatches.add(product);
            } else if (normName.startsWith(normalizedQuery)) {
                prefixMatches.add(product);
            }
        }

        // Сортировка совпадений по префиксу в алфавитном порядке
        Collections.sort(prefixMatches, new Comparator<Product>() {
            @Override
            public int compare(Product p1, Product p2) {
                return p1.getName().compareToIgnoreCase(p2.getName());
            }
        });

        List<Product> results = new ArrayList<>(exactMatches.size() + prefixMatches.size());
        results.addAll(exactMatches);
        results.addAll(prefixMatches);
        return results;
    }

    /**
     * Поиск первого точного совпадения по названию товара.
     */
    public Product findExactMatch(List<Product> products, String query) {
        if (products == null || products.isEmpty() || SearchNormalizer.isBlank(query)) {
            return null;
        }
        String normalizedQuery = SearchNormalizer.normalize(query);
        for (Product product : products) {
            if (product != null && normalizedQuery.equals(product.getNormalizedName())) {
                return product;
            }
        }
        return null;
    }
}
