package ru.study.storesearch.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import ru.study.storesearch.R;
import ru.study.storesearch.data.model.Section;
import ru.study.storesearch.domain.MapCoordinateMapper;

/**
 * Кастомный View для отображения схемы магазина и меток разделов.
 * Поддерживает масштабирование с сохранением пропорций (fitCenter),
 * корректную работу при повороте экрана и статическое выделение выбранного раздела.
 */
public class StoreMapView extends View {

    public interface OnMapClickListener {
        void onMapClick(float relativeX, float relativeY);
    }

    private Bitmap mapBitmap;
    private List<Section> sections = new ArrayList<>();
    private Long highlightedSectionId = null;
    private OnMapClickListener onMapClickListener;
    private boolean interactive = false;

    // Графические объекты для повторного использования (без аллокаций в onDraw)
    private final Paint bgPaint = new Paint();
    private final Paint bitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Paint markerFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint markerStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect textBounds = new Rect();
    private final RectF srcRect = new RectF();
    private final RectF dstRect = new RectF();
    private final RectF labelRect = new RectF();

    private float density = 1.0f;

    public StoreMapView(Context context) {
        super(context);
        init();
    }

    public StoreMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StoreMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        density = getResources().getDisplayMetrics().density;

        bgPaint.setColor(Color.rgb(238, 238, 240));

        markerFillPaint.setStyle(Paint.Style.FILL);
        markerFillPaint.setColor(Color.rgb(30, 136, 229)); // Синий

        markerStrokePaint.setStyle(Paint.Style.STROKE);
        markerStrokePaint.setColor(Color.WHITE);
        markerStrokePaint.setStrokeWidth(2.5f * density);

        // Статическое выделение: контрастное внешнее кольцо
        highlightRingPaint.setStyle(Paint.Style.STROKE);
        highlightRingPaint.setColor(Color.rgb(229, 57, 53)); // Ярко-красный
        highlightRingPaint.setStrokeWidth(3.5f * density);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(12.0f * density);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        labelBgPaint.setStyle(Paint.Style.FILL);
        labelBgPaint.setColor(Color.argb(200, 33, 33, 33)); // Полупрозрачный темный фон бейджа
    }

    public void setMapBitmap(Bitmap bitmap) {
        this.mapBitmap = bitmap;
        invalidate();
    }

    public void setSections(List<Section> sections) {
        this.sections = (sections != null) ? sections : new ArrayList<Section>();
        invalidate();
    }

    public void setHighlightedSectionId(Long sectionId) {
        this.highlightedSectionId = sectionId;
        invalidate();
    }

    public void setOnMapClickListener(OnMapClickListener listener) {
        this.onMapClickListener = listener;
        this.interactive = (listener != null);
    }

    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int viewW = getWidth();
        final int viewH = getHeight();

        // 1. Отрисовка фона области View
        canvas.drawRect(0, 0, viewW, viewH, bgPaint);

        if (mapBitmap == null || mapBitmap.isRecycled()) {
            // Если изображение схемы не задано, рисуем нейтральную подсказку
            textPaint.setColor(Color.DKGRAY);
            textPaint.setTextSize(14f * density);
            canvas.drawText(getContext().getString(R.string.map_not_loaded), viewW / 2.0f, viewH / 2.0f, textPaint);
            return;
        }

        final float imgW = mapBitmap.getWidth();
        final float imgH = mapBitmap.getHeight();

        // 2. Расчет параметров масштабирования fitCenter через чистый Java-класс
        float[] bounds = MapCoordinateMapper.calculateFitCenterBounds(viewW, viewH, imgW, imgH);
        float dispW = bounds[1];
        float dispH = bounds[2];
        float offsetX = bounds[3];
        float offsetY = bounds[4];

        // 3. Отрисовка схемы магазина
        srcRect.set(0, 0, imgW, imgH);
        dstRect.set(offsetX, offsetY, offsetX + dispW, offsetY + dispH);
        canvas.drawBitmap(mapBitmap, null, dstRect, bitmapPaint);

        // 4. Отрисовка меток разделов
        if (sections == null || sections.isEmpty()) {
            return;
        }

        final float normalRadius = 11.0f * density;
        final float highlightRadius = 16.0f * density;
        final float ringRadius = 23.0f * density;

        // Рисуем сначала обычные метки, а затем подсвеченную (чтобы она была поверх остальных)
        Section highlightedSection = null;

        for (Section section : sections) {
            if (highlightedSectionId != null && highlightedSectionId.equals(section.getId())) {
                highlightedSection = section;
                continue;
            }
            drawSectionMarker(canvas, section, viewW, viewH, imgW, imgH, false, normalRadius);
        }

        if (highlightedSection != null) {
            // Отрисовка подсвеченного маркера со статическим кольцом
            float screenX = MapCoordinateMapper.toScreenX(highlightedSection.getXPercent(), viewW, viewH, imgW, imgH);
            float screenY = MapCoordinateMapper.toScreenY(highlightedSection.getYPercent(), viewW, viewH, imgW, imgH);
            canvas.drawCircle(screenX, screenY, ringRadius, highlightRingPaint);
            drawSectionMarker(canvas, highlightedSection, viewW, viewH, imgW, imgH, true, highlightRadius);
        }
    }

    private void drawSectionMarker(Canvas canvas, Section section,
                                   float viewW, float viewH, float imgW, float imgH,
                                   boolean isHighlighted, float radius) {
        float x = MapCoordinateMapper.toScreenX(section.getXPercent(), viewW, viewH, imgW, imgH);
        float y = MapCoordinateMapper.toScreenY(section.getYPercent(), viewW, viewH, imgW, imgH);

        // 1. Цвет маркера
        if (isHighlighted) {
            markerFillPaint.setColor(Color.rgb(229, 57, 53)); // Красный
            markerStrokePaint.setStrokeWidth(3.0f * density);
        } else {
            markerFillPaint.setColor(Color.rgb(30, 136, 229)); // Синий
            markerStrokePaint.setStrokeWidth(2.0f * density);
        }

        // 2. Отрисовка круга маркера с белой обводкой
        canvas.drawCircle(x, y, radius, markerFillPaint);
        canvas.drawCircle(x, y, radius, markerStrokePaint);

        // 3. Подпись названия раздела под маркером в контрастном бейдже
        String name = section.getName();
        if (name != null && !name.isEmpty()) {
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize((isHighlighted ? 12.0f : 10.5f) * density);
            textPaint.getTextBounds(name, 0, name.length(), textBounds);

            float labelWidth = textBounds.width() + (12.0f * density);
            float labelHeight = textBounds.height() + (8.0f * density);
            float labelLeft = x - (labelWidth / 2.0f);
            float labelTop = y + radius + (4.0f * density);

            labelRect.set(labelLeft, labelTop, labelLeft + labelWidth, labelTop + labelHeight);

            if (isHighlighted) {
                labelBgPaint.setColor(Color.rgb(211, 47, 47)); // Красный бейдж для выделенного раздела
            } else {
                labelBgPaint.setColor(Color.argb(210, 33, 33, 33)); // Темный полупрозрачный
            }
            canvas.drawRoundRect(labelRect, 4.0f * density, 4.0f * density, labelBgPaint);

            float textY = labelTop + (labelHeight / 2.0f) + (textBounds.height() / 2.0f) - 1.0f;
            canvas.drawText(name, x, textY, textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!interactive || onMapClickListener == null || mapBitmap == null) {
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_UP:
                performClick();
                float touchX = event.getX();
                float touchY = event.getY();
                int viewW = getWidth();
                int viewH = getHeight();
                int imgW = mapBitmap.getWidth();
                int imgH = mapBitmap.getHeight();

                if (MapCoordinateMapper.isInsideImage(touchX, touchY, viewW, viewH, imgW, imgH)) {
                    float relX = MapCoordinateMapper.toRelativeX(touchX, viewW, viewH, imgW, imgH);
                    float relY = MapCoordinateMapper.toRelativeY(touchY, viewW, viewH, imgW, imgH);
                    onMapClickListener.onMapClick(relX, relY);
                    return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
