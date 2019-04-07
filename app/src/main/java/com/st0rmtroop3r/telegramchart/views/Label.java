package com.st0rmtroop3r.telegramchart.views;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import java.util.LinkedList;
import java.util.List;

public class Label {

    public static final String TAG = Label.class.getSimpleName();

    private Paint labelPaint = new Paint();
    private RectF labelRect = new RectF();
    private Paint labelFramePaint = new Paint();
    private String title;
    private Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float x;
    private float y = 50;
    private float minWidth = 600;
    private float padding = 50;
    private float textSize = 50;
    private float rowSpacing;
    private float columnSpacing;
    private Paint itemNamePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint itemValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Column names = new Column();
    private Column values = new Column();
    private Drawable arrow;
    boolean show = false;

    Label() {
        labelPaint.setColor(Color.WHITE);
        labelFramePaint.setStyle(Paint.Style.STROKE);
        labelFramePaint.setStrokeWidth(5);
//        labelFramePaint.setColor(Color.parseColor("#D8DCDE"));
//        labelFramePaint.setAlpha((int) (255 * 0.2));
        titlePaint.setColor(Color.BLACK);
        Typeface typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
        titlePaint.setTypeface(typeface);
        itemValuePaint.setTypeface(typeface);
        itemNamePaint.setColor(Color.BLACK);
        setTextSize(textSize);
    }

    void setTextSize(float size) {
        titlePaint.setTextSize(size);
        itemNamePaint.setTextSize(size);
        itemValuePaint.setTextSize(size);
    }

    void setArrowDrawable(Drawable drawable) {
        arrow = drawable;
    }

    void setMinWidth(float width) {
        minWidth = width;
    }

    void setRowSpacing(float spacing) {
        rowSpacing = spacing;
    }

    void setColumnSpacing(float spacing) {
        columnSpacing = spacing;
    }

    void setPadding(float p) {
        padding = p;
    }

    float getWidth() {
        float width = names.width + values.width + columnSpacing + padding * 2;
        return width > minWidth ? width : minWidth;

    }

    void draw(Canvas canvas) {
        if (!show) return;
        labelRect.left = x;
        labelRect.top = y;
        float columnsHeight = names.height > values.height ? names.height : values.height;
        labelRect.bottom = y + textSize + columnsHeight + padding * 2;
        float width = names.width + values.width + columnSpacing + padding * 2;
        if (width < minWidth) width = minWidth;
        labelRect.right = x + width;
        labelFramePaint.setStrokeWidth(15);
        canvas.drawRoundRect(labelRect, 20, 20, labelFramePaint);
        labelFramePaint.setStrokeWidth(10);
        canvas.drawRoundRect(labelRect, 20, 20, labelFramePaint);
        labelFramePaint.setStrokeWidth(5);
        canvas.drawRoundRect(labelRect, 20, 20, labelFramePaint);
        canvas.drawRoundRect(labelRect, 20, 20, labelPaint);

        float titleX = labelRect.left + padding;
        float titleY = labelRect.top + padding + textSize;

        // draw title
        canvas.drawText(title, titleX, titleY, titlePaint);
        // draw names
        float rowY = titleY;
        for (Text text: names.texts) {
            rowY += textSize + rowSpacing;
            canvas.drawText(text.string, titleX, rowY, text.paint);
        }
        // draw names
        float valueX;
        rowY = titleY;
        for (Text text: values.texts) {
            valueX = labelRect.right - padding - text.textWidth;
            rowY += textSize + rowSpacing;
            canvas.drawText(text.string, valueX, rowY, text.paint);
        }

        float arrowSize = textSize * 0.8f;
        float arrowRight = labelRect.right - padding;
        float arrowLeft = arrowRight - arrowSize;
        float arrowTop = titleY - arrowSize;
        arrow.setBounds((int)arrowLeft, (int)arrowTop, (int)arrowRight, (int)titleY);
        arrow.draw(canvas);
    }

    void addValue(String name, String value, int color) {
        Paint namePaint = new Paint(itemNamePaint);
        names.addText(name, namePaint);
        Paint valuePaint = new Paint(itemValuePaint);
        valuePaint.setColor(color);
        values.addText(value, valuePaint);
    }

    void setTitle(String newTitle) {
        title = newTitle;
    }

    public void setX(float dx) {
        x = dx;
    }

    void clear() {
        names.clear();
        values.clear();
    }

    void setBackgroundColor(int color) {
        labelPaint.setColor(color);
    }

    void setTextColor(int color) {
        titlePaint.setColor(color);
        itemNamePaint.setColor(color);
    }

    void setFrameColor(int color) {
        labelFramePaint.setColor(color);
    }

    class Column {

        float width = 0;
        float height = 0;
        List<Text> texts = new LinkedList<>();

        void addText(String s, Paint p) {
            Text text = new Text(s, p);
            texts.add(text);
            if (width < text.textWidth) width = text.textWidth;
            height += (p.getTextSize() + rowSpacing);
        }

        void clear() {
            texts.clear();
            width = 0;
            height = 0;
        }
    }

    class Text {
        String string;
        Paint paint;
        float textWidth;

        Text(String s, Paint p) {
            string = s;
            paint = p;
            textWidth = paint.measureText(string);
        }
    }
}
