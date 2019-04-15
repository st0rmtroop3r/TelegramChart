package com.st0rmtroop3r.telegramchart.views.charts.stackedbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.st0rmtroop3r.telegramchart.views.ChartGraphView;
import com.st0rmtroop3r.telegramchart.views.charts.ChartAnimator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StackedBarChartAnimator extends ChartAnimator<StackedBarChart> {

    private static final String TAG = StackedBarChartAnimator.class.getSimpleName();
    private HashMap<String, VisibilityAnimationHolder> animationHolders = new HashMap<>();
    private MaxStackAnimator maxStackAnimator = new MaxStackAnimator();
    private int maxStack = 0;

    public StackedBarChartAnimator() {
        minMaxValues = new int[2];
    }

    @Override
    public void setChartPreview(StackedBarChart chartPreview) {
        super.setChartPreview(chartPreview);
        addBarsToHolder(chartPreview.bars);
    }

    @Override
    public void setChart(StackedBarChart chart) {
        super.setChart(chart);
        addBarsToHolder(chart.bars);
        maxStackAnimator.chart = chart;
    }

    @Override
    public void setGraphView(ChartGraphView graphView) {
        super.setGraphView(graphView);
        maxStackAnimator.view = graphView;
    }

    @Override
    public void onSeriesCheckChanged(String seriesId, boolean checked) {
        VisibilityAnimationHolder animationHolder = animationHolders.get(seriesId);
        if (animationHolder != null) {
            animationHolder.statusChanged(checked);
        }
    }

    @Override
    public void onSelectedRangeChanged(float rangeFrom, float rangeTo) {
        chart.selectRange(rangeFrom, rangeTo);
        int newMaxStack = chart.calcChartMaxStack();

        if (maxStack != newMaxStack) {
            maxStackAnimator.startAnimation(maxStack, newMaxStack);
            minMaxValues[1] = maxStack;
            yAxis.setMinMaxValues(minMaxValues);
            maxStack = newMaxStack;
        } else {
            chart.setupBars();
            graphView.invalidate();
        }
    }

    @Override
    public void setInitialDataRange(float rangeFrom, float rangeTo) {
        chart.selectRange(rangeFrom, rangeTo);
        chart.prepareInvalidate();
        maxStack = chart.maxStackSum;
        minMaxValues[1] = maxStack;
        yAxis.setMinMaxValues(minMaxValues);
    }

    @Override
    public void setOnlyOneSeriesSelected(String seriesId) {
        for (VisibilityAnimationHolder holder : animationHolders.values()) {
            if (holder.barId.equals(seriesId)) {
                if (!holder.checked) holder.statusChanged(true);
            } else {
                if (holder.checked) holder.statusChanged(false);
            }
        }
    }

    private void addBarsToHolder(List<StackedBarChart.Bar> chartBars) {
        for (StackedBarChart.Bar bar : chartBars) {
            VisibilityAnimationHolder holder = animationHolders.get(bar.id);
            if (holder == null) {
                holder = new VisibilityAnimationHolder(bar.id);
                holder.checked = bar.checked;
                holder.addBar(bar);
                animationHolders.put(bar.id, holder);
            } else {
                holder.addBar(bar);
            }
        }
    }


    class MaxStackAnimator {

        View view;
        StackedBarChart chart;
        ValueAnimator valueAnimator;// = ValueAnimator.ofFloat(0, 1);
        int fromMin, diffMin, lastMin, pendingToMin, fromMax, diffMax, lastMax, pendingToMax;
        boolean pendingTo = false;

        MaxStackAnimator() {
//            valueAnimator.setDuration(300);
//            valueAnimator.addUpdateListener(animation -> {
//                float diffMultiplier = (float) animation.getAnimatedValue();
//                lastMax = (int) (fromMax + diffMax * diffMultiplier);
////                Log.w(TAG, "MaxStackAnimator: pendingToMax " + pendingToMax + ", lastMax " + lastMax);
//                chart.maxStackSum = lastMax;
//                chart.setupBars();
////                chart.setChartMaxMinValues(lastMin, lastMax);
////                chart.setupLines();
//                view.invalidate();
//            });
//            valueAnimator.addListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    Log.w(TAG, "onAnimationEnd: lastMax " + lastMax);
//                    if (pendingTo) {
//                        fromMax = lastMax;
//                        diffMax = pendingToMax - fromMax;
//                        pendingTo = false;
//                        valueAnimator.start();
//                    }
//                }
//            });
        }

        void startAnimation(int fromMaxY, int toMaxY) {
            int oldValue = fromMaxY;
            if (valueAnimator != null) {
                oldValue = (int) valueAnimator.getAnimatedValue();
                valueAnimator.cancel();
                valueAnimator.removeAllUpdateListeners();
            }
            valueAnimator = ValueAnimator.ofInt(oldValue, toMaxY);
            valueAnimator.addUpdateListener(animation -> {
                chart.maxStackSum = (int) valueAnimator.getAnimatedValue();
//                Log.w(TAG, "startAnimation: chart.maxStackSum " + chart.maxStackSum);
                chart.setupBars();
                view.invalidate();
            });
            valueAnimator.setInterpolator(new DecelerateInterpolator());
            valueAnimator.setDuration(200);
            valueAnimator.start();

//            if (valueAnimator.isStarted()) {
//                pendingToMax = toMaxY;
//                pendingTo = true;
//                return;
//            }
//            fromMax = fromMaxY;
//            diffMax = toMaxY - fromMaxY;
//            valueAnimator.start();
        }
    }

    class VisibilityAnimationHolder {
        String barId;
        boolean checked;
        List<StackedBarChart.Bar> bars = new ArrayList<>();
        BarVisibilityAnimator alphaAnimator = new BarVisibilityAnimator(bars);

        VisibilityAnimationHolder(String barId) {
            this.barId = barId;
        }

        void addBar(StackedBarChart.Bar bar) {
            bars.add(bar);
        }

        void statusChanged(boolean visible) {
            checked = visible;
            for (StackedBarChart.Bar bar : bars) {
                bar.checked = visible;
            }
            if (visible) {
                alphaAnimator.showChartBar();
            } else {
                alphaAnimator.hideChartBar();
            }
        }
    }

    class BarVisibilityAnimator {

        long animationDuration = 300;
        List<StackedBarChart.Bar> barView;
        final float visibilityNone = 0;
        final float visibilityFull = 1;
        ValueAnimator alphaInAnimator = ValueAnimator.ofFloat(visibilityNone, visibilityFull);
        ValueAnimator alphaOutAnimator = ValueAnimator.ofFloat(visibilityFull, visibilityNone);
        AlphaUpdateListener alphaListener;
        AnimationListener animationListener;

        BarVisibilityAnimator(List<StackedBarChart.Bar> view) {
            barView = view;
            alphaInAnimator.setDuration(animationDuration);
            alphaOutAnimator.setDuration(animationDuration);
            alphaListener = new AlphaUpdateListener(view);
            animationListener = new AnimationListener(view);
        }

        void showChartBar() {

            for (StackedBarChart.Bar bar : barView) {
                bar.draw = true;
            }

            float alphaFrom = visibilityNone;
            if (alphaOutAnimator.isStarted()) {
                alphaOutAnimator.cancel();
                alphaFrom = (float) alphaOutAnimator.getAnimatedValue();
            }

            alphaInAnimator = ValueAnimator.ofFloat(alphaFrom, visibilityFull);
            setupAnimator(alphaInAnimator, alphaListener);

            alphaInAnimator.start();
        }

        void hideChartBar() {

            for (StackedBarChart.Bar bar : barView) {
                bar.draw = true;
            }

            float alphaFrom = visibilityFull;
            if (alphaInAnimator.isStarted()) {
                alphaInAnimator.cancel();
                alphaFrom = (float) alphaInAnimator.getAnimatedValue();
            }

            alphaOutAnimator = ValueAnimator.ofFloat(alphaFrom, visibilityNone);
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

        List<StackedBarChart.Bar> bars;

        AlphaUpdateListener(List<StackedBarChart.Bar> barList) {
            bars = barList;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            for (StackedBarChart.Bar bar : bars) {
                float value = (float) animation.getAnimatedValue();
                bar.paint.setAlpha((int) (100 + 155 * value));
                bar.visibility = value;
            }
            chart.prepareInvalidate();
            chartPreview.prepareInvalidate();
            graphView.invalidate();
            graphPreview.invalidate();
        }
    }

    class AnimationListener extends AnimatorListenerAdapter {

        List<StackedBarChart.Bar> bars;

        AnimationListener(List<StackedBarChart.Bar> barList) {
            bars = barList;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            ((ValueAnimator)animation).removeAllUpdateListeners();
            animation.removeAllListeners();
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            ((ValueAnimator)animation).removeAllUpdateListeners();
            animation.removeAllListeners();
            for (StackedBarChart.Bar bar : bars) {
                bar.draw = bar.checked;
            }
//            chart.calcChartMaxStack();
        }
    }
}
