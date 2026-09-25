package ru.study.storesearch.domain;

/**
 * Утилитный класс для преобразования координат между экранными пикселями View
 * и относительными координатами схемы (0.0 .. 1.0) с учётом масштабирования fitCenter.
 * Полностью изолирован от Android-зависимостей для чистого модульного тестирования.
 */
public final class MapCoordinateMapper {

    private MapCoordinateMapper() {
    }

    /**
     * Вычисляет параметры отображения изображения внутри контейнера с масштабированием fitCenter.
     *
     * @return массив float из 4 элементов: [scale, displayedWidth, displayedHeight, offsetX, offsetY]
     */
    public static float[] calculateFitCenterBounds(float viewWidth, float viewHeight,
                                                   float imageWidth, float imageHeight) {
        if (viewWidth <= 0 || viewHeight <= 0 || imageWidth <= 0 || imageHeight <= 0) {
            return new float[]{1.0f, 0.0f, 0.0f, 0.0f, 0.0f};
        }
        float scaleX = viewWidth / imageWidth;
        float scaleY = viewHeight / imageHeight;
        float scale = Math.min(scaleX, scaleY);

        float displayedWidth = imageWidth * scale;
        float displayedHeight = imageHeight * scale;
        float offsetX = (viewWidth - displayedWidth) / 2.0f;
        float offsetY = (viewHeight - displayedHeight) / 2.0f;

        return new float[]{scale, displayedWidth, displayedHeight, offsetX, offsetY};
    }

    /**
     * Проверяет, попадает ли экранное касание в фактическую область отрисовки изображения.
     */
    public static boolean isInsideImage(float touchX, float touchY,
                                        float viewWidth, float viewHeight,
                                        float imageWidth, float imageHeight) {
        float[] bounds = calculateFitCenterBounds(viewWidth, viewHeight, imageWidth, imageHeight);
        float offsetX = bounds[3];
        float offsetY = bounds[4];
        float dispW = bounds[1];
        float dispH = bounds[2];

        return touchX >= offsetX && touchX <= (offsetX + dispW) &&
                touchY >= offsetY && touchY <= (offsetY + dispH);
    }

    /**
     * Преобразует экранную координату касания X в относительную координату (0.0 .. 1.0).
     * Если координата вне изображения, ограничивает её диапазоном [0.0, 1.0].
     */
    public static float toRelativeX(float touchX,
                                    float viewWidth, float viewHeight,
                                    float imageWidth, float imageHeight) {
        float[] bounds = calculateFitCenterBounds(viewWidth, viewHeight, imageWidth, imageHeight);
        float dispW = bounds[1];
        float offsetX = bounds[3];
        if (dispW <= 0) return 0.0f;

        float relX = (touchX - offsetX) / dispW;
        return clamp(relX, 0.0f, 1.0f);
    }

    /**
     * Преобразует экранную координату касания Y в относительную координату (0.0 .. 1.0).
     * Если координата вне изображения, ограничивает её диапазоном [0.0, 1.0].
     */
    public static float toRelativeY(float touchY,
                                    float viewWidth, float viewHeight,
                                    float imageWidth, float imageHeight) {
        float[] bounds = calculateFitCenterBounds(viewWidth, viewHeight, imageWidth, imageHeight);
        float dispH = bounds[2];
        float offsetY = bounds[4];
        if (dispH <= 0) return 0.0f;

        float relY = (touchY - offsetY) / dispH;
        return clamp(relY, 0.0f, 1.0f);
    }

    /**
     * Преобразует относительную координату X (0.0 .. 1.0) в экранную координату в пикселях.
     */
    public static float toScreenX(float relativeX,
                                  float viewWidth, float viewHeight,
                                  float imageWidth, float imageHeight) {
        float[] bounds = calculateFitCenterBounds(viewWidth, viewHeight, imageWidth, imageHeight);
        float dispW = bounds[1];
        float offsetX = bounds[3];
        return offsetX + (clamp(relativeX, 0.0f, 1.0f) * dispW);
    }

    /**
     * Преобразует относительную координату Y (0.0 .. 1.0) в экранную координату в пикселях.
     */
    public static float toScreenY(float relativeY,
                                  float viewWidth, float viewHeight,
                                  float imageWidth, float imageHeight) {
        float[] bounds = calculateFitCenterBounds(viewWidth, viewHeight, imageWidth, imageHeight);
        float dispH = bounds[2];
        float offsetY = bounds[4];
        return offsetY + (clamp(relativeY, 0.0f, 1.0f) * dispH);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }
}
