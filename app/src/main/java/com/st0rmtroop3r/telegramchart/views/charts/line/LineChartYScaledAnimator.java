package com.st0rmtroop3r.telegramchart.views.charts.line;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.st0rmtroop3r.telegramchart.views.charts.ChartAnimator;
import com.st0rmtroop3r.telegramchart.views.charts.YAxis2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LineChartYScaledAnimator extends ChartAnimator<LineChartYScaled> {

    private static final String TAG = LineChartYScaledAnimator.class.getSimpleName();
    private final HashMap<String, MinMaxAnimator> mimMaxMap = new HashMap<>();
    private HashMap<String, AlphaAnimationHolder> animationHolders = new HashMap<>();
    private String[] ids;

    @Override
    public void setChart(LineChartYScaled chart) {
        super.setChart(chart);
        for (LineChartYScaled.Line line : chart.chartLines) {
            MinMaxAnimator animator = mimMaxMap.get(line.id);
            if (animator == null) {
                animator = new MinMaxAnimator();
                animator.line = line;
                mimMaxMap.put(line.id, animator);
            } else {
                animator.line = line;
            }
        }
        addLineToHolder(chart.chartLines);
        minMaxValues = new int[chart.chartLines.size() * 2];
        ((YAxis2) yAxis).setLeftTextColor(chart.chartLines.get(0).color);
        ((YAxis2) yAxis).setRightTextColor(chart.chartLines.get(1).color);
        ids = new String[2];
        ids[0] = chart.chartLines.get(0).id;
        ids[1] = chart.chartLines.get(1).id;
    }

    @Override
    public void setChartPreview(LineChartYScaled chartPreview) {
        super.setChartPreview(chartPreview);
        addLineToHolder(chartPreview.chartLines);
    }

    @Override
    public void onSeriesCheckChanged(String seriesId, boolean checked) {
        Log.i(TAG, "onSeriesCheckChanged: seriesId " + seriesId + ", checked " + checked);
        AlphaAnimationHolder animationHolder = animationHolders.get(seriesId);
        if (animationHolder != null) {
            animationHolder.statusChanged(checked);
        }
        if (ids[0].equals(seriesId)) {
            ((YAxis2) yAxis).setLeftValuesEnabled(checked);

        }
        if (ids[1].equals(seriesId)) {
            ((YAxis2) yAxis).setRightValuesEnabled(checked);
        }
    }

    @Override
    public void onSelectedRangeChanged(float rangeFrom, float rangeTo) {
        chart.selectRange(rangeFrom, rangeTo);
        graphView.invalidate();

        chart.findNewMinMaxValues();
        int i = 0;
        for (LineChartYScaled.Line line : chart.chartLines) {
            MinMaxAnimator animator = mimMaxMap.get(line.id);
            if (animator != null) {
                animator.startAnimation(line.minNew, line.maxNew);
            } else {
                Log.e(TAG, "onSelectedRangeChanged: animator is null for id: " + line.id);
            }
            minMaxValues[i++] = line.minNew;
            minMaxValues[i++] = line.maxNew;
        }
        yAxis.setMinMaxValues(minMaxValues);
    }

    @Override
    public void setInitialDataRange(float rangeFrom, float rangeTo) {
        chart.selectRange(rangeFrom, rangeTo);
        int i = 0;
        for (LineChartYScaled.Line line : chart.chartLines) {
            line.findMinMaxValues();
            line.updateYInterval();
            minMaxValues[i++] = line.minNew;
            minMaxValues[i++] = line.maxNew;
        }
        yAxis.setMinMaxValues(minMaxValues);
        chart.setupLines();
        graphView.invalidate();
    }

    @Override
    public void setOnlyOneSeriesSelected(String seriesId) {
        for (AlphaAnimationHolder holder : animationHolders.values()) {
            if (holder.lineId.equals(seriesId)) {
                if (!holder.checked) holder.statusChanged(true);
            } else {
                if (holder.checked) holder.statusChanged(false);
            }
        }
        if (ids[1].equals(seriesId)) {
            ((YAxis2) yAxis).setLeftValuesEnabled(false);
        }
        if (ids[0].equals(seriesId)) {
            ((YAxis2) yAxis).setRightValuesEnabled(false);
        }
    }

    private void addLineToHolder(List<LineChartYScaled.Line> chartLines) {
        for (LineChartYScaled.Line line : chartLines) {
            AlphaAnimationHolder holder = animationHolders.get(line.id);
            if (holder == null) {
                holder = new AlphaAnimationHolder(line.id);
                holder.checked = line.checked;
                holder.addLine(line);
                animationHolders.put(line.id, holder);
            } else {
                holder.addLine(line);
            }
        }
    }

    class MinMaxAnimator {

        int minFrom, minLast, minDiff, minTo, maxFrom, maxLast, maxDiff, maxTo;
        ValueAnimator animator;
        LineChartYScaled.Line line;

        void startAnimation(int newMin, int newMax) {
            if (newMin == minTo && newMax == maxTo) return;
            if (animator == null) {
                minLast = newMin;
                maxLast = newMax;
                animator = getAnimator();
                return;
            }
            animator.cancel();
            minFrom = minLast;
            maxFrom = maxLast;
            minDiff = newMin - minFrom;
            maxDiff = newMax - maxFrom;
            animator.start();
        }

        private ValueAnimator getAnimator() {
            ValueAnimator newAnimator = ValueAnimator.ofFloat(0.2f, 1);
            newAnimator.setDuration(200);
            newAnimator.addUpdateListener(animation -> {
                float factor = (float) animation.getAnimatedValue();
                minLast = (int) (minFrom + minDiff * factor);
                maxLast = (int) (maxFrom + maxDiff * factor);
                line.setLineMaxMinValues(minLast, maxLast);
                graphView.invalidate();
            });
            return newAnimator;
        }
    }

    class AlphaAnimationHolder {
        String lineId;
        boolean checked;
        List<LineChartYScaled.Line> lines = new ArrayList<>();
        LineAlphaAnimator alphaAnimator = new LineAlphaAnimator(lines);

        AlphaAnimationHolder(String lineId) {
            this.lineId = lineId;
        }

        void addLine(LineChartYScaled.Line line) {
            lines.add(line);
        }

        void statusChanged(boolean visible) {
            checked = visible;
            for (LineChartYScaled.Line line : lines) {
                line.checked = visible;
            }
            if (visible) {
                alphaAnimator.showChartLine();
            } else {
                alphaAnimator.hideChartLine();
            }
        }
    }

    class LineAlphaAnimator {

        long animationDuration = 300;
        List<LineChartYScaled.Line> lineView;
        ValueAnimator alphaInAnimator = ValueAnimator.ofInt(0, 255);
        ValueAnimator alphaOutAnimator = ValueAnimator.ofInt(255, 0);
        AlphaUpdateListener alphaListener;
        AnimationListener animationListener;

        LineAlphaAnimator(List<LineChartYScaled.Line> view) {
            lineView = view;
            alphaInAnimator.setDuration(animationDuration);
            alphaOutAnimator.setDuration(animationDuration);
            alphaInAnimator.setInterpolator(new LinearInterpolator());
            alphaOutAnimator.setInterpolator(new LinearInterpolator());
            alphaListener = new AlphaUpdateListener(view);
            animationListener = new AnimationListener(view);
        }

        void showChartLine() {

            for (LineChartYScaled.Line line : lineView) {
                line.draw = true;
            }

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

            for (LineChartYScaled.Line line : lineView) {
                line.draw = true;
            }

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

        List<LineChartYScaled.Line> lines;

        AlphaUpdateListener(List<LineChartYScaled.Line> lineList) {
            lines = lineList;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            for (LineChartYScaled.Line line : lines) {
                line.paint.setAlpha((int) animation.getAnimatedValue());
            }
            graphView.invalidate();
            graphPreview.invalidate();
        }
    }

    class AnimationListener extends AnimatorListenerAdapter {

        List<LineChartYScaled.Line> lines;

        AnimationListener(List<LineChartYScaled.Line> lineList) {
            lines = lineList;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            ((ValueAnimator)animation).removeAllUpdateListeners();
            animation.removeAllListeners();
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            for (LineChartYScaled.Line line : lines) {
                line.draw = line.checked;
            }
            ((ValueAnimator)animation).removeAllUpdateListeners();
            animation.removeAllListeners();
        }
    }
}
