package ru.study.storesearch.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import ru.study.storesearch.R;
import ru.study.storesearch.data.DemoDataGenerator;
import ru.study.storesearch.data.StoreRepository;

/**
 * Главный экран приложения.
 * Позволяет выбрать роль («Оператор магазина» или «Покупатель»),
 * просмотреть статистику каталога и загрузить демонстрационные данные.
 */
public class MainActivity extends Activity {

    private StoreRepository repository;
    private TextView tvDbStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = new StoreRepository(this);

        tvDbStats = (TextView) findViewById(R.id.tvDbStats);
        Button btnOperator = (Button) findViewById(R.id.btnOperator);
        Button btnCustomer = (Button) findViewById(R.id.btnCustomer);
        Button btnDemoData = (Button) findViewById(R.id.btnDemoData);
        Button btnClearData = (Button) findViewById(R.id.btnClearData);

        // Переход в режим оператора
        btnOperator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, OperatorActivity.class);
                startActivity(intent);
            }
        });

        // Переход в режим покупателя (поиск товаров)
        btnCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        // Загрузка демонстрационного набора данных
        btnDemoData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLoadDemoData();
            }
        });

        // Очистка каталога
        btnClearData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClearData();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDatabaseStats();
    }

    private void updateDatabaseStats() {
        int sections = repository.getSectionCount();
        int products = repository.getProductCount();
        String statsText = getString(R.string.db_status_format, sections, products);
        tvDbStats.setText(statsText);
    }

    private void handleLoadDemoData() {
        int existingSections = repository.getSectionCount();
        if (existingSections > 0) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.btn_demo_data)
                    .setMessage("В базе уже имеются данные (" + existingSections + " разделов). Перезаписать демонстрационными данными?")
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int loaded = DemoDataGenerator.populateDemoData(repository, true);
                            updateDatabaseStats();
                            Toast.makeText(MainActivity.this,
                                    getString(R.string.demo_loaded_success, loaded), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            int loaded = DemoDataGenerator.populateDemoData(repository, false);
            updateDatabaseStats();
            Toast.makeText(this, getString(R.string.demo_loaded_success, loaded), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleClearData() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.btn_clear_data)
                .setMessage(R.string.confirm_clear_data)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        repository.clearAllData();
                        repository.clearCustomMapPath();
                        updateDatabaseStats();
                        Toast.makeText(MainActivity.this, R.string.data_cleared_success, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
