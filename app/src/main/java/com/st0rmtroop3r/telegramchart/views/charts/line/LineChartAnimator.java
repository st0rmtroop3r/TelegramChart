package com.st0rmtroop3r.telegramchart.views.charts.line;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.st0rmtroop3r.telegramchart.views.ChartGraphPreview;
import com.st0rmtroop3r.telegramchart.views.ChartGraphView;
import com.st0rmtroop3r.telegramchart.views.charts.ChartAnimator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LineChartAnimator extends ChartAnimator<LineChart> {

    private static final String TAG = LineChartAnimator.class.getSimpleName();

    private HashMap<String, AlphaAnimationHolder> animationHolders = new HashMap<>();
    private MinMaxAnimator previewMinMaxAnimator = new MinMaxAnimator();
    private MinMaxAnimator reactiveMinMaxAnimator = new MinMaxAnimator();

    public LineChartAnimator() {
        minMaxValues = new int[2];
    }

    @Override
    public void setChartPreview(LineChart chart) {
        super.setChartPreview(chart);
        addLineToHolder(chart.chartLines);
        previewMinMaxAnimator.chart = chart;
    }

    @Override
    public void setChart(LineChart chart) {
        super.setChart(chart);
        addLineToHolder(chart.chartLines);
        reactiveMinMaxAnimator.chart = chart;
    }

    @Override
    public void setGraphPreview(ChartGraphPreview graphPreview) {
        super.setGraphPreview(graphPreview);
        previewMinMaxAnimator.view = graphPreview;
    }

    @Override
    public void setGraphView(ChartGraphView graphView) {
        super.setGraphView(graphView);
        reactiveMinMaxAnimator.view = graphView;
    }

    public void onSeriesCheckChanged(String seriesId, boolean checked) {

        AlphaAnimationHolder animationHolder = animationHolders.get(seriesId);
        if (animationHolder != null) {
            animationHolder.statusChanged(checked);
        }

        int previewNewMin = Integer.MAX_VALUE;
        int previewNewMax = Integer.MIN_VALUE;
        int reactiveNewMin = Integer.MAX_VALUE;
        int reactiveNewMax = Integer.MIN_VALUE;

        for (LineChart.Line line : chartPreview.chartLines) {
            if (!line.checked) continue;
            if (line.min < previewNewMin) previewNewMin = line.min;
            if (line.max > previewNewMax) previewNewMax = line.max;
        }
        for (LineChart.Line line : chart.chartLines) {
            if (!line.checked) continue;
            if (line.min < reactiveNewMin) reactiveNewMin = line.min;
            if (line.max > reactiveNewMax) reactiveNewMax = line.max;
        }

        if (chartPreview.chartMinValue != previewNewMin || chartPreview.chartMaxValue != previewNewMax) {
            previewMinMaxAnimator.startAnimation(chartPreview.chartMinValue, previewNewMin,
                    chartPreview.chartMaxValue, previewNewMax);
        }
        reactiveNewMin = reactiveNewMin / 5 * 5;
        reactiveNewMax = reactiveNewMax / 5 * 5;
        if (chart.chartMinValue != reactiveNewMin || chart.chartMaxValue != reactiveNewMax) {
            reactiveMinMaxAnimator.startAnimation(chart.chartMinValue, reactiveNewMin,
                    chart.chartMaxValue, reactiveNewMax);
            minMaxValues[0] = reactiveNewMin;
            minMaxValues[1] = reactiveNewMax;
            yAxis.setMinMaxValues(minMaxValues);
        }
    }

    public void onSelectedRangeChanged(float rangeFrom, float rangeTo) {
        chart.selectRange(rangeFrom, rangeTo);
        chart.findMinMaxValues();

        int reactiveNewMin = Integer.MAX_VALUE;
        int reactiveNewMax = Integer.MIN_VALUE;
        for (LineChart.Line line : chart.chartLines) {
            if (!line.checked) continue;
            if (line.min < reactiveNewMin) reactiveNewMin = line.min;
            if (line.max > reactiveNewMax) reactiveNewMax = line.max;
        }

        reactiveNewMin = reactiveNewMin / 5 * 5;
        reactiveNewMax = reactiveNewMax / 5 * 5;
        if (chart.chartMinValue != reactiveNewMin || chart.chartMaxValue != reactiveNewMax) {
            reactiveMinMaxAnimator.startAnimation(chart.chartMinValue, reactiveNewMin,
                    chart.chartMaxValue, reactiveNewMax);
            minMaxValues[0] = reactiveNewMin;
            minMaxValues[1] = reactiveNewMax;
            yAxis.setMinMaxValues(minMaxValues);
        } else {
            chart.setupLines();
            graphView.invalidate();
        }

    }

    @Override
    public void setInitialDataRange(float rangeFrom, float rangeTo) {

        chart.selectRange(rangeFrom, rangeTo);
        chart.findMinMaxValues();

        int newMin = Integer.MAX_VALUE;
        int newMax = Integer.MIN_VALUE;
        for (LineChart.Line line : chart.chartLines) {
            if (!line.draw) continue;
            if (line.min < newMin) newMin = line.min;
            if (line.max > newMax) newMax = line.max;
        }

        newMin = newMin / 5 * 5;
        newMax = newMax / 5 * 5;
        chart.setChartMaxMinValues(newMin, newMax);
        chart.setupLines();

        minMaxValues[0] = newMin;
        minMaxValues[1] = newMax;
        yAxis.setMinMaxValues(minMaxValues);
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

        int previewNewMin = Integer.MAX_VALUE;
        int previewNewMax = Integer.MIN_VALUE;
        int reactiveNewMin = Integer.MAX_VALUE;
        int reactiveNewMax = Integer.MIN_VALUE;

        for (LineChart.Line line : chartPreview.chartLines) {
            if (!line.checked) continue;
            if (line.min < previewNewMin) previewNewMin = line.min;
            if (line.max > previewNewMax) previewNewMax = line.max;
        }
        for (LineChart.Line line : chart.chartLines) {
            if (!line.checked) continue;
            if (line.min < reactiveNewMin) reactiveNewMin = line.min;
            if (line.max > reactiveNewMax) reactiveNewMax = line.max;
        }

        if (chartPreview.chartMinValue != previewNewMin || chartPreview.chartMaxValue != previewNewMax) {
            previewMinMaxAnimator.startAnimation(chartPreview.chartMinValue, previewNewMin,
                    chartPreview.chartMaxValue, previewNewMax);
        }
        reactiveNewMin = reactiveNewMin / 5 * 5;
        reactiveNewMax = reactiveNewMax / 5 * 5;
        if (chart.chartMinValue != reactiveNewMin || chart.chartMaxValue != reactiveNewMax) {
            reactiveMinMaxAnimator.startAnimation(chart.chartMinValue, reactiveNewMin,
                    chart.chartMaxValue, reactiveNewMax);

            minMaxValues[0] = reactiveNewMin;
            minMaxValues[1] = reactiveNewMax;
            yAxis.setMinMaxValues(minMaxValues);
        }
    }

    private void addLineToHolder(List<LineChart.Line> chartLines) {
        for (LineChart.Line line : chartLines) {
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

        View view;
        LineChart chart;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.2f, 1);
        int fromMin, diffMin, lastMin, pendingToMin, fromMax, diffMax, lastMax, pendingToMax;
        boolean pendingTo = false;

        MinMaxAnimator() {
            valueAnimator.setDuration(200);
            valueAnimator.addUpdateListener(animation -> {
                float diffMultiplier = (float) animation.getAnimatedValue();
                lastMin = (int) (fromMin + diffMin * diffMultiplier);
                lastMax = (int) (fromMax + diffMax * diffMultiplier);
                chart.setChartMaxMinValues(lastMin, lastMax);
                chart.setupLines();
                view.invalidate();
            });
            valueAnimator.setInterpolator(new DecelerateInterpolator());
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (pendingTo) {
                        fromMin = lastMin;
                        fromMax = lastMax;
                        diffMin = pendingToMin - fromMin;
                        diffMax = pendingToMax - fromMax;
                        pendingTo = false;
                        valueAnimator.start();
                    }
                }
            });
        }

        void startAnimation(int fromMinY, int toMinY, int fromMaxY, int toMaxY) {
            if (valueAnimator.isStarted()) {
                pendingToMin = toMinY;
                pendingToMax = toMaxY;
                pendingTo = true;
                return;
            }
            fromMin = fromMinY;
            fromMax = fromMaxY;
            diffMin = toMinY - fromMinY;
            diffMax = toMaxY - fromMaxY;
            valueAnimator.start();
        }
    }

    class AlphaAnimationHolder {
        String lineId;
        boolean checked;
        List<LineChart.Line> lines = new ArrayList<>();
        LineAlphaAnimator alphaAnimator = new LineAlphaAnimator(lines);

        AlphaAnimationHolder(String lineId) {
            this.lineId = lineId;
        }

        void addLine(LineChart.Line line) {
            lines.add(line);
        }

        void statusChanged(boolean visible) {
            checked = visible;
            for (LineChart.Line line : lines) {
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
        List<LineChart.Line> lineView;
        ValueAnimator alphaInAnimator = ValueAnimator.ofInt(0, 255);
        ValueAnimator alphaOutAnimator = ValueAnimator.ofInt(255, 0);
        AlphaUpdateListener alphaListener;
        AnimationListener animationListener;

        LineAlphaAnimator(List<LineChart.Line> view) {
            lineView = view;
            alphaInAnimator.setDuration(animationDuration);
            alphaOutAnimator.setDuration(animationDuration);
            alphaListener = new AlphaUpdateListener(view);
            animationListener = new AnimationListener(view);
        }

        void showChartLine() {

            for (LineChart.Line line : lineView) {
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

            for (LineChart.Line line : lineView) {
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

        List<LineChart.Line> lines;

        AlphaUpdateListener(List<LineChart.Line> lineList) {
            lines = lineList;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            for (LineChart.Line line : lines) {
                line.paint.setAlpha((int) animation.getAnimatedValue());
            }

            graphView.invalidate();
            graphPreview.invalidate();
        }
    }

    class AnimationListener extends AnimatorListenerAdapter {

        List<LineChart.Line> lines;

        AnimationListener(List<LineChart.Line> lineList) {
            lines = lineList;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            ((ValueAnimator)animation).removeAllUpdateListeners();
            animation.removeAllListeners();
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            for (LineChart.Line line : lines) {
                line.draw = line.checked;
            }
            ((ValueAnimator)animation).removeAllUpdateListeners();
            animation.removeAllListeners();
        }
    }
}
