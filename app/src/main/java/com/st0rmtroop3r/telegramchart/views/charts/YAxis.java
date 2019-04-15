package com.st0rmtroop3r.telegramchart.views.charts;

import android.graphics.Canvas;
import android.view.View;

public abstract class YAxis {

    public View viewHolder;

    public abstract void setSize(int w, int h, int pLeft, int pTop, int pRight, int pBottom);

    public abstract void draw(Canvas canvas);

    public abstract void setTextColor(int color);

    public abstract void setLinesColor(int color);

    public abstract void setTextSize(float size);

    public abstract void setGridLineStrokeWidth(float width);

    public abstract void setMinMaxValues(int[] values);
}
