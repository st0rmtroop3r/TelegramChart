package com.st0rmtroop3r.telegramchart.views;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DatesRange extends Drawable {

    public final Text leftDayOut = new Text();
    public final Text leftDayIn = new Text();
    public final Text leftMonthOut = new Text();
    public final Text leftMonthIn = new Text();
    public final Text leftYearOut = new Text();
    public final Text leftYearIn = new Text();

    public final Text dash = new Text();

    public final Text rightDayOut = new Text();
    public final Text rightDayIn = new Text();
    public final Text rightMonthOut = new Text();
    public final Text rightMonthIn = new Text();
    public final Text rightYearOut = new Text();
    public final Text rightYearIn = new Text();

    public final List<Text> texts = new ArrayList<>(12);

    public DatesRange() {
        texts.add(leftDayOut);
        texts.add(leftDayIn);
        texts.add(leftMonthOut);
        texts.add(leftMonthIn);
        texts.add(leftYearOut);
        texts.add(leftYearIn);
        texts.add(dash);
        texts.add(rightDayOut);
        texts.add(rightDayIn);
        texts.add(rightMonthOut);
        texts.add(rightMonthIn);
        texts.add(rightYearOut);
        texts.add(rightYearIn);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        for (Text text : texts) {
            canvas.drawText(text.text, text.x, text.y, text.paint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        for (Text text : texts) {
            text.paint.setAlpha(alpha);
        }
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        for (Text text : texts) {
            text.paint.setColorFilter(colorFilter);
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

    public void setTextSize(float size) {
        for (Text text : texts) {
            text.paint.setTextSize(size);
        }
    }

    public void setTextColor(int color) {
        int alpha;
        for (Text text : texts) {
            alpha = text.paint.getAlpha();
            text.paint.setColor(color);
            text.paint.setAlpha(alpha);
        }
    }

    public class Text {
        public String text = "";
        public Paint paint = new Paint();
        public float x;
        public float y;

        public void copyFrom(Text other) {
            text = other.text;
            x= other.x;
            y = other.y;
        }
    }
}
