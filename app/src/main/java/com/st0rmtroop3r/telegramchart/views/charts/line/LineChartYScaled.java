package com.st0rmtroop3r.telegramchart.views.charts.line;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.st0rmtroop3r.telegramchart.enitity.ChartData;
import com.st0rmtroop3r.telegramchart.enitity.ChartYData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LineChartYScaled extends AbstractLineChart {

    private static final String TAG = LineChartYScaled.class.getSimpleName();
    protected List<Line> chartLines;

    @Override
    public void setData(ChartData chartData) {
        xData = chartData.xData;
        xDataLength = chartData.xData.length - 1;
        xDataTo = xDataLength;
        chartLines = new ArrayList<>(chartData.yDataList.size());
        for (ChartYData yData : chartData.yDataList) {
            Line line = new Line(yData.yData,
                    Color.parseColor(yData.color), yData.name, yData.id, yData.visible);
            chartLines.add(line);
        }
        if (viewHeight != 0) {
            for (Line line : chartLines) {
                line.updateYInterval();
            }
        }
        circles.clear();
        for (Line line : chartLines) {
            circles.add(new Circle(line.color, line.id, line.checked));
        }
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.viewWidth = right - left;
        this.viewHeight = bottom - top;
        xInterval = (float) viewWidth / (xDataTo - xDataFrom - 1);
        for (Line line : chartLines) {
            line.updateYInterval();
        }
        xAxisGridLine.y1 = bottom;
    }

    @Override
    public void draw(Canvas canvas) {

        for (int i = chartLines.size() - 1; i >= 0 ; i--) {
            Line line = chartLines.get(i);
            if (line.draw) {
                line.setupLines();
                canvas.drawLines(line.points, xDataFrom << 2, (xDataTo - xDataFrom) << 2, line.paint);
            }
        }
        xAxisGridLine.draw(canvas);

        for (int i = circles.size() - 1; i >= 0; i--) {
            Circle c = circles.get(i);
            if (c.isLineVisible) c.draw(canvas);
        }
    }

    @Override
    public void prepareInvalidate() {
        setupLines();
    }

    @Override
    public boolean onDown(float x) {
        return isHighlightAvailable(x);
    }

    @Override
    public boolean onMove(float x) {
        return (isHighlightAvailable(x));
    }

    @Override
    public boolean onUp() {

        currentHighlightIndex = -1;

        toolTip.setShow(false);
        toolTip.clear();
        resetHighlightPaths();

        return false;
    }

    @Override
    public void setLineStrokeWidth(float width) {
        if (chartLines == null) return;
        for (Line line : chartLines) {
            line.paint.setStrokeWidth(lineStrokeWidth);
        }
    }

    public void setupLines() {
        for (Line line : chartLines) {
            if (!line.draw) continue;
            line.setupLines();
        }
    }

    public void findNewMinMaxValues() {
        for (Line line : chartLines) {
            line.findNewMinMaxValues();
        }
    }

    private boolean isHighlightAvailable(float x) {
        int nearestXDataIndex = getNearestXDataIndex(x);
        if (nearestXDataIndex < 0 || nearestXDataIndex > xDataLength
                || nearestXDataIndex == currentHighlightIndex) return false;
        setupHighlightPaths(x, nearestXDataIndex);
        return true;
    }

    private int getNearestXDataIndex(float x) {
//        float scaledXPosition = x + xOffset;
//        float scaledWidthPercent = scaledXPosition / scaledWidth;
//        return Math.round(xDataLength * scaledWidthPercent);
        float scaledXPosition = x + xOffset;
        float scaledWidthPercent = scaledXPosition / scaledWidth;
        int index = Math.round((xDataLength - 1) * scaledWidthPercent);
        index = Math.max(index, 0);
        return Math.min(index, xDataLength - 1);
    }

    private float getDataIndexXCoordinate(int dataIndex) {
        return left + xInterval * dataIndex - xOffset;
    }

    private void setupHighlightPaths(float x, int dataIndex) {
        float xCoordinate = getDataIndexXCoordinate(dataIndex);

        xAxisGridLine.x = xCoordinate;
        xAxisGridLine.isShowing = true;

        toolTip.clear();
        for (int i = 0; i < chartLines.size(); i++) {
            Line line = chartLines.get(i);
            if (!line.checked) continue;
            circles.get(i).setCoordinates(xCoordinate, bottom - line.yInterval * (line.data[dataIndex] - line.min));
            circles.get(i).isShowing = true;
            toolTip.addValue(line.name, "" + line.data[dataIndex], line.paint.getColor());
        }

        toolTip.setTitle(labelDateFormat.format(new Date(xData[dataIndex])));
        float labelWidth = toolTip.getWidth();
        float labelX = x - labelWidth / 2;
        float labelMargin = toolTip.getMarginH();
        if (labelX < labelMargin) {
            labelX = labelMargin;
        } else if (labelX + labelWidth + labelMargin > viewWidth) {
            labelX = viewWidth - labelWidth - labelMargin;
        }
        toolTip.setX(labelX);
        toolTip.setShow(true);
        currentHighlightIndex = dataIndex;
    }

    private void resetHighlightPaths() {
        xAxisGridLine.isShowing = false;
        for (Circle circle : circles) {
            circle.isShowing = false;
        }
    }

    class Line {

        final float[] points;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int[] data;
        String name;
        String id;
        int color;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int minNew = Integer.MAX_VALUE;
        int maxNew = Integer.MIN_VALUE;
        boolean draw;
        boolean checked;

        int yDataLength = 1;
        float yInterval = 0;

        Line (int[] data, int color, String name, String id, boolean visible) {
            this.data = data;
            this.name = name;
            this.id = id;
            this.color = color;
            this.draw = visible;
            this.checked = visible;
            findMinMaxValues();
            points = new float[data.length * 4 - 2];

            paint.setColor(color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(lineStrokeWidth);

        }

        void findNewMinMaxValues() {
            if (data == null) return;
            minNew = Integer.MAX_VALUE;
            maxNew = Integer.MIN_VALUE;
            for (int i = xDataFrom; i < xDataTo; i++) {
                int value = data[i];
                if (value > maxNew) maxNew = value;
                if (value < minNew) minNew = value;
            }
        }

        void findMinMaxValues() {
            findMinMaxValuesInRange(xDataFrom, xDataTo);
        }
        void findMinMaxValuesInRange(int from, int to) {
            if (data == null) return;
            min = Integer.MAX_VALUE;
            max = Integer.MIN_VALUE;
            for (int i = from; i < to; i++) {
                checkMinMaxValue(data[i]);
            }
        }

        void checkMinMaxValue(int value) {
            if (value > max) max = value;
            if (value < min) min = value;
        }

        void setLineMaxMinValues(int newMin, int newMax) {
            min = newMin;
            max = newMax;
            updateYInterval();
        }

        void updateYInterval() {
            if (viewHeight == 0) return;
            yDataLength = max - min;
            yInterval = yDataLength != 0 ? (float) viewHeight / yDataLength : viewHeight;
        }

        void setupLines() {
            float yBase = bottom;
            float xBase = left + xDataFrom * xInterval - xOffset;
            int xDataDrawLength = xDataTo - xDataFrom;
            int lineIndex;
            float y = yBase - yInterval * (data[xDataFrom] - min);
            points[xDataFrom << 2] = xBase;
            points[(xDataFrom << 2) + 1] = y;
            for (int k = 1; k <= xDataDrawLength; k++) {
                float x = xBase + k * xInterval;
                int dataIndex = xDataFrom + k;
                y = yBase - yInterval * (data[dataIndex] - min);
                lineIndex = (dataIndex << 2) - 2;

                points[lineIndex] = x;
                points[lineIndex + 1] = y;
                points[lineIndex + 2] = x;
                points[lineIndex + 3] = y;

            }
        }
    }

}
