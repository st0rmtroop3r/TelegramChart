package com.st0rmtroop3r.telegramchart;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;

import com.st0rmtroop3r.telegramchart.views.ChartDrawable;
import com.st0rmtroop3r.telegramchart.views.charts.ChartAnimator;
import com.st0rmtroop3r.telegramchart.views.charts.ToolTip;
import com.st0rmtroop3r.telegramchart.views.charts.YAxis;
import com.st0rmtroop3r.telegramchart.views.charts.YAxis1;
import com.st0rmtroop3r.telegramchart.views.charts.YAxis2;
import com.st0rmtroop3r.telegramchart.views.charts.area.AreaChart;
import com.st0rmtroop3r.telegramchart.views.charts.area.AreaChartAnimator;
import com.st0rmtroop3r.telegramchart.views.charts.area.AreaChartToolTip;
import com.st0rmtroop3r.telegramchart.views.charts.line.AbstractLineChart;
import com.st0rmtroop3r.telegramchart.views.charts.line.LineChart;
import com.st0rmtroop3r.telegramchart.views.charts.line.LineChartAnimator;
import com.st0rmtroop3r.telegramchart.views.charts.line.LineChartToolTip;
import com.st0rmtroop3r.telegramchart.views.charts.line.LineChartYScaled;
import com.st0rmtroop3r.telegramchart.views.charts.line.LineChartYScaledAnimator;
import com.st0rmtroop3r.telegramchart.views.charts.stackedbar.StackedBarChart;
import com.st0rmtroop3r.telegramchart.views.charts.stackedbar.StackedBarChartAnimator;
import com.st0rmtroop3r.telegramchart.views.charts.stackedbar.StackedBarChartToolTip;

public class ChartFactory {

    private static final String TAG = "ChartFactory";
    private ChartDrawable chartPreview;
    private ChartDrawable chart;
    private ChartAnimator animator;
    private String name;
    private YAxis yAxis;
    private final Resources resources;
    private final Resources.Theme theme;
    private final TypedValue typedValue;

    ChartFactory(ChartFactoryBuilder builder) {
        resources = builder.getContext().getResources();
        theme = builder.getContext().getTheme();
        typedValue = new TypedValue();
        chartPreview = createPreviewChart(builder);
        chart = createReactiveChart(builder);
        chart.setToolTip(createToolTip(builder));
        animator = createChartAnimator(builder);
        name = getName(builder);
        yAxis = createYAxis(builder);
    }

    private YAxis createYAxis(ChartFactoryBuilder builder) {
        YAxis yAxis = new YAxis1();
        int textColor = Color.LTGRAY;
        switch (builder.getType()) {
            case "line":
                if (builder.isYScaled()) {
                    yAxis = new YAxis2();
                }
                break;
            case "bar":

                break;
            case "area":
                Log.w(TAG, "createYAxis: " );
                theme.resolveAttribute(R.attr.y_axis_text_messages_apps, typedValue, true);
                textColor = typedValue.data;
                break;
        }
        theme.resolveAttribute(R.attr.axis_lines_color, typedValue, true);
        int lineColor = typedValue.data;
        yAxis.setTextColor(textColor);
        yAxis.setLinesColor(lineColor);
        return yAxis;
    }

    private ToolTip createToolTip(ChartFactoryBuilder builder) {
        ToolTip toolTip;
        switch (builder.getType()) {
            case "line":
                toolTip = new LineChartToolTip();
                break;
            case "bar":
                toolTip = new StackedBarChartToolTip();
                break;
            case "area":
                toolTip = new AreaChartToolTip();
                break;
            default:
                toolTip = null;
        }
        if (toolTip == null) return null;

        theme.resolveAttribute(R.attr.label_background_color, typedValue, true);
        toolTip.setBackgroundColor(typedValue.data);
        theme.resolveAttribute(R.attr.label_title_color, typedValue, true);
        toolTip.setTextColor(typedValue.data);
        theme.resolveAttribute(R.attr.label_frame_color, typedValue, true);
        toolTip.setFrameColor(typedValue.data);
        toolTip.setTextSize(resources.getDimension(R.dimen.label_text_size));
        toolTip.setMinWidth(resources.getDimension(R.dimen.label_min_width));
        toolTip.setArrowDrawable(resources.getDrawable(R.drawable.ic_arrow_right));
        toolTip.setRowSpacing(resources.getDimension(R.dimen.label_row_spacing));
        toolTip.setColumnSpacing(resources.getDimension(R.dimen.label_column_spacing));
        toolTip.setPadding(resources.getDimension(R.dimen.label_padding));
        toolTip.setMarginH(resources.getDimension(R.dimen.label_margin));
        return toolTip;
    }

    private String getName(ChartFactoryBuilder builder) {
        String chartName = "Ololo";
        switch (builder.getType()) {
            case "line":
                chartName = builder.isYScaled() ? "Interactions" : "Followers";
                break;
            case "bar":
                chartName = builder.isStacked() ? "Fruits" : "Views";
                break;
            case "area":
                chartName = "Fruits";
                break;
        }
        return chartName;
    }

    private ChartAnimator createChartAnimator(ChartFactoryBuilder builder) {
        ChartAnimator chartAnimator = null;
        switch (builder.getType()) {
            case "line":
                chartAnimator = builder.isYScaled() ? new LineChartYScaledAnimator() : new LineChartAnimator();
                break;
            case "bar":
                chartAnimator = new StackedBarChartAnimator();
                break;
            case "area":
                chartAnimator = new AreaChartAnimator();
                break;
        }
        return chartAnimator;
    }

    private ChartDrawable createPreviewChart(ChartFactoryBuilder builder) {
        ChartDrawable chartDrawable = null;
        switch (builder.getType()) {
            case "line":
                float width = resources.getDimension(R.dimen.selector_chart_stroke_width);
                AbstractLineChart chart = builder.isYScaled() ? new LineChartYScaled() : new LineChart();
                chart.setLineStrokeWidth(width);
                chartDrawable = chart;
                break;
            case "bar":
//                chartDrawable = builder.isStacked() ? new StackedBarChart() : new BarChart();
                chartDrawable = new StackedBarChart();
                break;
            case "area":
                chartDrawable = new AreaChart();
                break;
        }
        return chartDrawable;
    }

    private ChartDrawable createReactiveChart(ChartFactoryBuilder builder) {
        ChartDrawable chartDrawable = null;
        switch (builder.getType()) {
            case "line":
                float width = resources.getDimension(R.dimen.reactive_chart_stroke_width);
                AbstractLineChart chart = builder.isYScaled() ? new LineChartYScaled() : new LineChart();
                chart.setLineStrokeWidth(width);
                chartDrawable = chart;
                break;
            case "bar":
//                chartDrawable = builder.isStacked() ? new StackedBarChartZoom() : new BarChartZoom();
//                chartDrawable = builder.isStacked() ? new StackedBarChart() : new BarChartZoom();
                chartDrawable = new StackedBarChart();
                break;
            case "area":
                chartDrawable = new AreaChart();
                break;
        }
        return chartDrawable;
    }

    public ChartDrawable getChartPreview() {
        return chartPreview;
    }

    public ChartAnimator getAnimator() {
        return animator;
    }

    public ChartDrawable getChart() {
        return chart;
    }

    public String getName() {
        return name;
    }

    public YAxis getyAxis() {
        return yAxis;
    }
}
