package com.st0rmtroop3r.telegramchart.views.charts.area;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.st0rmtroop3r.telegramchart.enitity.ChartData;
import com.st0rmtroop3r.telegramchart.enitity.ChartYData;
import com.st0rmtroop3r.telegramchart.views.ChartDrawable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AreaChart extends ChartDrawable {

    public static final String TAG = AreaChart.class.getSimpleName();

    protected XAxisGridLine xAxisGridLine = new XAxisGridLine();
    protected ChartData data;

    protected int areasCount;
    protected List<Area> areas = new ArrayList<>();
    protected List<Stack> stacks;
    private long time;

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.viewWidth = right - left;
        this.viewHeight = bottom - top;
        xInterval = (float) viewWidth / (xDataTo - xDataFrom - 1);
        xAxisGridLine.y0 = top;
        xAxisGridLine.y1 = bottom;
    }

    @Override
    public void setData(ChartData chartData) {
        data = chartData;
        xData = chartData.xData;
        for (ChartYData yData : data.yDataList) {
            areas.add(new Area(yData));
        }
        xDataLength = chartData.xData.length;
        xDataTo = xDataLength;
        xDataFrom = 300;
        xInterval = (float) viewWidth / (xDataTo - xDataFrom - 1);
        areasCount = chartData.yDataList.size();
        stacks = new ArrayList<>(xDataLength);
        for (int i = 0; i < xDataLength; i++) {
            Stack stack = new Stack(areasCount);
            for (int j = 0; j < areasCount; j++) {
                stack.area = areas.get(j);
                stack.values[j] = data.yDataList.get(j).yData[i];
            }
            stack.calcPercents();
            stacks.add(stack);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        for (int i = areas.size() - 1; i >= 0; i--) {
            Area area = areas.get(i);
            if (!area.draw) continue;
            canvas.drawPath(area.path, area.paint);
        }
        xAxisGridLine.draw(canvas);
    }

    @Override
    public void prepareInvalidate() {
        setupPaths();
    }


    @Override
    public boolean onDown(float x) {
        return isHighlightAvailable(x);
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
        for (int i = 0; i < areas.size(); i++) {
            Area chart = areas.get(i);
            if (!chart.draw) continue;

            toolTip.addValue(chart.name, "" + chart.data[dataIndex], chart.paint.getColor());
            ((AreaChartToolTip)toolTip).addPercent(stacks.get(dataIndex).percents[i]);
        }
        toolTip.setDateLong(xData[dataIndex]);
        toolTip.setTitle(labelDateFormat.format(new Date(xData[dataIndex])));
        float labelWidth = toolTip.getWidth();
        float labelMargin = toolTip.getMarginH();
        float toolTipX = xCoordinate - labelWidth - labelMargin;
        if (toolTipX < labelMargin) {
            toolTipX = xCoordinate + labelMargin;
        } else if (toolTipX + labelWidth + labelMargin > viewWidth) {
            toolTipX = viewWidth - labelWidth - labelMargin;
        }
        toolTip.setX(toolTipX);
        toolTip.setShow(true);
        currentHighlightIndex = dataIndex;
    }

    private void resetHighlightPaths() {
        xAxisGridLine.isShowing = false;
    }

    void recalcStacksPercents() {
        for (int i = xDataFrom; i < xDataTo; i++) {
            stacks.get(i).calcPercents();
        }
    }

    void setupPaths() {
        float xBase = left - xOffset;
        float x = 0;
        float y = bottom;
        for (Area area : areas) {
            area.path.reset();
            area.path.moveTo(xBase, y);
        }

        for (int i = xDataFrom; i < xDataTo; i++) {
            y = bottom;
            for (int a = 0; a < areasCount; a++) {
                Area area = areas.get(a);
                if (!area.draw) continue;
                Path path = area.path;
                x = xBase + i * xInterval;
                y = y - viewHeight * stacks.get(i).percents[a] * area.visibility;
                path.lineTo(x, y);
            }
        }
        Path path;
        for (Area area : areas) {
            path = area.path;
            path.lineTo(x, bottom);
        }
    }

    class Stack {
        int[] values;
        float[] percents;
        Area area;
        Stack(int size) {
            values = new int[size];
            percents = new float[size];
        }

        void calcPercents() {
            long sum = 0;
            Area area;
            for (int i = 0; i < values.length; i++) {
                area = areas.get(i);
                if (!area.draw) continue;
//                sum += values[i];
                sum += (values[i] * area.visibility);
            }
            for (int i = 0; i < values.length; i++) {
                area = areas.get(i);
                if (!area.draw) continue;
//                percents[i] = (float) values[i] * area.visibility / sum;
                percents[i] = (float) values[i] / sum;
            }
        }
    }

    class Area {
        String id;
        String name;
        Path path = new Path();
        Paint paint = new Paint();
        int color;
        float visibility = 1;
        ChartYData yData;
        int[] data;
        boolean checked;
        boolean draw;

        Area(ChartYData chartYData) {
            yData = chartYData;
            id = chartYData.id;
            data = chartYData.yData;
            checked = chartYData.visible;
            draw = chartYData.visible;
            name = chartYData.name;
            color = Color.parseColor(chartYData.color);
            paint.setColor(color);

        }
    }
    class XAxisGridLine {

        final Paint paint = new Paint();
        float x;
        float y0 = 0f;
        float y1;
        boolean isShowing = false;

        XAxisGridLine() {
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.GRAY);
            paint.setStrokeWidth(5);
            paint.setAlpha(150);
        }

        void draw(Canvas canvas) {
            if (isShowing) canvas.drawLine(x, y0, x, y1, paint);
        }
    }
}
