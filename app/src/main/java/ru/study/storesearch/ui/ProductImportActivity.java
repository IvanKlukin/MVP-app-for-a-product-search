package ru.study.storesearch.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.List;

import ru.study.storesearch.R;
import ru.study.storesearch.data.StoreRepository;
import ru.study.storesearch.data.model.Section;
import ru.study.storesearch.domain.ProductImportParser;
import ru.study.storesearch.domain.SearchNormalizer;

/**
 * Экран пакетного импорта товаров из CSV/TXT файлов или ручного ввода.
 * Проверяет наличие указанного раздела в базе данных магазина.
 * Строки с несуществующим разделом пропускаются с выводом информативной ошибки.
 * При повторном импорте существующего товара его привязка обновляется (upsert).
 */
public class ProductImportActivity extends Activity {

    private static final int REQUEST_PICK_FILE = 201;

    private StoreRepository repository;
    private ProductImportParser importParser;

    private EditText etImportText;
    private TextView tvImportStats;
    private TextView tvImportErrors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        repository = new StoreRepository(this);
        importParser = new ProductImportParser();

        etImportText = (EditText) findViewById(R.id.etImportText);
        tvImportStats = (TextView) findViewById(R.id.tvImportStats);
        tvImportErrors = (TextView) findViewById(R.id.tvImportErrors);

        Button btnChooseFile = (Button) findViewById(R.id.btnChooseFile);
        Button btnPasteExample = (Button) findViewById(R.id.btnPasteExample);
        Button btnRunImport = (Button) findViewById(R.id.btnRunImport);

        btnChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImportFile();
            }
        });

        btnPasteExample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pasteExampleText();
            }
        });

        btnRunImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeImport();
            }
        });

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;
        String importText = intent.getStringExtra("import_text");
        if (importText != null) {
            etImportText.setText(importText);
            if (intent.getBooleanExtra("auto_import", false)) {
                executeImport();
            }
        }
    }

    private static final String TAG = "ProductImportActivity";

    private void chooseImportFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.btn_choose_file)), REQUEST_PICK_FILE);
        } catch (Exception e) {
            android.util.Log.e(TAG, "File picker not available", e);
            Toast.makeText(this, R.string.error_no_file_picker, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_FILE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                loadTextFromUri(fileUri);
            }
        }
    }

    private void loadTextFromUri(Uri uri) {
        InputStream is = null;
        try {
            is = getContentResolver().openInputStream(uri);
            if (is == null) {
                Toast.makeText(this, R.string.error_file_read, Toast.LENGTH_SHORT).show();
                return;
            }
            // Считываем и отображаем исходный текст файла целиком (включая все строки и возможные ошибки)
            String rawText = importParser.readRawText(is);
            etImportText.setText(rawText);
            Toast.makeText(this, R.string.import_file_loaded, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error reading import file", e);
            Toast.makeText(this, R.string.error_file_read, Toast.LENGTH_SHORT).show();
        } finally {
            try {
                if (is != null) is.close();
            } catch (Exception ignored) {
            }
        }
    }

    private void pasteExampleText() {
        String example = "Молоко;Молочные продукты\n" +
                "Кефир;Молочные продукты\n" +
                "Батон;Хлеб\n" +
                "Хлеб ржаной;Хлеб\n" +
                "Вода;Напитки\n" +
                "Сок;Напитки\n" +
                "Яблоки;Овощи и фрукты\n" +
                "Бананы;Овощи и фрукты\n" +
                "Мыло;Бытовая химия\n" +
                "Стиральный порошок;Бытовая химия";
        etImportText.setText(example);
    }

    private void executeImport() {
        String content = etImportText.getText().toString();
        if (content.trim().isEmpty()) {
            Toast.makeText(this, R.string.error_no_import_data, Toast.LENGTH_SHORT).show();
            return;
        }

        ProductImportParser.ParseResult parseResult = importParser.parse(content);
        List<ProductImportParser.ParsedItem> items = parseResult.getItems();
        List<ProductImportParser.ImportError> parseErrors = parseResult.getErrors();

        int importedCount = 0;
        int skippedCount = parseErrors.size();
        StringBuilder errorLog = new StringBuilder();

        // 1. Ошибки синтаксиса строк (сохраняются с номерами строк и понятными сообщениями)
        for (ProductImportParser.ImportError err : parseErrors) {
            errorLog.append(getString(R.string.import_error_line_format, err.getLineNumber(), err.getMessage()))
                    .append("\n");
        }

        // 2. Валидация разделов и сохранение товаров
        for (ProductImportParser.ParsedItem item : items) {
            Section section = repository.getSectionByName(item.getSectionName());
            if (section == null) {
                skippedCount++;
                errorLog.append(getString(R.string.error_unknown_section, item.getSectionName(), item.getLineNumber()))
                        .append("\n");
            } else {
                String normalized = SearchNormalizer.normalize(item.getProductName());
                long id = repository.upsertProduct(item.getProductName(), normalized, section.getId());
                if (id > 0) {
                    importedCount++;
                } else {
                    skippedCount++;
                    errorLog.append(getString(R.string.import_error_db_write_format, item.getProductName(), item.getLineNumber()))
                            .append("\n");
                }
            }
        }

        // 3. Вывод результатов
        tvImportStats.setVisibility(View.VISIBLE);
        tvImportStats.setText(getString(R.string.import_stats_format, importedCount, skippedCount));

        if (errorLog.length() > 0) {
            tvImportErrors.setVisibility(View.VISIBLE);
            tvImportErrors.setText(errorLog.toString().trim());
        } else {
            tvImportErrors.setVisibility(View.GONE);
        }

        Toast.makeText(this, getString(R.string.import_stats_format, importedCount, skippedCount), Toast.LENGTH_SHORT).show();
    }
}
