package com.st0rmtroop3r.telegramchart.views.charts;

import com.st0rmtroop3r.telegramchart.views.ChartDrawable;
import com.st0rmtroop3r.telegramchart.views.ChartGraphPreview;
import com.st0rmtroop3r.telegramchart.views.ChartGraphView;

public abstract class ChartAnimator <T extends ChartDrawable> {

    protected T chartPreview;
    protected T chart;
    protected ChartGraphPreview graphPreview;
    protected ChartGraphView graphView;
    public YAxis yAxis;
    protected int[] minMaxValues;

    public abstract void onSeriesCheckChanged(String seriesId, boolean checked);

    public abstract void onSelectedRangeChanged(float rangeFrom, float rangeTo);

    public abstract void setInitialDataRange(float rangeFrom, float rangeTo);

    public abstract void setOnlyOneSeriesSelected(String id);

    public void setChartPreview(T chartPreview) {
        this.chartPreview = chartPreview;
    }

    public void setChart(T chart) {
        this.chart = chart;
    }

    public void setGraphPreview(ChartGraphPreview graphPreview) {
        this.graphPreview = graphPreview;
    }

    public void setGraphView(ChartGraphView graphView) {
        this.graphView = graphView;
    }
}
