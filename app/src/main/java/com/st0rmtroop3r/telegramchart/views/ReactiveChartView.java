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
import com.st0rmtroop3r.telegramchart.enitity.ChartData;
import com.st0rmtroop3r.telegramchart.enitity.ChartYData;
import com.st0rmtroop3r.telegramchart.views.charts.line.LineChartToolTip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReactiveChartView extends LineChartView {

    private static final String TAG = ReactiveChartView.class.getSimpleName();

    private XAxisGridLine line = new XAxisGridLine();
    private final Paint innerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Circle> circles = new ArrayList<>();
    private float circleOuterRadius = 25f;
    private float circleInnerRadius = 15f;
    private int currentHighlightIndex = -1;
    private long[] xData;
    private LineChartToolTip label = new LineChartToolTip();
    private float labelMargin = 50;
    private SimpleDateFormat lableDateFormat = new SimpleDateFormat("EEE, dd MMM YYYY");

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

        circleOuterRadius = resources.getDimension(R.dimen.line_chart_circle_outer_radius);
        circleInnerRadius = resources.getDimension(R.dimen.line_chart_circle_inner_radius);

        chartStrokeWidth = resources.getDimension(R.dimen.reactive_chart_stroke_width);

        labelMargin = resources.getDimension(R.dimen.label_padding);
        theme.resolveAttribute(R.attr.label_background_color, typedValue, true);
        label.setBackgroundColor(typedValue.data);
        theme.resolveAttribute(R.attr.label_title_color, typedValue, true);
        label.setTextColor(typedValue.data);
        theme.resolveAttribute(R.attr.label_frame_color, typedValue, true);
        label.setFrameColor(typedValue.data);
        label.setTextSize(resources.getDimension(R.dimen.label_text_size));
        label.setMinWidth(resources.getDimension(R.dimen.label_min_width));
        label.setArrowDrawable(resources.getDrawable(R.drawable.ic_arrow_right));
        label.setRowSpacing(resources.getDimension(R.dimen.label_row_spacing));
        label.setColumnSpacing(resources.getDimension(R.dimen.label_column_spacing));
        label.setPadding(resources.getDimension(R.dimen.label_padding));

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
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

        label.draw(canvas);
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
    public void setChartsData(ChartData newChart) {
        super.setChartsData(newChart);
        xData = newChart.xData;
        circles.clear();
        for (ChartYData chartLine : newChart.yDataList) {
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
//
//    @Override
//    public void selectRange(float fromPercent, float toPercent) {
//        super.selectRange(fromPercent, toPercent);
//        if (!labelShow) {
//            label.show = false;
//        }
//    }

    public void setHighlightLineColor(int color) {
        line.paint.setColor(color);
    }

    public void setLabelBackgroundColor(int color) {
        label.setBackgroundColor(color);
    }

    public void setLabelTitleColor(int color) {
        label.setTextColor(color);
    }

    public void setLabelFrameColor(int color) {
        label.setFrameColor(color);
    }

    public void setCircleInnerColor(int color) {
        innerCirclePaint.setColor(color);
    }

//    boolean labelShow;

    private void onActionDown(float x) {
//        labelShow = !labelShow;
        if (isHighlightAvailable(x)) invalidate();
    }

    private void onActionMove(float x) {
//        labelShow = true;
        resetHighlightPaths();
        if (isHighlightAvailable(x)) invalidate();
    }

    private void onActionUp() {
//        if (labelShow) return;
        currentHighlightIndex = -1;
//        label.show = false;
        label.setShow(false);
        label.clear();
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

        label.clear();
        for (int i = 0; i < chartLines.size(); i++) {
            ChartLineView chart = chartLines.get(i);
            if (!chart.visible) continue;
            circles.get(i).setCoordinates(xCoordinate, viewHeight - yInterval * chart.data[dataIndex]);
            circles.get(i).isShowing = true;
            label.addValue(chart.name, "" + chart.data[dataIndex], chart.paint.getColor());
        }

        label.setTitle(lableDateFormat.format(new Date(xData[dataIndex])));
        float labelWidth = label.getWidth();
        float labelX = xCoordinate - labelWidth / 2;
        if (labelX < labelMargin) {
            labelX = labelMargin;
        } else if (labelX + labelWidth + labelMargin > viewWidth) {
            labelX = viewWidth - labelWidth - labelMargin;
        }
        label.setX(labelX);
//        label.show = true;
        label.setShow(true);
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

