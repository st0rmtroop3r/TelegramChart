package com.st0rmtroop3r.telegramchart.views.charts;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

public abstract class ToolTip {

    private static final String TAG = "ToolTip";
    protected RectF toolTipRect = new RectF();
    protected Paint toolTipFramePaint = new Paint();
    protected Paint toolTipPaint = new Paint();
    protected Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected Paint itemNamePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected Paint itemValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected String title;
    protected float x;
    protected float y = 10;
    protected float minWidth = 600;
    protected float marginH = 50;
    protected float padding = 50;
    protected float textSize = 50;
    protected float rowSpacing = 20;
    protected float columnSpacing = 20;
    private boolean show = false;
    private long dateLong;
    protected Drawable arrow;

    public ToolTip() {
        toolTipPaint.setColor(Color.WHITE);
        toolTipFramePaint.setStyle(Paint.Style.STROKE);
        toolTipFramePaint.setStrokeWidth(5);
        titlePaint.setColor(Color.BLACK);
        Typeface typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
        titlePaint.setTypeface(typeface);
        itemValuePaint.setTypeface(typeface);
        itemNamePaint.setColor(Color.BLACK);
        setTextSize(textSize);
    }

    public abstract void draw(Canvas canvas);

    public abstract void clear();

    public abstract void addValue(String name, String value, int color);

    public abstract float getWidth();

    public void setBackgroundColor(int color) {
        toolTipPaint.setColor(color);
    }

    public void setTextColor(int color) {
        titlePaint.setColor(color);
        itemNamePaint.setColor(color);
    }

    public void setFrameColor(int color) {
        toolTipFramePaint.setColor(color);
    }

    public void setTextSize(float size) {
        textSize = size;
        titlePaint.setTextSize(size);
        itemNamePaint.setTextSize(size);
        itemValuePaint.setTextSize(size);
    }

    public void setMinWidth(float width) {
        minWidth = width;
    }

    public void setArrowDrawable(Drawable drawable) {
        arrow = drawable;
    }

    public void setMarginH(float margin) {
        marginH = margin;
    }

    public float getMarginH() {
        return marginH;
    }

    public void setRowSpacing(float spacing) {
        rowSpacing = spacing;
        Log.w(TAG, "setRowSpacing: " + rowSpacing);
    }

    public void setColumnSpacing(float spacing) {
        columnSpacing = spacing;
    }

    public void setPadding(float p) {
        padding = p;
    }

    public void setTitle(String newTitle) {
        title = newTitle;
    }

    public void setX(float dx) {
        x = dx;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public boolean clicked(float x, float y) {
        return toolTipRect.contains(x, y);
    }

    public long getDateLong() {
        return dateLong;
    }

    public void setDateLong(long dateLong) {
        this.dateLong = dateLong;
    }


    public class Column {

        public float width = 0;
        public float height = 0;
        public List<Text> texts = new LinkedList<>();

        public void addText(String s, Paint p) {
            Text text = new Text(s, p);
            texts.add(text);
            if (width < text.textWidth) width = text.textWidth;
            height += (p.getTextSize() + rowSpacing);
            Log.i(TAG, "addText: " + height + " = p.getTextSize() " + p.getTextSize() + " + rowSpacing " + rowSpacing);
        }

        public void clear() {
            texts.clear();
            width = 0;
            height = 0;
        }
    }

    public class Text {
        public String string;
        public Paint paint;
        public float textWidth;

        public Text(String s, Paint p) {
            string = s;
            paint = p;
            textWidth = paint.measureText(string);
        }
    }
}
