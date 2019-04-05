package com.st0rmtroop3r.telegramchart.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;

import com.st0rmtroop3r.telegramchart.R;
import com.st0rmtroop3r.telegramchart.enitity.Chart;
import com.st0rmtroop3r.telegramchart.enitity.ChartLine;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReactiveChartView extends ChartView {

    private static final String TAG = ReactiveChartView.class.getSimpleName();

    private XAxisGridLine line = new XAxisGridLine();
    private final Paint innerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Circle> circles = new ArrayList<>();
    private float circleOuterRadius = 25f;
    private float circleInnerRadius = 15f;
    private int currentHighlightIndex = -1;
    private long[] xData;
    private Badge badge;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, MMM dd");

    public ReactiveChartView(Context context) {
        super(context);
        init(context);
    }

    public ReactiveChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ReactiveChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        Resources resources = context.getResources();
        Resources.Theme theme = context.getTheme();
        TypedValue typedValue = new TypedValue();

        theme.resolveAttribute(R.attr.highlight_line_color, typedValue, true);
        line.paint.setColor(typedValue.data);
        line.paint.setStyle(Paint.Style.STROKE);
        line.paint.setStrokeWidth(resources.getDimension(R.dimen.highlight_line));
        line.y0 = 0f;

        theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true);
        innerCirclePaint.setColor(typedValue.data);
        innerCirclePaint.setStyle(Paint.Style.FILL);

        circleOuterRadius = resources.getDimension(R.dimen.chart_line_circle_outer_radius);
        circleInnerRadius = resources.getDimension(R.dimen.chart_line_circle_inner_radius);

        chartStrokeWidth = resources.getDimension(R.dimen.reactive_chart_stroke_width);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        badge.chartWidth = viewWidth;
        line.y1 = viewHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        line.draw(canvas);

        for (int i = circles.size() - 1; i >= 0; i--) {
            Circle c = circles.get(i);
            if (c.isLineVisible) c.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onActionDown(event.getX());
                return true;
            case MotionEvent.ACTION_MOVE:
                onActionMove(event.getX());
                return true;
            case MotionEvent.ACTION_UP:
                onActionUp();
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    public void setChartsData(Chart newChart) {
        super.setChartsData(newChart);
        xData = newChart.xData;
        circles.clear();
        for (ChartLine chartLine : newChart.chartLines) {
            circles.add(new Circle(Color.parseColor(chartLine.color), chartLine.id, chartLine.visible));
        }
    }

    @Override
    public void setLineVisible(String lineId, boolean visible) {
        super.setLineVisible(lineId, visible);
        for (Circle circle : circles) {
            if (circle.id.equals(lineId)) circle.isLineVisible = visible;
        }
    }

    public void setBadge(Badge badge) {
        this.badge = badge;
    }

    private void onActionDown(float x) {
        if (isHighlightAvailable(x)) invalidate();
    }

    private void onActionMove(float x) {
        resetHighlightPaths();
        if (isHighlightAvailable(x)) invalidate();
    }

    private void onActionUp() {
        currentHighlightIndex = -1;
        badge.setVisibility(GONE);
        badge.removeValues();
        resetHighlightPaths();
        invalidate();
    }

    private boolean isHighlightAvailable(float x) {
        int nearestXDataIndex = getNearestXDataIndex(x);
        if (nearestXDataIndex < 0 || nearestXDataIndex > xAxisLength
                || nearestXDataIndex == currentHighlightIndex) return false;
        setupHighlightPaths(nearestXDataIndex);
        return true;
    }

    private void setupHighlightPaths(int dataIndex) {
        float xCoordinate = getDataIndexXCoordinate(dataIndex);

        line.x = xCoordinate;
        line.isShowing = true;

        badge.removeValues();
        for (int i = 0; i < chartLines.size(); i++) {
            ChartLineView chart = chartLines.get(i);
            if (!chart.visible) continue;
            circles.get(i).setCoordinates(xCoordinate, viewHeight - yInterval * chart.data[dataIndex]);
            circles.get(i).isShowing = true;
            badge.addValue("" + chart.data[dataIndex], chart.name, chart.paint.getColor());
        }

        badge.setTitle(simpleDateFormat.format(new Date(xData[dataIndex])));
        badge.setX(xCoordinate);
        badge.setVisibility(VISIBLE);
        currentHighlightIndex = dataIndex;
    }

    private void resetHighlightPaths() {
        line.isShowing = false;
        for (Circle circle : circles) {
            circle.isShowing = false;
        }
    }

    private int getNearestXDataIndex(float x) {
        float scaledXPosition = x - xOffset;
        float scaledWidthPercent = (scaledXPosition - leftPadding) / (totalScaledWidth - totalXPadding);
        return Math.round(xAxisLength * scaledWidthPercent);
    }

    private float getDataIndexXCoordinate(int dataIndex) {
        return leftPadding + xInterval * dataIndex + xOffset;
    }

    private class XAxisGridLine {

        private final Paint paint = new Paint();
        float x;
        float y0;
        float y1;
        boolean isShowing = false;

        void draw(Canvas canvas) {
            if (isShowing) canvas.drawLine(x, y0, x, y1, paint);
        }
    }

    private class Circle {
        String id;
        boolean isLineVisible;
        boolean isShowing = false;
        float x;
        float y;
        Paint paint = new Paint();

        Circle(int color, String id, boolean visible) {
            paint.setColor(color);
            paint.setStyle(Paint.Style.FILL);
            this.id = id;
            this.isLineVisible = visible;
        }

        void setCoordinates(float cx, float cy) {
            x = cx;
            y = cy;
        }

        void draw(Canvas canvas) {
            if (isShowing) {
                canvas.drawCircle(x, y, circleOuterRadius, paint);
                canvas.drawCircle(x, y, circleInnerRadius, innerCirclePaint);
            }
        }
    }
}

