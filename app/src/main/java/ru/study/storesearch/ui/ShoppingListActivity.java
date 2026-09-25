package ru.study.storesearch.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ru.study.storesearch.R;
import ru.study.storesearch.data.StoreRepository;
import ru.study.storesearch.data.model.Product;
import ru.study.storesearch.domain.ProductSearchService;
import ru.study.storesearch.domain.ShoppingListParser;

/**
 * Экран пакетного поиска товаров по списку покупок из буфера обмена.
 * Получает текст из ClipboardManager, разбирает строки с удалением маркеров,
 * выполняет поиск и отображает сопоставление товаров с разделами магазина.
 */
public class ShoppingListActivity extends Activity {

    private StoreRepository repository;
    private ShoppingListParser shoppingParser;
    private ProductSearchService searchService;

    private EditText etShoppingListInput;
    private TextView tvShoppingSummary;
    private ListView lvShoppingResults;

    private ShoppingResultsAdapter resultsAdapter;
    private List<Product> allProducts = new ArrayList<Product>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        repository = new StoreRepository(this);
        shoppingParser = new ShoppingListParser();
        searchService = new ProductSearchService();

        etShoppingListInput = (EditText) findViewById(R.id.etShoppingListInput);
        tvShoppingSummary = (TextView) findViewById(R.id.tvShoppingSummary);
        lvShoppingResults = (ListView) findViewById(R.id.lvShoppingResults);

        Button btnPasteClipboard = (Button) findViewById(R.id.btnPasteClipboard);
        Button btnProcessList = (Button) findViewById(R.id.btnProcessList);

        resultsAdapter = new ShoppingResultsAdapter(this, new ArrayList<ShoppingListParser.ShoppingItemResult>());
        lvShoppingResults.setAdapter(resultsAdapter);

        // Вставка текста из буфера обмена
        btnPasteClipboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pasteFromClipboard();
            }
        });

        // Запуск сопоставления списка
        btnProcessList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processShoppingList();
            }
        });

        allProducts = repository.getAllProducts();

        String listExtra = getIntent().getStringExtra("shopping_list");
        if (listExtra != null && !listExtra.trim().isEmpty()) {
            etShoppingListInput.setText(listExtra);
            processShoppingList();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        allProducts = repository.getAllProducts();
        String listExtra = intent.getStringExtra("shopping_list");
        if (listExtra != null && !listExtra.trim().isEmpty()) {
            etShoppingListInput.setText(listExtra);
            processShoppingList();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        allProducts = repository.getAllProducts();
    }

    private void pasteFromClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null || !clipboard.hasPrimaryClip()) {
            Toast.makeText(this, R.string.clipboard_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        ClipData clip = clipboard.getPrimaryClip();
        if (clip == null || clip.getItemCount() == 0) {
            Toast.makeText(this, R.string.clipboard_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        CharSequence clipText = clip.getItemAt(0).getText();
        if (clipText == null || clipText.toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.clipboard_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        etShoppingListInput.setText(clipText.toString());
        Toast.makeText(this, R.string.shopping_list_pasted, Toast.LENGTH_SHORT).show();

        // Автоматически запускаем поиск по вставленному списку
        processShoppingList();
    }

    private void processShoppingList() {
        String input = etShoppingListInput.getText().toString();
        if (input.trim().isEmpty()) {
            Toast.makeText(this, R.string.shopping_list_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        List<ShoppingListParser.ShoppingItemResult> results =
                shoppingParser.processShoppingList(input, allProducts, searchService);

        if (results.isEmpty()) {
            Toast.makeText(this, R.string.shopping_list_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        int foundCount = 0;
        int notFoundCount = 0;
        for (ShoppingListParser.ShoppingItemResult item : results) {
            if (item.isFound()) {
                foundCount++;
            } else {
                notFoundCount++;
            }
        }

        tvShoppingSummary.setVisibility(View.VISIBLE);
        tvShoppingSummary.setText(getString(R.string.shopping_summary_format, results.size(), foundCount, notFoundCount));

        resultsAdapter.clear();
        for (ShoppingListParser.ShoppingItemResult res : results) {
            resultsAdapter.add(res);
        }
        resultsAdapter.notifyDataSetChanged();
    }

    private static class ShoppingResultsAdapter extends ArrayAdapter<ShoppingListParser.ShoppingItemResult> {
        public ShoppingResultsAdapter(Context context, List<ShoppingListParser.ShoppingItemResult> items) {
            super(context, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_shopping_result, parent, false);
            }
            ShoppingListParser.ShoppingItemResult item = getItem(position);
            if (item != null) {
                TextView tvName = (TextView) convertView.findViewById(R.id.tvShoppingItemName);
                TextView tvStatus = (TextView) convertView.findViewById(R.id.tvShoppingItemStatus);

                tvName.setText(item.getCleanedQuery());

                if (item.isFound()) {
                    tvStatus.setText(getContext().getString(R.string.shopping_item_section, item.getSectionName()));
                    tvStatus.setTextColor(getContext().getResources().getColor(R.color.status_found));
                } else {
                    tvStatus.setText(R.string.status_not_found);
                    tvStatus.setTextColor(getContext().getResources().getColor(R.color.status_not_found));
                }
            }
            return convertView;
        }
    }
}
