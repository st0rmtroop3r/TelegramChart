package com.st0rmtroop3r.telegramchart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.Nullable;

public class ReactiveChartView extends ChartView {

    private static final String TAG = ReactiveChartView.class.getSimpleName();

    private Path linePath = new Path();
    private Paint linePaint = new Paint();

    public ReactiveChartView(Context context) {
        super(context);
        init(context);
    }

    public ReactiveChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ReactiveChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        linePaint.setColor(Color.LTGRAY);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeWidth(10f);
    }

    void setRange(float fromPercent, float toPercent) {
        setChartsDataDisplayRange(fromPercent, toPercent);
        setupChartsPath();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(linePath, linePaint);
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

    private void onActionDown(float x) {

        float nearestXPoint = getNearestXPoint(x);
        float coordX = xAxisInterval * nearestXPoint;

        Log.w(TAG, "onActionDown: x: " + x + ", coordX: " + coordX);

        linePath.moveTo(coordX, 0f);
        linePath.lineTo(coordX, viewHeight);

        invalidate();
    }

    private void onActionMove(float x) {
    }

    private void onActionUp() {
        linePath.reset();
    }


    private float getNearestXPoint(float x) {
        float xAxisPercent = x / viewWidth;
        float xPoint = (xAxisLength - 1) * xAxisPercent;
        return xPoint;
    }
}

