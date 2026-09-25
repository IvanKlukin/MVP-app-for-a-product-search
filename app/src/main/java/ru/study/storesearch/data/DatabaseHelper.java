package ru.study.storesearch.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Помощник для работы с локальной базой данных SQLite.
 * Включает каскадное удаление и поддержку внешних ключей.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "store_locator.db";
    public static final int DATABASE_VERSION = 1;

    // Таблица разделов
    public static final String TABLE_SECTIONS = "sections";
    public static final String COL_SECTION_ID = "id";
    public static final String COL_SECTION_NAME = "name";
    public static final String COL_SECTION_X = "x_percent";
    public static final String COL_SECTION_Y = "y_percent";

    // Таблица товаров
    public static final String TABLE_PRODUCTS = "products";
    public static final String COL_PRODUCT_ID = "id";
    public static final String COL_PRODUCT_NAME = "name";
    public static final String COL_PRODUCT_NORMALIZED = "normalized_name";
    public static final String COL_PRODUCT_SECTION_ID = "section_id";

    private static final String SQL_CREATE_SECTIONS =
            "CREATE TABLE " + TABLE_SECTIONS + " (" +
                    COL_SECTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_SECTION_NAME + " TEXT NOT NULL UNIQUE, " +
                    COL_SECTION_X + " REAL NOT NULL, " +
                    COL_SECTION_Y + " REAL NOT NULL" +
                    ");";

    private static final String SQL_CREATE_PRODUCTS =
            "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                    COL_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_PRODUCT_NAME + " TEXT NOT NULL, " +
                    COL_PRODUCT_NORMALIZED + " TEXT NOT NULL UNIQUE, " +
                    COL_PRODUCT_SECTION_ID + " INTEGER NOT NULL, " +
                    "FOREIGN KEY (" + COL_PRODUCT_SECTION_ID + ") REFERENCES " +
                    TABLE_SECTIONS + "(" + COL_SECTION_ID + ") ON DELETE CASCADE" +
                    ");";

    private static final String SQL_CREATE_INDEX_NORMALIZED =
            "CREATE INDEX IF NOT EXISTS idx_products_normalized ON " +
                    TABLE_PRODUCTS + " (" + COL_PRODUCT_NORMALIZED + ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Явное включение проверки внешних ключей в SQLite (доступно с API 16)
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_SECTIONS);
        db.execSQL(SQL_CREATE_PRODUCTS);
        db.execSQL(SQL_CREATE_INDEX_NORMALIZED);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SECTIONS);
        onCreate(db);
    }
}
