package com.st0rmtroop3r.telegramchart.views.charts.stackedbar;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.st0rmtroop3r.telegramchart.enitity.ChartData;
import com.st0rmtroop3r.telegramchart.enitity.ChartYData;
import com.st0rmtroop3r.telegramchart.views.ChartDrawable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StackedBarChart extends ChartDrawable {

    private static final String TAG = StackedBarChart.class.getSimpleName();

    protected int paddingX;
    protected int paddingRight;
//    private float scaledWidth;

    protected int maxStackSum;
    protected int[] stackSums;
    protected List<Bar> bars = new ArrayList<>();
//    private ToolTip toolTip = new StackedBarChartToolTip();
//    private SimpleDateFormat labelDateFormat = new SimpleDateFormat("EEE, dd MMM YYYY");
//    private long[] xData;
//    private int currentHighlightIndex = -1;
    private List<HighlightBar> highlightedBars;

    @Override
    public void setData(ChartData chartData) {
        xData = chartData.xData;
        if (chartData.yDataList == null || chartData.yDataList.isEmpty()) return;
        xDataLength = chartData.xData.length;
        highlightedBars = new ArrayList<>(chartData.yDataList.size());

        bars = new ArrayList<>(chartData.yDataList.size());
        for (ChartYData yData : chartData.yDataList) {
            bars.add(new Bar(yData));
            highlightedBars.add(new HighlightBar(Color.parseColor(yData.color)));
            if (yData.yData.length < xDataLength) xDataLength = yData.yData.length;
        }
        xDataTo = xDataLength;
        stackSums = new int[xDataLength];

    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.viewWidth = right - left;
        this.viewHeight = bottom - top;
        this.paddingX = left;
        xInterval = (float) viewWidth / (xDataTo - xDataFrom - 1);
    }

    @Override
    public void draw(Canvas canvas) {
        for (Bar bar : bars) {
            canvas.drawLines(bar.barLines, bar.paint);
        }
        if (toolTip != null && toolTip.isShow()) {
            for (HighlightBar bar : highlightedBars) {
                canvas.drawLines(bar.line, bar.paint);
            }
        }
    }

    @Override
    public void prepareInvalidate() {
//        calcMaxStackSumAndPercents(xDataFrom, xDataTo);
//        calcChartMaxStack(xDataFrom, xDataTo);
        updateMaxStackSum();
        setupBars();
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

//    @Override
//    public void applyTheme(Resources.Theme theme) { }

//    public void selectRange(float fromPercent, float toPercent) {
//        scaledWidth = (viewWidth) / (toPercent - fromPercent);
//        xInterval = scaledWidth / xDataLength;
//        xOffset = scaledWidth * fromPercent;
//
//        xDataFrom = (int) (xDataLength * fromPercent - paddingX / xInterval) - 1;
//        if (xDataFrom < 0) xDataFrom = 0;
//
//        xDataTo = (int) (xDataLength * toPercent + paddingX / xInterval) + 1;
//        if (xDataTo > xDataLength) xDataTo = xDataLength;
//    }

//    @Override
    protected void setupBars() {
        float halfBarWidth = xInterval / 2;

        float xBase = left + halfBarWidth - xOffset;
        int li;
        float x;
        float y;
        Bar previousBar = null;
        for (Bar bar : bars) {
            if (!bar.draw) continue;
            bar.paint.setStrokeWidth(xInterval * 1.01f);
//            bar.paint.setStrokeWidth(xInterval);

            for (int i = 0; i < xDataLength; i++) {
                li = i << 2;
                x = xBase + xInterval * (i);;
                y = bottom;
                if (previousBar != null) {
                    y = previousBar.barLines[li + 3];
                }
                bar.barLines[li] = x;
                bar.barLines[li + 1] = y;
                bar.barLines[li + 2] = x;
                bar.barLines[li + 3] = y - (float) bar.data[i] * bar.visibility / maxStackSum * viewHeight;
            }
            previousBar = bar;
        }
    }

    public void updateMaxStackSum() {
        maxStackSum = calcChartMaxStack(xDataFrom, xDataTo);
    }

    public int calcChartMaxStack() {
        return calcChartMaxStack(xDataFrom, xDataTo);
    }

    public void calcStacksSums() {
        List<Bar> visibleBars = new ArrayList<>();
        for (Bar bar : bars) {
            if (bar.draw) {
                visibleBars.add(bar);
            }
        }
//        int maxStack = 0;
        for (int i = 0; i < xDataLength; i++) {
            int stackSum = 0;
            for (Bar bar : visibleBars) {
                stackSum += bar.data[i] * bar.visibility;
            }
            stackSums[i] = stackSum;
        }
    }

    int calcChartMaxStack(int from, int to) {
        List<Bar> visibleBars = new ArrayList<>();
        for (Bar bar : bars) {
            if (bar.draw) {
                visibleBars.add(bar);
            }
        }
        int maxStack = 0;
        for (int i = from; i < to; i++) {
            int stackSum = 0;
            for (Bar bar : visibleBars) {
                stackSum += bar.data[i] * bar.visibility;
            }
            if (stackSum > maxStack) maxStack = stackSum;
        }
        return maxStack;
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
        float barX = bars.get(0).barLines[dataIndex << 2];
        float y1 = bottom;
        int barSum = 0;
        float barWidth = xInterval * 1.01f;
        toolTip.clear();
        for (int i = 0; i < bars.size(); i++) {
            Bar bar = bars.get(i);
            if (!bar.draw) continue;
            HighlightBar highlightBar = highlightedBars.get(i);
            highlightBar.paint.setStrokeWidth(barWidth);
            float y2 = y1 - (float) bar.data[dataIndex] * bar.visibility / maxStackSum * viewHeight;
            highlightBar.line[0] = barX;
            highlightBar.line[1] = y1;
            highlightBar.line[2] = barX;
            highlightBar.line[3] = y2;
            y1 = y2;
            barSum += bar.data[dataIndex];
            bar.paint.setAlpha(122);
            toolTip.addValue(bar.name, "" + bar.data[dataIndex], highlightBar.color);
        }
        if (highlightedBars.size() > 1) {
            toolTip.addValue("All", "" + barSum, Color.BLACK);
        }

        toolTip.setTitle(labelDateFormat.format(new Date(xData[dataIndex])));
        float toolTipWidth = toolTip.getWidth();
        float toolTipMargin = toolTip.getMarginH();
        float toolTipX = barX - toolTipWidth - toolTipMargin;
        if (toolTipX < toolTipMargin) {
            toolTipX = barX + toolTipMargin;
        } else if (toolTipX + toolTipWidth + toolTipMargin > viewWidth) {
            toolTipX = viewWidth - toolTipWidth - toolTipMargin;
        }
        toolTip.setX(toolTipX);
        toolTip.setShow(true);
        currentHighlightIndex = dataIndex;
    }

    private void resetHighlightPaths() {
        for (Bar bar : bars) {
            bar.paint.setAlpha(255);
        }
    }

    class Bar {

        String id;
        String name;
        int[] data;
        float[] barLines;
        float[] barHeights;
        Paint paint = new Paint();
        float visibility = 1;
        boolean draw = true;
        boolean checked;

        Bar(ChartYData yData) {
            id = yData.id;
            name = yData.name;
            data = yData.yData;
            checked = yData.visible;
            barLines = new float[yData.yData.length * 4];
            barHeights = new float[yData.yData.length];
            paint.setColor(Color.parseColor(yData.color));
            paint.setStyle(Paint.Style.STROKE);
        }
    }

    class HighlightBar {
        float[] line = new float[4];
        Paint paint = new Paint();
        int color;

        HighlightBar(int barColor) {
            color = barColor;
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(color);
        }
    }
}
