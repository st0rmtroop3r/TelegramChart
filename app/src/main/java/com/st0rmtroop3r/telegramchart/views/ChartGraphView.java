package com.st0rmtroop3r.telegramchart.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.st0rmtroop3r.telegramchart.enitity.ChartData;

import androidx.annotation.Nullable;

public class ChartGraphView extends View {

    private static final String TAG = ChartGraphView.class.getSimpleName();
    ChartDrawable chart;

    public ChartGraphView(Context context) {
        super(context);
    }

    public ChartGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChartGraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        chart.draw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        setChartBounds();
    }

    public void setChart(ChartDrawable chart) {
        this.chart = chart;
        setChartBounds();
    }

    private void setChartBounds() {
        if (getWidth() > 0) {
            chart.setBounds(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(),
                    getHeight() - getPaddingBottom());
            chart.prepareInvalidate();
        }
    }

    public void setData(ChartData chart) {
        if (this.chart == null) return;
        this.chart.setData(chart);
    }

    public void applyTheme(Resources.Theme theme) {
        if (chart != null) {
            chart.applyTheme(theme);
            invalidate();
        }
    }

}
