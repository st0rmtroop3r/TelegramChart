package com.st0rmtroop3r.telegramchart.views.charts.stackedbar;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.st0rmtroop3r.telegramchart.views.charts.ToolTip;

public class StackedBarChartToolTip extends ToolTip {

    public static final String TAG = StackedBarChartToolTip.class.getSimpleName();

    private Column names = new Column();
    private Column values = new Column();

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
            canvas.drawText(text.string, titleX, rowY, text.paint);
        }
        // draw names
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
    public void clear() {
        names.clear();
        values.clear();
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
    public float getWidth() {
        float width = names.width + values.width + columnSpacing + padding * 2;
        return width > minWidth ? width : minWidth;
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        for (Text text : names.texts) {
            text.paint.setColor(color);
        }
        if (values.texts.size() > 1) {
            values.texts.get(values.texts.size() - 1).paint.setColor(color);
        }
    }
}
