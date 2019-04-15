package com.st0rmtroop3r.telegramchart.views.charts.area;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.st0rmtroop3r.telegramchart.views.charts.ToolTip;

public class AreaChartToolTip extends ToolTip {

    private static final String TAG = "AreaChartToolTip";

    private Column percents = new Column();
    private Column names = new Column();
    private Column values = new Column();
    private Paint itemPercentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public AreaChartToolTip() {
        super();
        Typeface typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
        itemPercentPaint.setTypeface(typeface);

    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        itemPercentPaint.setColor(color);
        for (Text text : percents.texts) {
            text.paint.setColor(color);
        }
        for (Text text : names.texts) {
            text.paint.setColor(color);
        }
    }

    @Override
    public void setTextSize(float size) {
        super.setTextSize(size);
        if (itemPercentPaint == null) return;
        itemPercentPaint.setTextSize(size);
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

        // draw percents
        float rowY = titleY;
        for (Text text: percents.texts) {
            rowY += textSize + rowSpacing;
            canvas.drawText(text.string, titleX + percents.width - text.textWidth, rowY, text.paint);
        }

        // draw names
        float namesX = titleX + percents.width + columnSpacing;
        rowY = titleY;
        for (Text text: names.texts) {
            rowY += textSize + rowSpacing;
            canvas.drawText(text.string, namesX, rowY, text.paint);
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
    public void clear() {
        percents.clear();
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

    void addPercent(float percent) {
        int round = (int) (100 * percent);
        percents.addText(round + "%", new Paint(itemPercentPaint));
    }

    @Override
    public float getWidth() {
        float width = percents.width + names.width+ values.width + columnSpacing * 2 + padding * 2;
        return width > minWidth ? width : minWidth;
    }

}
