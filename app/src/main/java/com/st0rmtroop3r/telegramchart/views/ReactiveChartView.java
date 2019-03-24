package com.st0rmtroop3r.telegramchart.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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

    private final Path linePath = new Path();
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint innerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Circle> circles = new ArrayList<>();
    private final float circleOuterRadius = 25f;
    private final float circleInnerRadius = 15f;
    private int currentHighlightIndex = -1;
    private long[] xData;
    private Badge badge;

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
        linePaint.setColor(typedValue.data);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeWidth(resources.getDimension(R.dimen.highlight_line));

        theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true);
        innerCirclePaint.setColor(typedValue.data);
        innerCirclePaint.setStyle(Paint.Style.FILL);

        chartStrokeWidth = resources.getDimension(R.dimen.reactive_chart_stroke_width);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        badge.chartWidth = viewWidth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPath(linePath, linePaint);

        for (int i = circles.size() - 1; i >= 0; i--) {
            Circle c = circles.get(i);
            if (c.visible) c.draw(canvas);
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
            if (circle.id.equals(lineId)) circle.visible = visible;
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
        linePath.moveTo(xCoordinate, 0f);
        linePath.lineTo(xCoordinate, viewHeight);

        badge.removeValues();
        for (int i = 0; i < chartLines.size(); i++) {
            ChartLineView chart = chartLines.get(i);
            if (!chart.visible) continue;
            circles.get(i).setCoordinates(xCoordinate, viewHeight - yInterval * chart.data[dataIndex]);
            badge.addValue("" + chart.data[dataIndex], chart.name, chart.paint.getColor());
        }

        badge.setTitle(new SimpleDateFormat("EEE, MMM dd").format(new Date(xData[dataIndex])));
        badge.setX(xCoordinate);
        badge.setVisibility(VISIBLE);
        currentHighlightIndex = dataIndex;
    }

    private void resetHighlightPaths() {
        linePath.reset();
        for (Circle circle : circles) {
            circle.reset();
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

    private class Circle {
        String id;
        boolean visible;
        Path outer = new Path();
        Path inner = new Path();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        Circle(int color, String id, boolean visible) {
            paint.setColor(color);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(5f);
            this.id = id;
            this.visible = visible;
        }

        void setCoordinates(float x, float y) {
            outer.addCircle(x, y, circleOuterRadius, Path.Direction.CW);
            inner.addCircle(x, y, circleInnerRadius, Path.Direction.CW);
        }

        void draw(Canvas canvas) {
            canvas.drawPath(outer, paint);
            canvas.drawPath(inner, innerCirclePaint);
        }

        void reset() {
            outer.reset();
            inner.reset();
        }
    }
}

