package ru.study.storesearch.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ru.study.storesearch.R;
import ru.study.storesearch.data.StoreRepository;
import ru.study.storesearch.data.model.Product;
import ru.study.storesearch.data.model.Section;
import ru.study.storesearch.domain.BitmapUtils;
import ru.study.storesearch.domain.ProductSearchService;
import ru.study.storesearch.domain.SearchNormalizer;
import ru.study.storesearch.ui.view.StoreMapView;

/**
 * Экран поиска товаров для покупателя.
 * Реализует поиск по первым буквам, отображение подсказок,
 * вывод названия найденного раздела и визуальное статическое выделение метки на схеме.
 */
public class SearchActivity extends Activity {

    private StoreRepository repository;
    private ProductSearchService searchService;

    private EditText etSearchQuery;
    private TextView tvSearchStatus;
    private ListView lvSuggestions;
    private StoreMapView storeMapViewSearch;

    private List<Product> allProducts = new ArrayList<Product>();
    private SuggestionsAdapter suggestionsAdapter;
    private Bitmap mapBitmap;
    private boolean isInternalTextUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        repository = new StoreRepository(this);
        searchService = new ProductSearchService();

        etSearchQuery = (EditText) findViewById(R.id.etSearchQuery);
        tvSearchStatus = (TextView) findViewById(R.id.tvSearchStatus);
        lvSuggestions = (ListView) findViewById(R.id.lvSuggestions);
        storeMapViewSearch = (StoreMapView) findViewById(R.id.storeMapViewSearch);
        Button btnGoShoppingList = (Button) findViewById(R.id.btnGoShoppingList);

        // На схеме для покупателя отключен режим добавления меток
        storeMapViewSearch.setInteractive(false);

        suggestionsAdapter = new SuggestionsAdapter(this, new ArrayList<Product>());
        lvSuggestions.setAdapter(suggestionsAdapter);

        // Выбор подсказки из списка
        lvSuggestions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Product selected = suggestionsAdapter.getItem(position);
                if (selected != null) {
                    selectProduct(selected);
                }
            }
        });

        // Отслеживание ввода текста для поиска по начальным буквам
        etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isInternalTextUpdate) return;
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Переход к поиску по списку из буфера
        btnGoShoppingList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchActivity.this, ShoppingListActivity.class);
                startActivity(intent);
            }
        });

        loadStoreMap();
    }

    @Override
    protected void onResume() {
        super.onResume();
        allProducts = repository.getAllProducts();
        List<Section> sections = repository.getAllSections();
        storeMapViewSearch.setSections(sections);

        String currentQuery = etSearchQuery.getText().toString();
        if (!SearchNormalizer.isBlank(currentQuery)) {
            performSearch(currentQuery);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapBitmap != null && !mapBitmap.isRecycled()) {
            mapBitmap.recycle();
            mapBitmap = null;
        }
    }

    private void loadStoreMap() {
        String customPath = repository.getCustomMapPath();
        if (customPath != null && new File(customPath).exists()) {
            mapBitmap = BitmapUtils.decodeSampledBitmapFromFile(customPath, 1200, 1200);
        }
        if (mapBitmap == null) {
            mapBitmap = BitmapUtils.decodeSampledBitmapFromResource(
                    getResources(), R.drawable.default_store_map, 1200, 1200);
        }
        storeMapViewSearch.setMapBitmap(mapBitmap);
    }

    private void performSearch(String query) {
        if (SearchNormalizer.isBlank(query)) {
            suggestionsAdapter.clear();
            lvSuggestions.setVisibility(View.GONE);
            storeMapViewSearch.setHighlightedSectionId(null);
            tvSearchStatus.setText(R.string.search_prompt);
            return;
        }

        List<Product> matches = searchService.search(allProducts, query);

        if (matches.isEmpty()) {
            suggestionsAdapter.clear();
            lvSuggestions.setVisibility(View.GONE);
            storeMapViewSearch.setHighlightedSectionId(null);
            tvSearchStatus.setText(R.string.product_not_found);
            tvSearchStatus.setTextColor(getResources().getColor(R.color.status_not_found));
        } else {
            suggestionsAdapter.clear();
            for (Product p : matches) {
                suggestionsAdapter.add(p);
            }
            suggestionsAdapter.notifyDataSetChanged();
            lvSuggestions.setVisibility(View.VISIBLE);

            // Если есть точное совпадение с введенным текстом, сразу выделяем его раздел
            String normQuery = SearchNormalizer.normalize(query);
            Product exact = null;
            for (Product p : matches) {
                if (normQuery.equals(p.getNormalizedName())) {
                    exact = p;
                    break;
                }
            }

            if (exact != null) {
                highlightProductOnMap(exact);
            } else {
                tvSearchStatus.setText("Найдено вариантов: " + matches.size() + ". Выберите подходящий товар:");
                tvSearchStatus.setTextColor(getResources().getColor(R.color.text_primary));
                // Подсвечиваем первый найденный вариант
                storeMapViewSearch.setHighlightedSectionId(matches.get(0).getSectionId());
            }
        }
    }

    private void selectProduct(Product product) {
        isInternalTextUpdate = true;
        etSearchQuery.setText(product.getName());
        etSearchQuery.setSelection(product.getName().length());
        isInternalTextUpdate = false;

        lvSuggestions.setVisibility(View.GONE);
        highlightProductOnMap(product);
    }

    private void highlightProductOnMap(Product product) {
        storeMapViewSearch.setHighlightedSectionId(product.getSectionId());
        String info = "Товар: " + product.getName() + "\nРаздел: " + product.getSectionName();
        tvSearchStatus.setText(info);
        tvSearchStatus.setTextColor(getResources().getColor(R.color.primary_dark));
    }

    private static class SuggestionsAdapter extends ArrayAdapter<Product> {
        public SuggestionsAdapter(Context context, List<Product> items) {
            super(context, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_product_suggestion, parent, false);
            }
            Product item = getItem(position);
            if (item != null) {
                TextView tvName = (TextView) convertView.findViewById(R.id.tvItemProductName);
                TextView tvSection = (TextView) convertView.findViewById(R.id.tvItemSectionName);
                tvName.setText(item.getName());
                tvSection.setText(item.getSectionName());
            }
            return convertView;
        }
    }
}
