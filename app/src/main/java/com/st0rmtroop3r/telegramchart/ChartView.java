package com.st0rmtroop3r.telegramchart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.st0rmtroop3r.telegramchart.enitity.Chart;
import com.st0rmtroop3r.telegramchart.enitity.ChartLine;

import java.util.ArrayList;

public class ChartView extends View {

    private final static String TAG = ChartView.class.getSimpleName();

    protected final ArrayList<ChartLineView> charts = new ArrayList<>();
    protected int yAxisMaxValue = 0;
    protected int xAxisLength = 0;
    protected float xInterval = 0;
    protected int viewWidth = 0;
    protected int viewHeight = 0;
    protected float xFrom = 0;
    protected float xTo = 1;
    protected float totalScaledWidth;
    protected float xOffset;
    protected int leftPadding = 0;
    protected int rightPadding = 0;
    protected int totalXPadding = leftPadding + rightPadding;
    protected float chartStrokeWidth = 10;

    public ChartView(Context context) {
        super(context);
    }

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (ChartLineView chart : charts) {
            canvas.drawPath(chart.path, chart.paint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.w(TAG, "onSizeChanged: w: " + w + ", h: " + h);
        viewWidth = w;
        viewHeight = h;
        leftPadding = getPaddingLeft();
        rightPadding = getPaddingRight();
        totalXPadding = leftPadding + rightPadding;
        Log.w(TAG, "onSizeChanged: rightPadding = " + rightPadding + ", leftPadding = " + leftPadding);
        updateView();
    }

    void setChartsData(Chart chart) {
        xAxisLength = chart.xData.length - 1;
        yAxisMaxValue = 0;
        charts.clear();
        for (ChartLine chartLine : chart.chartLines) {
            ChartLineView chartLineView = new ChartLineView(chartLine.yData, Color.parseColor(chartLine.color));
            charts.add(chartLineView);
            if (yAxisMaxValue < chartLineView.yAxisMax) {
                yAxisMaxValue = chartLineView.yAxisMax;
            }
        }
        if (viewWidth > 0 && viewHeight > 0) {
            updateView();
        }
    }

    void setZoomRange(float fromPercent, float toPercent) {
        xFrom = fromPercent;
        xTo = toPercent;
        updateView();
    }

    protected void updateView() {

        float range = xTo - xFrom;
        totalScaledWidth = viewWidth / range;
        xInterval = (totalScaledWidth - totalXPadding) / xAxisLength;

        xOffset = -totalScaledWidth * xFrom;

        int fromDataIndex = (int) (xAxisLength * xFrom);
        fromDataIndex = fromDataIndex > 0 ? fromDataIndex - 1 : 0;

        float dataFromX = fromDataIndex * xInterval + xOffset + leftPadding;

        setupChartsPath(dataFromX, fromDataIndex);

        invalidate();
    }

    private void setupChartsPath(float dataFromX, int fromDataIndex) {

        for (ChartLineView chart : charts) {
            chart.heightInterval = (float) viewHeight / yAxisMaxValue;
            chart.path.reset();
            chart.path.moveTo(dataFromX, viewHeight - chart.heightInterval * chart.data[fromDataIndex]);
        }

        for (int i = 1; dataFromX + xInterval * i < viewWidth + xInterval; i++) {

            for (ChartLineView chart : charts) {
                try {
                    chart.path.lineTo(dataFromX + xInterval * i,
                            viewHeight - chart.heightInterval * chart.data[fromDataIndex + i]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    break;
                }
            }
        }
    }

    class ChartLineView {

        Path path = new Path();
        Paint paint = new Paint();
        int[] data;
        int yAxisMax = 0;
        float heightInterval;

        ChartLineView(int[] data, int color) {
            this.data = data;
            Log.w(TAG, "ChartLineView: data.length = " + data.length);
            for (int aData : data) {
                if (aData > yAxisMax) yAxisMax = aData;
            }

            paint.setColor(color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(chartStrokeWidth);

        }
    }

}
