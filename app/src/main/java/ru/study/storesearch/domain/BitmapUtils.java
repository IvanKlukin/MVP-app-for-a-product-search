package ru.study.storesearch.domain;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

/**
 * Утилита безопасной загрузки растровых изображений с масштабированием
 * (inSampleSize) для защиты от OutOfMemoryError на бюджетных устройствах с API 17.
 */
public final class BitmapUtils {

    private BitmapUtils() {
    }

    /**
     * Вычисляет оптимальный коэффициент уменьшения inSampleSize (степень 2).
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * Безопасно загружает растровое изображение из файла.
     */
    public static Bitmap decodeSampledBitmapFromFile(String filePath, int reqWidth, int reqHeight) {
        if (filePath == null) return null;
        File file = new File(filePath);
        if (!file.exists() || !file.canRead()) return null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        if (options.outWidth <= 0 || options.outHeight <= 0) {
            return null;
        }

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565; // Экономия памяти на API 17

        try {
            return BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError oom) {
            return null;
        }
    }

    /**
     * Безопасно загружает растровое изображение из ресурсов.
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        if (res == null || resId == 0) return null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        if (options.outWidth <= 0 || options.outHeight <= 0) {
            return null;
        }

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        try {
            return BitmapFactory.decodeResource(res, resId, options);
        } catch (OutOfMemoryError oom) {
            return null;
        }
    }
}
