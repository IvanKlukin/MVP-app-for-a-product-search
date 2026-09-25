package ru.study.storesearch.domain;

import org.junit.Test;
import static org.junit.Assert.*;

public class StoreMapCoordinatesTest {

    @Test
    public void testFitCenterBoundsCalculation() {
        // Контейнер 1000 x 500, картинка 800 x 600
        // Масштабирование по высоте: scale = 500 / 600 = 0.8333333
        // Отображаемая ширина = 800 * (500/600) = 666.6667
        // Отступ по X = (1000 - 666.6667) / 2 = 166.6667
        // Отступ по Y = 0
        float[] bounds = MapCoordinateMapper.calculateFitCenterBounds(1000f, 500f, 800f, 600f);
        float scale = bounds[0];
        float dispW = bounds[1];
        float dispH = bounds[2];
        float offsetX = bounds[3];
        float offsetY = bounds[4];

        assertEquals(500f / 600f, scale, 0.001f);
        assertEquals(800f * (500f / 600f), dispW, 0.01f);
        assertEquals(500f, dispH, 0.01f);
        assertEquals((1000f - dispW) / 2f, offsetX, 0.01f);
        assertEquals(0f, offsetY, 0.001f);
    }

    @Test
    public void testCoordinateRoundTrip() {
        // Проверяем прямое и обратное преобразование координат
        float viewW = 800f;
        float viewH = 1200f;
        float imgW = 600f;
        float imgH = 400f;

        float originalRelX = 0.35f;
        float originalRelY = 0.80f;

        // Относительные -> Экранные пиксели
        float screenX = MapCoordinateMapper.toScreenX(originalRelX, viewW, viewH, imgW, imgH);
        float screenY = MapCoordinateMapper.toScreenY(originalRelY, viewW, viewH, imgW, imgH);

        // Экранные пиксели -> Относительные
        float roundTripRelX = MapCoordinateMapper.toRelativeX(screenX, viewW, viewH, imgW, imgH);
        float roundTripRelY = MapCoordinateMapper.toRelativeY(screenY, viewW, viewH, imgW, imgH);

        assertEquals(originalRelX, roundTripRelX, 0.0001f);
        assertEquals(originalRelY, roundTripRelY, 0.0001f);
    }

    @Test
    public void testIsInsideImageDetection() {
        // Контейнер 1000 x 500, картинка 800 x 600 (pillarbox по бокам, отступ слева ~166px)
        float viewW = 1000f;
        float viewH = 500f;
        float imgW = 800f;
        float imgH = 600f;

        // Точка в левом пустом поле (letterbox/pillarbox)
        assertFalse(MapCoordinateMapper.isInsideImage(50f, 250f, viewW, viewH, imgW, imgH));

        // Точка в правом пустом поле
        assertFalse(MapCoordinateMapper.isInsideImage(950f, 250f, viewW, viewH, imgW, imgH));

        // Точка строго по центру изображения
        assertTrue(MapCoordinateMapper.isInsideImage(500f, 250f, viewW, viewH, imgW, imgH));
    }

    @Test
    public void testClampingOutOfBounds() {
        float viewW = 500f;
        float viewH = 500f;
        float imgW = 500f;
        float imgH = 500f;

        // Касание за левой/верхней границей
        assertEquals(0.0f, MapCoordinateMapper.toRelativeX(-100f, viewW, viewH, imgW, imgH), 0.0001f);
        assertEquals(0.0f, MapCoordinateMapper.toRelativeY(-50f, viewW, viewH, imgW, imgH), 0.0001f);

        // Касание за правой/нижней границей
        assertEquals(1.0f, MapCoordinateMapper.toRelativeX(800f, viewW, viewH, imgW, imgH), 0.0001f);
        assertEquals(1.0f, MapCoordinateMapper.toRelativeY(1000f, viewW, viewH, imgW, imgH), 0.0001f);
    }
}
