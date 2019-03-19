package com.st0rmtroop3r.telegramchart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.Nullable;

public class ReactiveChartView extends ChartView {

    private static final String TAG = ReactiveChartView.class.getSimpleName();

    private float xFrom = 0;
    private float xTo = 1;

    private Path linePath = new Path();
    private Paint linePaint = new Paint();

    private Path aPath = new Path();
    private Paint aPaint = new Paint();

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

    void setZoomRange(float fromPercent, float toPercent) {
        xFrom = fromPercent;
        xTo = toPercent;
        updateView2();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawPath(linePath, linePaint);

        canvas.drawPath(aPath, aPaint);
    }


    private int getNearestXPoint(float x) {
        float xAxisPercent = x / viewWidth;
        Log.w(TAG, "getNearestXPoint: " + x + " / " + viewWidth + " = " + xAxisPercent);
        float xPoint = (xAxisLength) * xAxisPercent;
        Log.w(TAG, "getNearestXPoint: " + (xAxisLength) + " * " + xAxisPercent + " = " + xPoint);
        return Math.round(xPoint);
    }

    private void ololo() {
        int[] aData = charts.get(0).data;
        float range = xTo - xFrom;
        float totalScaledWidth = viewWidth / range;
        float xInterval = (totalScaledWidth - 100) / (aData.length - 1);
        float xOffset = -totalScaledWidth * xFrom;
        Log.w(TAG, "ololo: xInterval " + xInterval + ", xOffset " + xOffset );

        int fromDataIndex = (int) ((aData.length - 1) * xFrom);
        fromDataIndex = fromDataIndex > 0 ? fromDataIndex - 1 : 0;
        int toDataIndex = (int) ((aData.length - 1) * xTo);
        toDataIndex = toDataIndex < aData.length - 1 ? toDataIndex + 1 : aData.length - 1;
        float dataFromX = fromDataIndex * xInterval + xOffset + 50;
//        Log.w(TAG, "ololo: fromDataIndex " + fromDataIndex + ", toDataIndex " + toDataIndex + ", dataFromX " + dataFromX);

        Path path = aPath;
        path.reset();
        path.moveTo(dataFromX, viewHeight - charts.get(0).heightInterval * aData[(int) fromDataIndex]);
        for (int i = 1; dataFromX + xInterval * i < viewWidth + xInterval; i++) {
            try {
                path.lineTo(dataFromX + xInterval * i, viewHeight - charts.get(0).heightInterval * aData[fromDataIndex + i]);
            } catch (ArrayIndexOutOfBoundsException e) {
//                Log.e(TAG, "ololo: i " + i + ", " + ", fromDataIndex + i = " + (fromDataIndex + i));
                break;
            }
        }
        aPath = path;
        aPaint = charts.get(0).paint;
    }

    private void updateView2() {
        ololo();
        invalidate();
    }


}

