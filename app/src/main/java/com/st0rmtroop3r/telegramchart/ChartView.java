package com.st0rmtroop3r.telegramchart;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.st0rmtroop3r.telegramchart.enitity.Chart;
import com.st0rmtroop3r.telegramchart.enitity.ChartLine;

import java.util.ArrayList;

public class ChartView extends View {

    private final static String TAG = ChartView.class.getSimpleName();

    protected final ArrayList<ChartLineView> chartLines = new ArrayList<>();
    protected int yAxisMaxValue = 0;
    protected int targetYAxisMaxValue = 0;
    protected int xAxisLength = 0;
    protected float xInterval = 0;
    protected int viewWidth = 0;
    protected int viewHeight = 0;
    protected float xFrom = 0;
    protected float xTo = 1;
    protected float totalScaledWidth;
    protected float xOffset;
    protected int paddingTop = 0;
    protected int paddingBottom = 0;
    protected int leftPadding = 0;
    protected int rightPadding = 0;
    protected int totalXPadding = leftPadding + rightPadding;
    protected float chartStrokeWidth = 10;
    private ValueAnimator yAxisMaxValueAnimator;

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
        for (int i = chartLines.size() - 1; i >= 0 ; i--) {
            ChartLineView lineView = chartLines.get(i);
            if (lineView.draw) {
                canvas.drawPath(lineView.path, lineView.paint);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        paddingTop = getPaddingTop();
        paddingBottom = getPaddingBottom();
        leftPadding = getPaddingLeft();
        rightPadding = getPaddingRight();
        totalXPadding = leftPadding + rightPadding;
        updateView();
    }

    void setYAxisMaxValue(int newValue) {
        if (newValue == targetYAxisMaxValue) return;
        targetYAxisMaxValue = newValue;
        int oldValue = yAxisMaxValue;

        if (yAxisMaxValueAnimator != null) {
            oldValue = (int) yAxisMaxValueAnimator.getAnimatedValue();
            yAxisMaxValueAnimator.cancel();
            yAxisMaxValueAnimator.removeAllUpdateListeners();
        }
        yAxisMaxValueAnimator = ValueAnimator.ofInt(oldValue, targetYAxisMaxValue);
        yAxisMaxValueAnimator.addUpdateListener(animation -> {
            yAxisMaxValue = (int) animation.getAnimatedValue();
            updateView();
        });
        yAxisMaxValueAnimator.setDuration(300);
        yAxisMaxValueAnimator.start();
    }

    void setChartsData(Chart chart) {
        xAxisLength = chart.xData.length - 1;
        yAxisMaxValue = 0;
        chartLines.clear();
        for (ChartLine line : chart.chartLines) {
            ChartLineView chartLineView = new ChartLineView(line.yData,
                    Color.parseColor(line.color), line.name, line.id, line.visible);
            chartLines.add(chartLineView);
            if (yAxisMaxValue < chartLineView.yAxisMax) {
                yAxisMaxValue = chartLineView.yAxisMax;
            }
        }
        yAxisMaxValueAnimator = ValueAnimator.ofInt(yAxisMaxValue, yAxisMaxValue);
        if (viewWidth > 0 && viewHeight > 0) {
            updateView();
        }
    }

    void setZoomRange(float fromPercent, float toPercent) {
        xFrom = fromPercent;
        xTo = toPercent;
        updateView();
    }

    void setLineVisible(String lineId, boolean visible) {
        for (ChartLineView line : chartLines) {
            if (line.id.equals(lineId)) {
                line.setVisible(visible);
            }
        }
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

        for (ChartLineView chart : chartLines) {
            chart.heightInterval = (float) (viewHeight - paddingBottom - paddingTop) / yAxisMaxValue;
            chart.path.reset();
            chart.path.moveTo(dataFromX, viewHeight - paddingBottom - chart.heightInterval * chart.data[fromDataIndex]);
        }

        for (int i = 1; dataFromX + xInterval * i < viewWidth + xInterval; i++) {

            for (ChartLineView chart : chartLines) {
                try {
                    chart.path.lineTo(dataFromX + xInterval * i,
                            viewHeight - paddingBottom - chart.heightInterval * chart.data[fromDataIndex + i]);
                } catch (ArrayIndexOutOfBoundsException e) {}
            }
        }
    }

    class ChartLineView {

        Path path = new Path();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int[] data;
        String name;
        String id;
        int color;
        int yAxisMax = 0;
        float heightInterval;
        boolean draw = true;
        boolean visible = true;
        ChartLineAnimator animator;

        ChartLineView(int[] data, int color, String name, String id, boolean visible) {
            this.data = data;
            this.name = name;
            this.id = id;
            this.color = color;
            this.draw = visible;
            this.visible = visible;
            for (int aData : data) {
                if (aData > yAxisMax) yAxisMax = aData;
            }

            paint.setColor(color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(chartStrokeWidth);

            animator = new ChartLineAnimator(this);
        }

        void setVisible(boolean visible) {
            this.visible = visible;
            if (visible) {
                animator.showChartLine();
            } else {
                animator.hideChartLine();
            }
        }
    }

    class ChartLineAnimator {

        long animationDuration = 300;
        ChartLineView lineView;
        ValueAnimator alphaInAnimator = ValueAnimator.ofInt(0, 255);
        ValueAnimator alphaOutAnimator = ValueAnimator.ofInt(255, 0);
        AlphaUpdateListener alphaListener;
        AnimationListener animationListener;

        ChartLineAnimator(ChartLineView view) {
            lineView = view;
            alphaInAnimator.setDuration(animationDuration);
            alphaOutAnimator.setDuration(animationDuration);
            alphaListener = new AlphaUpdateListener(view);
            animationListener = new AnimationListener(view);
        }

        void showChartLine() {

            lineView.draw = true;

            int alphaFrom = 0;
            if (alphaOutAnimator.isStarted()) {
                alphaOutAnimator.cancel();
                alphaFrom = (int) alphaOutAnimator.getAnimatedValue();
            }

            alphaInAnimator = ValueAnimator.ofInt(alphaFrom, 255);
            setupAnimator(alphaInAnimator, alphaListener);

            alphaInAnimator.start();
        }

        void hideChartLine() {

            lineView.draw = true;

            int alphaFrom = 255;
            if (alphaInAnimator.isStarted()) {
                alphaInAnimator.cancel();
                alphaFrom = (int) alphaInAnimator.getAnimatedValue();
            }

            alphaOutAnimator = ValueAnimator.ofInt(alphaFrom, 0);
            setupAnimator(alphaOutAnimator, alphaListener);

            alphaOutAnimator.start();
        }

        void setupAnimator(ValueAnimator animator, ValueAnimator.AnimatorUpdateListener listener) {
            animator.addUpdateListener(listener);
            animator.addListener(animationListener);
            animator.setDuration(animationDuration);
        }
    }

    class AlphaUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        ChartLineView lineView;

        AlphaUpdateListener(ChartLineView view) {
            lineView = view;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            lineView.paint.setAlpha((int) animation.getAnimatedValue());
            updateView();
        }
    }

    class AnimationListener extends AnimatorListenerAdapter {

        ChartLineView lineView;

        AnimationListener(ChartLineView view) {
            lineView = view;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            ((ValueAnimator)animation).removeAllUpdateListeners();
            animation.removeAllListeners();
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            lineView.draw = lineView.visible;
            ((ValueAnimator)animation).removeAllUpdateListeners();
            animation.removeAllListeners();
        }
    }

}
