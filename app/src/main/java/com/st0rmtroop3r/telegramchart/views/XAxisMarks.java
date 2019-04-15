package com.st0rmtroop3r.telegramchart.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.st0rmtroop3r.telegramchart.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class XAxisMarks extends View {
    private static final String TAG = XAxisMarks.class.getSimpleName();

    private int viewWidth = 0;
    int xPadding = 50;
    private int xMarksMarginBaseLine;
    private int xAxisMarkFadeDuration = 150;

    private int textColor = Color.GRAY;
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ArgbEvaluator argbEvaluator = new ArgbEvaluator();

    private long[] xAxisData;
    private float xAxisDataRangeFrom;
    private float xAxisDataRangeTo;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd");
    XAxis xAxis;

    public XAxisMarks(Context context) {
        super(context);
        init(context);
    }

    public XAxisMarks(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public XAxisMarks(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        Resources resources = context.getResources();
        Resources.Theme theme = context.getTheme();
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(R.attr.axis_marks_text_color, typedValue, true);
        textColor = typedValue.data;

        textPaint.setColor(textColor);
        textPaint.setTextSize(resources.getDimension(R.dimen.coordinates_text_size));

        xMarksMarginBaseLine = resources.getDimensionPixelSize(R.dimen.coord_x_marks_margin_x_axis);

        xAxis = new XAxis();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        xAxis.setMarksY();
        xAxis.applyXDataRange();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        xAxis.draw(canvas);
    }

    public void setXAxisData(long[] xAxisData) {
        this.xAxisData = xAxisData;
        xAxis.setData(xAxisData);
    }

    public void setXAxisDataRange(float from, float to) {
//        Log.w(TAG, "setXAxisDataRange: from  " + from + ", to " + to );
        xAxisDataRangeFrom = from;
        xAxisDataRangeTo = to;
        xAxis.setMarksY();
        xAxis.applyXDataRange();
    }

    public void setTextColor(int color) {
        textPaint.setColor(color);
        invalidate();
    }

    class XAxis {

        AxisMark[] xMarks;
        int skip = 1;
        int prevSkip = 1;
        float markWidth = 0;
        ValueAnimator fadeInAnimator = ValueAnimator.ofObject(argbEvaluator, Color.TRANSPARENT, textColor);
        ValueAnimator fadeOutAnimator = ValueAnimator.ofObject(argbEvaluator, textColor, Color.TRANSPARENT);
        Paint fadeOutPaint = new Paint(textPaint);
        Paint fadeInPaint = new Paint(textPaint);
        boolean isFadingOut = false;
        boolean isFadingIn = false;
        private int totalChartWidth;

        XAxis() {
            fadeInAnimator.setDuration(xAxisMarkFadeDuration);
            fadeInAnimator.addUpdateListener(animation -> {
                fadeInPaint.setColor((Integer) animation.getAnimatedValue());
                invalidate();
            });
            fadeInAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) { isFadingIn = true; }
                @Override
                public void onAnimationEnd(Animator animation) { isFadingIn = false; }
            });

            fadeOutAnimator.setDuration(xAxisMarkFadeDuration);
            fadeOutAnimator.addUpdateListener(animation -> {
                fadeOutPaint.setColor((Integer) animation.getAnimatedValue());
                invalidate();
            });
            fadeOutAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) { isFadingOut = true; }
                @Override
                public void onAnimationEnd(Animator animation) { isFadingOut = false; }
            });
        }

        private void setMarksY() {
            if (xMarks == null) return;
            for (AxisMark xMark : xMarks) {
                xMark.y = (int) (xMarksMarginBaseLine + textPaint.getTextSize());
            }
        }

        private void applyXDataRange() {
            if (xAxisData == null || viewWidth == 0) return;

            totalChartWidth = (int) ((viewWidth) / (xAxisDataRangeTo - xAxisDataRangeFrom));
            float numberOfMarksChartCanFit = (totalChartWidth - markWidth) / markWidth;
            float skipInterval = xMarks.length / numberOfMarksChartCanFit;

            int newSkip = 1;
            while (newSkip < skipInterval) {
                newSkip = newSkip * 2;
            }
            if (newSkip == skip) {
                invalidate();
                return;
            }
            prevSkip = skip;
            skip = newSkip;

            if (prevSkip > 0 && prevSkip < skip) {
                // fade out
                fadeInAnimator.end();
                fadeOutAnimator.start();
            } else {
                // fade in
                fadeOutAnimator.end();
                fadeInAnimator.start();
            }
        }

        void setMarkX(AxisMark mark, int i) {
            mark.x = (int) ((totalChartWidth - xPadding - markWidth * 0.1) / xMarks.length * i - totalChartWidth * xAxisDataRangeFrom) + xPadding;
        }

        void draw(Canvas canvas) {

            if (isFadingOut) {

                for (int i = 0; i < xMarks.length; i += skip) {
                    setMarkX(xMarks[i], i);
                    canvas.drawText(xMarks[i].text, xMarks[i].x, xMarks[i].y, textPaint);
                }
                for (int i = prevSkip; i < xMarks.length; i = i + prevSkip * 2) {
                    setMarkX(xMarks[i], i);
                    canvas.drawText(xMarks[i].text, xMarks[i].x, xMarks[i].y, fadeOutPaint);
                }

            } else if (isFadingIn) {

                for (int i = 0; i < xMarks.length; i += prevSkip) {
                    setMarkX(xMarks[i], i);
                    canvas.drawText(xMarks[i].text, xMarks[i].x, xMarks[i].y, textPaint);
                }
                for (int i = skip; i < xMarks.length; i = i + skip * 2) {
                    setMarkX(xMarks[i], i);
                    canvas.drawText(xMarks[i].text, xMarks[i].x, xMarks[i].y, fadeInPaint);
                }

            } else {

                for (int i = 0; i < xMarks.length; i += skip) {
                    setMarkX(xMarks[i], i);
                    canvas.drawText(xMarks[i].text, xMarks[i].x, xMarks[i].y, textPaint);
                }
            }
        }

        void setData(long[] xAxisData) {
            markWidth = 0;
            xMarks = new AxisMark[xAxisData.length];
            for (int i = 0; i < xAxisData.length; i++) {
                xMarks[i] = new AxisMark();
                xMarks[i].text = simpleDateFormat.format(new Date(xAxisData[i]));
                float measuredText = textPaint.measureText(xMarks[i].text) * 1.1f;
                if (measuredText > markWidth) {
                    markWidth = measuredText;
                }
            }
        }
    }
}
