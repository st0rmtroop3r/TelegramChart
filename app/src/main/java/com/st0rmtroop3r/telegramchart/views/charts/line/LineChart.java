package com.st0rmtroop3r.telegramchart.views.charts.line;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.st0rmtroop3r.telegramchart.enitity.ChartData;
import com.st0rmtroop3r.telegramchart.enitity.ChartYData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LineChart extends AbstractLineChart {

    private static final String TAG = LineChart.class.getSimpleName();

    protected int chartMaxValue = 0;
    protected int chartMinValue = 0;
    protected int yDataLength = 1;
    protected float yInterval = 0;
    protected float lineStrokeWidth = 5;
    protected List<Line> chartLines;

    @Override
    public void setLineStrokeWidth(float width) {
        if (chartLines == null) return;
        for (Line line : chartLines) {
            line.paint.setStrokeWidth(lineStrokeWidth);
        }
    }

    @Override
    public void setData(ChartData chartData) {
        xData = chartData.xData;
        xDataLength = chartData.xData.length - 1;
        xDataTo = xDataLength;
        chartMaxValue = 0;
        chartMinValue = Integer.MAX_VALUE;
        chartLines = new ArrayList<>(chartData.yDataList.size());
        for (ChartYData yData : chartData.yDataList) {
            Line line = new Line(yData.yData,
                    Color.parseColor(yData.color), yData.name, yData.id, yData.visible);
            chartLines.add(line);
            if (chartMaxValue < line.max) {
                chartMaxValue = line.max;
            }
            if (chartMinValue > line.min) {
                chartMinValue = line.min;
            }
        }
        circles.clear();
        for (ChartYData chartLine : chartData.yDataList) {
            circles.add(new Circle(Color.parseColor(chartLine.color), chartLine.id, chartLine.visible));
        }
        yDataLength = chartMaxValue - chartMinValue;
        yInterval = yDataLength != 0 ? (float) viewHeight / yDataLength : viewHeight;

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
        yDataLength = chartMaxValue - chartMinValue;
        yInterval = yDataLength != 0 ? (float) viewHeight / yDataLength : viewHeight;
        xAxisGridLine.y1 = bottom;
    }

    @Override
    public void draw(Canvas canvas) {
        for (int i = chartLines.size() - 1; i >= 0; i--) {
            Line lineView = chartLines.get(i);
            if (lineView.draw) {
                canvas.drawLines(lineView.points, xDataFrom << 2, (xDataTo - xDataFrom) << 2, lineView.paint);
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

    public void setChartMaxMinValues(int newMin, int newMax) {
        chartMinValue = newMin;
        chartMaxValue = newMax;

        yDataLength = chartMaxValue - chartMinValue;
        yInterval = yDataLength != 0 ? (float) viewHeight / yDataLength : viewHeight;
    }

    public void setupLines() {
        float yBase = bottom;
        float xBase = left + xDataFrom * xInterval - xOffset;
        int xDataDrawLength = xDataTo - xDataFrom;

        int lineIndex;
        for (Line line : chartLines) {
            if (!line.draw) continue;
            float y = yBase - yInterval * (line.data[xDataFrom] - chartMinValue);
            line.points[xDataFrom << 2] = xBase;
            line.points[(xDataFrom << 2) + 1] = y;
            for (int k = 1; k <= xDataDrawLength; k++) {
                float x = xBase + k * xInterval;
                int dataIndex = xDataFrom + k;
                y = yBase - yInterval * (line.data[dataIndex] - chartMinValue);
                lineIndex = (dataIndex << 2) - 2;
                line.points[lineIndex] = x;
                line.points[lineIndex + 1] = y;
                line.points[lineIndex + 2] = x;
                line.points[lineIndex + 3] = y;
            }
        }
    }

    public void findMinMaxValues() {
        for (Line line : chartLines) {
            line.findMinMaxValuesInRange(xDataFrom, xDataTo);
        }
    }

    @Override
    public boolean onMove(float x) {
        return isHighlightAvailable(x);
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
    public boolean onDown(float x) {
        return isHighlightAvailable(x);
    }

    private boolean isHighlightAvailable(float x) {
        int nearestXDataIndex = getNearestXDataIndex(x);
        if (nearestXDataIndex < 0 || nearestXDataIndex > xDataLength
                || nearestXDataIndex == currentHighlightIndex) return false;
        setupHighlightPaths(x, nearestXDataIndex);
        return true;
    }

    private int getNearestXDataIndex(float x) {

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
            Line chart = chartLines.get(i);
            if (!chart.checked) continue;
            circles.get(i).setCoordinates(xCoordinate, bottom - yInterval * (chart.data[dataIndex] - chartMinValue));
            circles.get(i).isShowing = true;
            toolTip.addValue(chart.name, "" + chart.data[dataIndex], chart.paint.getColor());
        }
        toolTip.setDateLong(xData[dataIndex]);
        toolTip.setTitle(labelDateFormat.format(new Date(xData[dataIndex])));
        float labelWidth = toolTip.getWidth();
        float toolTipX = x - labelWidth / 2;
        float labelMargin = toolTip.getMarginH();
        if (toolTipX < labelMargin) {
            toolTipX = labelMargin;
        } else if (toolTipX + labelWidth + labelMargin > viewWidth) {
            toolTipX = viewWidth - labelWidth - labelMargin;
        }

        toolTip.setX(toolTipX);

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
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        boolean draw;
        boolean checked;

        Line(int[] data, int color, String name, String id, boolean visible) {
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
    }

}
