package com.st0rmtroop3r.telegramchart.views.charts.area;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;

import com.st0rmtroop3r.telegramchart.views.charts.ChartAnimator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AreaChartAnimator extends ChartAnimator<AreaChart> {

    private final static String TAG = AreaChartAnimator.class.getSimpleName();
    private HashMap<String, VisibilityAnimationHolder> animationHolders = new HashMap<>();

    @Override
    public void setChartPreview(AreaChart chartPreview) {
        super.setChartPreview(chartPreview);
        addBarsToHolder(chartPreview.areas);
    }

    @Override
    public void setChart(AreaChart chart) {
        super.setChart(chart);
        addBarsToHolder(chart.areas);
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
        chart.setupPaths();
        graphView.invalidate();
    }

    @Override
    public void setInitialDataRange(float rangeFrom, float rangeTo) {
        chart.selectRange(rangeFrom, rangeTo);
        chart.setupPaths();
        chartPreview.selectRange(0, 1);
        chartPreview.setupPaths();
        graphView.invalidate();
        graphPreview.invalidate();
//        yAxis.setYAxisMaxValue(0 , 100);
        int[] minMax = new int[2];
        minMax[1] = 100;
        yAxis.setMinMaxValues(minMax);
    }

    @Override
    public void setOnlyOneSeriesSelected(String seriesId) {
        for (VisibilityAnimationHolder holder : animationHolders.values()) {
            if (holder.areaId.equals(seriesId)) {
                if (!holder.checked) holder.statusChanged(true);
            } else {
                if (holder.checked) holder.statusChanged(false);
            }
        }
    }

    private void addBarsToHolder(List<AreaChart.Area> chartBars) {
        for (AreaChart.Area bar : chartBars) {
            VisibilityAnimationHolder holder = animationHolders.get(bar.id);
            if (holder == null) {
                holder = new VisibilityAnimationHolder(bar.id);
                holder.checked = bar.checked;
                holder.addArea(bar);
                animationHolders.put(bar.id, holder);
            } else {
                holder.addArea(bar);
            }
        }
    }

    class VisibilityAnimationHolder {
        String areaId;
        boolean checked;
        List<AreaChart.Area> areas = new ArrayList<>();
        VisibilityAnimator alphaAnimator = new VisibilityAnimator(areas);

        VisibilityAnimationHolder(String areaId) {
            this.areaId = areaId;
        }

        void addArea(AreaChart.Area area) {
            areas.add(area);
        }

        void statusChanged(boolean visible) {
            checked = visible;
            for (AreaChart.Area area : areas) {
                area.checked = visible;
            }
            if (visible) {
                alphaAnimator.showChartArea();
            } else {
                alphaAnimator.hideChartArea();
            }
        }
    }

    class VisibilityAnimator {

        long animationDuration = 300;
        List<AreaChart.Area> areaView;
        final float visibilityNone = 0;
        final float visibilityFull = 1;
        ValueAnimator alphaInAnimator = ValueAnimator.ofFloat(visibilityNone, visibilityFull);
        ValueAnimator alphaOutAnimator = ValueAnimator.ofFloat(visibilityFull, visibilityNone);
        AlphaUpdateListener alphaListener;
        AnimationListener animationListener;

        VisibilityAnimator(List<AreaChart.Area> view) {
            areaView = view;
            alphaInAnimator.setDuration(animationDuration);
            alphaOutAnimator.setDuration(animationDuration);
            alphaListener = new AlphaUpdateListener(view);
            animationListener = new AnimationListener(view);
        }

        void showChartArea() {

            for (AreaChart.Area area : areaView) {
                area.draw = true;
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

        void hideChartArea() {

            for (AreaChart.Area area : areaView) {
                area.draw = true;
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

        List<AreaChart.Area> areas;

        AlphaUpdateListener(List<AreaChart.Area> areaList) {
            areas = areaList;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            for (AreaChart.Area area : areas) {
                float value = (float) animation.getAnimatedValue();
                area.paint.setAlpha((int) (100 + 155 * value));
                area.visibility = value;
            }
            chart.recalcStacksPercents();
            chart.prepareInvalidate();
            chartPreview.recalcStacksPercents();
            chartPreview.prepareInvalidate();
            graphView.invalidate();
            graphPreview.invalidate();
        }
    }

    class AnimationListener extends AnimatorListenerAdapter {

        List<AreaChart.Area> areas;

        AnimationListener(List<AreaChart.Area> areaList) {
            areas = areaList;
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
            for (AreaChart.Area area : areas) {
                area.draw = area.checked;
            }
//            chart.calcChartMaxStack();
        }
    }
}
