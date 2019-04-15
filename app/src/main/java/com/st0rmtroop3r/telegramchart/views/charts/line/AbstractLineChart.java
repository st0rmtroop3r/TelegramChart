package com.st0rmtroop3r.telegramchart.views.charts.line;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.TypedValue;

import com.st0rmtroop3r.telegramchart.R;
import com.st0rmtroop3r.telegramchart.views.ChartDrawable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractLineChart extends ChartDrawable {

    protected XAxisGridLine xAxisGridLine = new XAxisGridLine();
    protected final Paint innerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final List<Circle> circles = new ArrayList<>();
    protected float circleOuterRadius = 25f;
    protected float circleInnerRadius = 15f;
    protected float lineStrokeWidth = 5;

    public void setLineStrokeWidth(float width) {
        lineStrokeWidth = width;
    }

    @Override
    public void applyTheme(Resources.Theme theme) {
        super.applyTheme(theme);
        Resources resources = theme.getResources();

        circleInnerRadius = resources.getDimension(R.dimen.line_chart_circle_inner_radius);
        circleOuterRadius = resources.getDimension(R.dimen.line_chart_circle_outer_radius);

        TypedValue typedValue = new TypedValue();

        theme.resolveAttribute(R.attr.chart_background, typedValue, true);
        innerCirclePaint.setColor(typedValue.data);

        theme.resolveAttribute(R.attr.highlight_line_color, typedValue, true);
        xAxisGridLine.paint.setColor(typedValue.data);

    }

    class XAxisGridLine {

        final Paint paint = new Paint();
        float x;
        float y0 = 0f;
        float y1;
        boolean isShowing = false;

        XAxisGridLine() {
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.LTGRAY);
            paint.setStrokeWidth(6);
        }

        void draw(Canvas canvas) {
            if (isShowing) canvas.drawLine(x, y0, x, y1, paint);
        }
    }

    class Circle {
        String id;
        boolean isLineVisible;
        boolean isShowing = false;
        float x;
        float y;
        Paint paint = new Paint();

        Circle(int color, String id, boolean visible) {
            paint.setColor(color);
            paint.setStyle(Paint.Style.FILL);
            this.id = id;
            this.isLineVisible = visible;
        }

        void setCoordinates(float cx, float cy) {
            x = cx;
            y = cy;
        }

        void draw(Canvas canvas) {
            if (isShowing) {
                canvas.drawCircle(x, y, circleOuterRadius, paint);
                canvas.drawCircle(x, y, circleInnerRadius, innerCirclePaint);
            }
        }
    }

}
