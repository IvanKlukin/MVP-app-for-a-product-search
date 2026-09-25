package ru.study.storesearch.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import ru.study.storesearch.data.model.Product;
import ru.study.storesearch.data.model.Section;

/**
 * Репозиторий для доступа к локальным данным (SQLite и SharedPreferences).
 * Гарантирует закрытие ресурсов, безопасность параметризованных запросов
 * и целостность внешних ключей.
 */
public class StoreRepository {

    private static final String PREFS_NAME = "store_search_prefs";
    private static final String KEY_CUSTOM_MAP_PATH = "custom_map_path";

    private final DatabaseHelper dbHelper;
    private final SharedPreferences preferences;

    public StoreRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context.getApplicationContext());
        this.preferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ==========================================
    // РАЗДЕЛЫ МАГАЗИНА (SECTIONS)
    // ==========================================

    public long insertSection(String name, float xPercent, float yPercent) {
        if (name == null || name.trim().isEmpty()) {
            return -1;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_SECTION_NAME, name.trim());
        values.put(DatabaseHelper.COL_SECTION_X, xPercent);
        values.put(DatabaseHelper.COL_SECTION_Y, yPercent);
        return db.insert(DatabaseHelper.TABLE_SECTIONS, null, values);
    }

    public boolean updateSection(long id, String name, float xPercent, float yPercent) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_SECTION_NAME, name.trim());
        values.put(DatabaseHelper.COL_SECTION_X, xPercent);
        values.put(DatabaseHelper.COL_SECTION_Y, yPercent);
        int rows = db.update(DatabaseHelper.TABLE_SECTIONS, values,
                DatabaseHelper.COL_SECTION_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public boolean deleteSection(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Внешние ключи удалят связанные товары каскадно благодаря ON DELETE CASCADE
        int rows = db.delete(DatabaseHelper.TABLE_SECTIONS,
                DatabaseHelper.COL_SECTION_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public Section getSectionById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_SECTIONS, null,
                    DatabaseHelper.COL_SECTION_ID + " = ?",
                    new String[]{String.valueOf(id)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToSection(cursor);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public Section getSectionByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_SECTIONS, null,
                    DatabaseHelper.COL_SECTION_NAME + " = ? COLLATE NOCASE",
                    new String[]{name.trim()}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToSection(cursor);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public List<Section> getAllSections() {
        List<Section> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_SECTIONS, null,
                    null, null, null, null, DatabaseHelper.COL_SECTION_NAME + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToSection(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public int getSectionCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_SECTIONS, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return 0;
    }

    private Section cursorToSection(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SECTION_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SECTION_NAME));
        float x = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SECTION_X));
        float y = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SECTION_Y));
        return new Section(id, name, x, y);
    }

    // ==========================================
    // ТОВАРЫ (PRODUCTS)
    // ==========================================

    /**
     * Вставка либо обновление товара (upsert).
     * Если товар с таким normalizedName уже существует, обновляется его название и привязка к разделу.
     */
    public long upsertProduct(String name, String normalizedName, long sectionId) {
        if (name == null || name.trim().isEmpty() || normalizedName == null || normalizedName.trim().isEmpty()) {
            return -1;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = null;
        long existingId = -1;
        try {
            cursor = db.query(DatabaseHelper.TABLE_PRODUCTS,
                    new String[]{DatabaseHelper.COL_PRODUCT_ID},
                    DatabaseHelper.COL_PRODUCT_NORMALIZED + " = ?",
                    new String[]{normalizedName}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                existingId = cursor.getLong(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_PRODUCT_NAME, name.trim());
        values.put(DatabaseHelper.COL_PRODUCT_NORMALIZED, normalizedName);
        values.put(DatabaseHelper.COL_PRODUCT_SECTION_ID, sectionId);

        if (existingId > 0) {
            // Обновляем существующий товар
            int updated = db.update(DatabaseHelper.TABLE_PRODUCTS, values,
                    DatabaseHelper.COL_PRODUCT_ID + " = ?",
                    new String[]{String.valueOf(existingId)});
            return updated > 0 ? existingId : -1;
        } else {
            // Вставляем новую запись
            return db.insert(DatabaseHelper.TABLE_PRODUCTS, null, values);
        }
    }

    public Product getProductByExactNormalizedName(String normalizedName) {
        if (normalizedName == null || normalizedName.trim().isEmpty()) {
            return null;
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        String sql = "SELECT p." + DatabaseHelper.COL_PRODUCT_ID + ", " +
                "p." + DatabaseHelper.COL_PRODUCT_NAME + ", " +
                "p." + DatabaseHelper.COL_PRODUCT_NORMALIZED + ", " +
                "p." + DatabaseHelper.COL_PRODUCT_SECTION_ID + ", " +
                "s." + DatabaseHelper.COL_SECTION_NAME + " AS section_name " +
                "FROM " + DatabaseHelper.TABLE_PRODUCTS + " p " +
                "INNER JOIN " + DatabaseHelper.TABLE_SECTIONS + " s ON p." + DatabaseHelper.COL_PRODUCT_SECTION_ID + " = s." + DatabaseHelper.COL_SECTION_ID + " " +
                "WHERE p." + DatabaseHelper.COL_PRODUCT_NORMALIZED + " = ? LIMIT 1";
        try {
            cursor = db.rawQuery(sql, new String[]{normalizedName});
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToProduct(cursor);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        String sql = "SELECT p." + DatabaseHelper.COL_PRODUCT_ID + ", " +
                "p." + DatabaseHelper.COL_PRODUCT_NAME + ", " +
                "p." + DatabaseHelper.COL_PRODUCT_NORMALIZED + ", " +
                "p." + DatabaseHelper.COL_PRODUCT_SECTION_ID + ", " +
                "s." + DatabaseHelper.COL_SECTION_NAME + " AS section_name " +
                "FROM " + DatabaseHelper.TABLE_PRODUCTS + " p " +
                "INNER JOIN " + DatabaseHelper.TABLE_SECTIONS + " s ON p." + DatabaseHelper.COL_PRODUCT_SECTION_ID + " = s." + DatabaseHelper.COL_SECTION_ID + " " +
                "ORDER BY p." + DatabaseHelper.COL_PRODUCT_NAME + " ASC";
        try {
            cursor = db.rawQuery(sql, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToProduct(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public int getProductCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PRODUCTS, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return 0;
    }

    public void clearAllData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(DatabaseHelper.TABLE_PRODUCTS, null, null);
            db.delete(DatabaseHelper.TABLE_SECTIONS, null, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private Product cursorToProduct(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUCT_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUCT_NAME));
        String normalized = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUCT_NORMALIZED));
        long sectionId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUCT_SECTION_ID));
        String sectionName = cursor.getString(cursor.getColumnIndexOrThrow("section_name"));
        return new Product(id, name, normalized, sectionId, sectionName);
    }

    // ==========================================
    // НАСТРОЙКИ СХЕМЫ (CUSTOM MAP PATH)
    // ==========================================

    public String getCustomMapPath() {
        return preferences.getString(KEY_CUSTOM_MAP_PATH, null);
    }

    public void setCustomMapPath(String path) {
        preferences.edit().putString(KEY_CUSTOM_MAP_PATH, path).apply();
    }

    public void clearCustomMapPath() {
        preferences.edit().remove(KEY_CUSTOM_MAP_PATH).apply();
    }
}
