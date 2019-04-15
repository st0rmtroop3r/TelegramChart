package com.st0rmtroop3r.telegramchart.views;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.TypedValue;

import com.st0rmtroop3r.telegramchart.R;
import com.st0rmtroop3r.telegramchart.enitity.ChartData;
import com.st0rmtroop3r.telegramchart.views.charts.ToolTip;

import java.text.SimpleDateFormat;

public abstract class ChartDrawable {

    protected int viewWidth;
    protected int viewHeight;
    protected int left;
    protected int top;
    protected int right;
    protected int bottom;
    protected int xDataLength;
    protected int xDataFrom = 0;
    protected int xDataTo;
    protected float xInterval;
    protected float xOffset;
    protected float scaledWidth;
    protected ToolTip toolTip;
    protected SimpleDateFormat labelDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
    protected long[] xData;
    protected int currentHighlightIndex = -1;

    public abstract void setData(ChartData chartData);

    public abstract void setBounds(int left, int top, int right, int bottom);

    public abstract void draw(Canvas canvas);

    public abstract void prepareInvalidate();

    public abstract boolean onDown(float x);
    public abstract boolean onMove(float x);
    public abstract boolean onUp();

    public void applyTheme(Resources.Theme theme) {
        if (toolTip == null) return;

        TypedValue typedValue = new TypedValue();

        theme.resolveAttribute(R.attr.label_background_color, typedValue, true);
        toolTip.setBackgroundColor(typedValue.data);

        theme.resolveAttribute(R.attr.label_title_color, typedValue, true);
        toolTip.setTextColor(typedValue.data);

        theme.resolveAttribute(R.attr.label_frame_color, typedValue, true);
        toolTip.setFrameColor(typedValue.data);
    }

    public void selectRange(float fromPercent, float toPercent) {
        scaledWidth = viewWidth / (toPercent - fromPercent);
        xInterval = scaledWidth / xDataLength;
        xOffset = scaledWidth * fromPercent;

        xDataFrom = (int) (xDataLength * fromPercent - left / xInterval);
        if (xDataFrom < 0) xDataFrom = 0;

        xDataTo = (int) (xDataLength * toPercent + left / xInterval) + 1;
        if (xDataTo > xDataLength) xDataTo = xDataLength;
    }

    public void setToolTip(ToolTip newToolTip) {
        toolTip = newToolTip;
    }

    public ToolTip getToolTip() {
        return toolTip;
    }

    public abstract class Series {}

}
