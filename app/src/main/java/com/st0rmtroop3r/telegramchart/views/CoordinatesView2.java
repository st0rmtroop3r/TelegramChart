package com.st0rmtroop3r.telegramchart.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.st0rmtroop3r.telegramchart.R;
import com.st0rmtroop3r.telegramchart.views.charts.YAxis;

import androidx.annotation.Nullable;

public class CoordinatesView2 extends View {

    private YAxis yAxis;

    public CoordinatesView2(Context context) {
        super(context);
    }

    public CoordinatesView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CoordinatesView2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        setAxisBounds();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (yAxis != null) {
            yAxis.draw(canvas);
        }
    }

    public void setYAxis(YAxis axis) {
        if (axis == null) return;
        yAxis = axis;

        Resources resources = getContext().getResources();
        Resources.Theme theme = getContext().getTheme();
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(R.attr.axis_marks_text_color, typedValue, true);
        yAxis.setTextColor(typedValue.data);
        theme.resolveAttribute(R.attr.axis_lines_color, typedValue, true);
        yAxis.setLinesColor(typedValue.data);
        yAxis.setTextSize(resources.getDimension(R.dimen.coordinates_text_size));
        yAxis.setGridLineStrokeWidth(resources.getDimension(R.dimen.x_axis_line_width));
        setAxisBounds();
    }

    public void setTextColor(int color) {
        if (yAxis != null) {
            yAxis.setTextColor(color);
            invalidate();
        }
    }
    public void setLinesColor(int color) {
        if (yAxis != null) {
            yAxis.setLinesColor(color);
            invalidate();
        }
    }

    private void setAxisBounds() {
        if (yAxis != null && getWidth() > 0) {
            yAxis.setSize(getWidth(), getHeight(), getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
            invalidate();
        }
    }
}
