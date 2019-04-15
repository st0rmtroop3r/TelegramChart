package com.st0rmtroop3r.telegramchart.views.charts.line;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.st0rmtroop3r.telegramchart.views.charts.ToolTip;

public class LineChartToolTip extends ToolTip {

    public static final String TAG = LineChartToolTip.class.getSimpleName();

    private Column names = new Column();
    private Column values = new Column();

    @Override
    public float getWidth() {
        float width = names.width + values.width + columnSpacing + padding * 2;
        return width > minWidth ? width : minWidth;
    }

    @Override
    public void draw(Canvas canvas) {
        if (!isShow()) return;
        toolTipRect.left = x;
        toolTipRect.top = y;
        float columnsHeight = names.height > values.height ? names.height : values.height;
        toolTipRect.bottom = y + textSize + columnsHeight + padding * 2;
        float width = names.width + values.width + columnSpacing + padding * 2;
        if (width < minWidth) width = minWidth;
        toolTipRect.right = x + width;
        toolTipFramePaint.setStrokeWidth(15);
        canvas.drawRoundRect(toolTipRect, 20, 20, toolTipFramePaint);
        toolTipFramePaint.setStrokeWidth(10);
        canvas.drawRoundRect(toolTipRect, 20, 20, toolTipFramePaint);
        toolTipFramePaint.setStrokeWidth(5);
        canvas.drawRoundRect(toolTipRect, 20, 20, toolTipFramePaint);
        canvas.drawRoundRect(toolTipRect, 20, 20, toolTipPaint);

        float titleX = toolTipRect.left + padding;
        float titleY = toolTipRect.top + padding + textSize;

        // draw title
        canvas.drawText(title, titleX, titleY, titlePaint);
        // draw names
        float rowY = titleY;
        for (Text text: names.texts) {
            rowY += textSize + rowSpacing;
            Log.w(TAG, "draw: rowY " + rowY + " = textSize " + textSize + " + rowSpacing " + rowSpacing);
            canvas.drawText(text.string, titleX, rowY, text.paint);
        }
        // draw values
        float valueX;
        rowY = titleY;
        for (Text text: values.texts) {
            valueX = toolTipRect.right - padding - text.textWidth;
            rowY += textSize + rowSpacing;
            canvas.drawText(text.string, valueX, rowY, text.paint);
        }

        if (arrow == null) return;
        float arrowSize = textSize * 0.8f;
        float arrowRight = toolTipRect.right - padding;
        float arrowLeft = arrowRight - arrowSize;
        float arrowTop = titleY - arrowSize;
        arrow.setBounds((int)arrowLeft, (int)arrowTop, (int)arrowRight, (int)titleY);
        arrow.draw(canvas);
    }

    @Override
    public void addValue(String name, String value, int color) {
        Paint namePaint = new Paint(itemNamePaint);
        names.addText(name, namePaint);
        Paint valuePaint = new Paint(itemValuePaint);
        valuePaint.setColor(color);
        values.addText(value, valuePaint);
    }

    @Override
    public void clear() {
        names.clear();
        values.clear();
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        for (Text text : names.texts) {
            text.paint.setColor(color);
        }
    }
//
//    class Column {
//
//        float width = 0;
//        float height = 0;
//        List<Text> texts = new LinkedList<>();
//
//        void addText(String s, Paint p) {
//            Text text = new Text(s, p);
//            texts.add(text);
//            if (width < text.textWidth) width = text.textWidth;
//            height += (p.getTextSize() + rowSpacing);
//        }
//
//        void clear() {
//            texts.clear();
//            width = 0;
//            height = 0;
//        }
//    }
//
//    class Text {
//        String string;
//        Paint paint;
//        float textWidth;
//
//        Text(String s, Paint p) {
//            string = s;
//            paint = p;
//            textWidth = paint.measureText(string);
//        }
//    }
}
