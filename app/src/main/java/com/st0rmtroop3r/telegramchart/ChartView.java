package com.st0rmtroop3r.telegramchart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class ChartView extends View {

    private final static String TAG = ChartView.class.getSimpleName();

    protected final ArrayList<Chart> charts = new ArrayList<>();
    private int yAxisMaxValue = 0;
    protected int xAxisLength = 0;
    protected float xAxisInterval = 0;
    protected int viewWidth = 0;
    protected int viewHeight = 0;

    public ChartView(Context context) {
        super(context);
    }

    public ChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (Chart chart : charts) {
            canvas.drawPath(chart.path, chart.paint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.w(TAG, "onSizeChanged: w: " + w + ", h: " + h);
        viewWidth = w;
        viewHeight = h;
        setupChartsPath();
    }

    void setChartsData(List<Pair<int[], Integer>> chartsData) {
        yAxisMaxValue = 0;
        for (Pair<int[], Integer> pair : chartsData) {
            int[] data = pair.first;
            Chart chart = new Chart(data, pair.second);
            charts.add(chart);
            if (yAxisMaxValue < chart.yAxisMax) {
                yAxisMaxValue = chart.yAxisMax;
            }
            xAxisLength = data.length;

        }
        if (viewWidth > 0 && viewHeight > 0) {
            Log.w(TAG, "setChartsData: viewWidth: " + viewWidth + ", viewHeight: " + viewHeight );
            setupChartsPath();
        }
    }

    protected void setupChartsPath() {

        if (xAxisLength < 3) {
            xAxisInterval = xAxisLength;
        } else {
            xAxisInterval = (float) viewWidth / (xAxisLength - 2);
            Log.w(TAG, "setChartsData: " + viewWidth + " / " + (xAxisLength - 2) + " = " + xAxisInterval);
        }
        for (Chart chart : charts) {
            chart.setupPath();
        }
        invalidate();
    }

    class Chart {
        Path path = new Path();
        Paint paint = new Paint();
        int[] data;

        int yAxisMax = 0;
        float widthInterval;
        float heightInterval;

        Chart(int[] data, int color) {
            this.data = data;
            Log.w(TAG, "Chart: data.length = " + data.length);
            for (int aData : data) {
                if (aData > yAxisMax) yAxisMax = aData;
            }

            paint.setColor(color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(10f);
        }

        void setupPath() {
//            Log.w(TAG, "setupPath: fromDataIndex: " + fromDataIndex + ", toDataIndex: " + toDataIndex + ", range = " + dataRange);

            if (data.length < 3) {
                widthInterval = data.length;
            } else {
                widthInterval = (float) viewWidth / data.length;
            }


            if (yAxisMaxValue == 0) {
                heightInterval = 0;
            } else {
                heightInterval = (float) viewHeight / yAxisMaxValue;
            }

            path.reset();
            path.moveTo(widthInterval, viewHeight - heightInterval * data[0]);
            for (int i = 1; i < data.length; i++) {
                path.lineTo(widthInterval * i, viewHeight - heightInterval * data[i]);
            }

        }
    }

}
