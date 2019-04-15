package com.st0rmtroop3r.telegramchart.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.st0rmtroop3r.telegramchart.R;
import com.st0rmtroop3r.telegramchart.enitity.ChartData;

public class ChartGraphPreview extends View {

    private static final String TAG = ChartGraphPreview.class.getSimpleName();

    ChartDrawable chart;
    Path clipPath = new Path();
    private float cornerRadius;

    public ChartGraphPreview(Context context) {
        super(context);
        init(context);
    }

    public ChartGraphPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChartGraphPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        Resources resources = context.getResources();
        cornerRadius = resources.getDimension(R.dimen.selector_window_corner_radius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.clipPath(clipPath);
        chart.draw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        clipPath.reset();
        RectF rect = new RectF(getPaddingLeft(), getPaddingTop(), w - getPaddingRight(), h - getPaddingBottom());
        clipPath.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW);
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

    @Deprecated
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
