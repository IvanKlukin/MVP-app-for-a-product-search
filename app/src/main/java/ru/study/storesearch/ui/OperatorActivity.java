package ru.study.storesearch.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import ru.study.storesearch.R;
import ru.study.storesearch.data.StoreRepository;
import ru.study.storesearch.data.model.Section;
import ru.study.storesearch.domain.BitmapUtils;
import ru.study.storesearch.ui.view.StoreMapView;

/**
 * Экран оператора магазина.
 * Предоставляет функции:
 * - Выбор и сохранение пользовательской схемы магазина во внутреннее хранилище.
 * - Интерактивное добавление меток разделов по нажатию на схему.
 * - Просмотр и удаление разделов.
 * - Переход к экрану импорта товаров.
 */
public class OperatorActivity extends Activity {

    private static final int REQUEST_PICK_MAP = 101;
    private static final String CUSTOM_MAP_FILENAME = "custom_store_map.png";

    private StoreRepository repository;
    private StoreMapView storeMapView;
    private Bitmap currentMapBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator);

        repository = new StoreRepository(this);
        storeMapView = (StoreMapView) findViewById(R.id.storeMapView);

        Button btnChooseMap = (Button) findViewById(R.id.btnChooseMap);
        Button btnResetMap = (Button) findViewById(R.id.btnResetMap);
        Button btnSectionsList = (Button) findViewById(R.id.btnSectionsList);
        Button btnGoImport = (Button) findViewById(R.id.btnGoImport);

        // Обработка клика по схеме для создания новой метки
        storeMapView.setOnMapClickListener(new StoreMapView.OnMapClickListener() {
            @Override
            public void onMapClick(float relativeX, float relativeY) {
                showAddSectionDialog(relativeX, relativeY);
            }
        });

        // Выбор схемы через совместимый с API 17 ACTION_GET_CONTENT
        btnChooseMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseMapImage();
            }
        });

        // Сброс к нейтральной стандартной схеме
        btnResetMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetToDefaultMap();
            }
        });

        // Просмотр и управление разделами
        btnSectionsList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSectionsListDialog();
            }
        });

        // Переход к импорту товаров
        btnGoImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OperatorActivity.this, ProductImportActivity.class);
                startActivity(intent);
            }
        });

        loadStoreMap();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSectionsOnMap();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recycleCurrentBitmap();
    }

    private void recycleCurrentBitmap() {
        if (currentMapBitmap != null && !currentMapBitmap.isRecycled()) {
            currentMapBitmap.recycle();
            currentMapBitmap = null;
        }
    }

    private void loadStoreMap() {
        recycleCurrentBitmap();

        String customPath = repository.getCustomMapPath();
        if (customPath != null && new File(customPath).exists()) {
            currentMapBitmap = BitmapUtils.decodeSampledBitmapFromFile(customPath, 1200, 1200);
        }

        // Если пользовательской схемы нет или она не загрузилась, используем стандартную
        if (currentMapBitmap == null) {
            currentMapBitmap = BitmapUtils.decodeSampledBitmapFromResource(
                    getResources(), R.drawable.default_store_map, 1200, 1200);
        }

        storeMapView.setMapBitmap(currentMapBitmap);
        refreshSectionsOnMap();
    }

    private void refreshSectionsOnMap() {
        List<Section> sections = repository.getAllSections();
        storeMapView.setSections(sections);
    }

    private void chooseMapImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.btn_choose_map)), REQUEST_PICK_MAP);
        } catch (Exception e) {
            Toast.makeText(this, "Приложение для выбора изображений не найдено", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_MAP && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                copyMapImageToInternalStorage(imageUri);
            }
        }
    }

    /**
     * Безопасное копирование выбранного пользователем файла во внутреннюю память приложения
     * через ContentResolver.openInputStream. Гарантирует сохранение схемы при перезапусках приложения на API 17.
     */
    private void copyMapImageToInternalStorage(Uri uri) {
        File internalFile = new File(getFilesDir(), CUSTOM_MAP_FILENAME);
        InputStream is = null;
        OutputStream os = null;
        try {
            is = getContentResolver().openInputStream(uri);
            if (is == null) {
                Toast.makeText(this, "Не удалось открыть выбранный файл", Toast.LENGTH_SHORT).show();
                return;
            }
            os = new FileOutputStream(internalFile);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();

            repository.setCustomMapPath(internalFile.getAbsolutePath());
            loadStoreMap();
            Toast.makeText(this, R.string.map_saved_success, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка сохранения изображения: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            try {
                if (is != null) is.close();
                if (os != null) os.close();
            } catch (Exception ignored) {
            }
        }
    }

    private void resetToDefaultMap() {
        repository.clearCustomMapPath();
        File internalFile = new File(getFilesDir(), CUSTOM_MAP_FILENAME);
        if (internalFile.exists()) {
            internalFile.delete();
        }
        loadStoreMap();
        Toast.makeText(this, R.string.map_reset_success, Toast.LENGTH_SHORT).show();
    }

    /**
     * Диалог ввода названия раздела при клике на схему.
     */
    private void showAddSectionDialog(final float relX, final float relY) {
        final EditText input = new EditText(this);
        input.setHint(R.string.hint_section_name);
        input.setSingleLine(true);

        int percentX = Math.round(relX * 100);
        int percentY = Math.round(relY * 100);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_new_section_title)
                .setMessage(getString(R.string.dialog_new_section_message, percentX, percentY))
                .setView(input)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String sectionName = input.getText().toString().trim();
                        if (sectionName.isEmpty()) {
                            Toast.makeText(OperatorActivity.this, R.string.error_empty_section_name, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (repository.getSectionByName(sectionName) != null) {
                            Toast.makeText(OperatorActivity.this, R.string.error_duplicate_section_name, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        long id = repository.insertSection(sectionName, relX, relY);
                        if (id > 0) {
                            refreshSectionsOnMap();
                            Toast.makeText(OperatorActivity.this,
                                    getString(R.string.section_added_success, sectionName), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * Диалог просмотра и удаления созданных разделов.
     */
    private void showSectionsListDialog() {
        final List<Section> sections = repository.getAllSections();
        if (sections.isEmpty()) {
            Toast.makeText(this, R.string.no_sections_yet, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] itemNames = new String[sections.size()];
        for (int i = 0; i < sections.size(); i++) {
            Section s = sections.get(i);
            itemNames[i] = s.getName() + " (" + Math.round(s.getXPercent() * 100) + "%, " +
                    Math.round(s.getYPercent() * 100) + "%)";
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.btn_sections_list)
                .setItems(itemNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Section selected = sections.get(which);
                        confirmDeleteSection(selected);
                    }
                })
                .setNegativeButton(R.string.close, null)
                .show();
    }

    private void confirmDeleteSection(final Section section) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage(getString(R.string.confirm_delete_section, section.getName()))
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        repository.deleteSection(section.getId());
                        refreshSectionsOnMap();
                        Toast.makeText(OperatorActivity.this,
                                getString(R.string.section_deleted_success, section.getName()), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
