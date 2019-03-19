package com.st0rmtroop3r.telegramchart;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;

public class CoordinatesView extends View {

    private final static String TAG = CoordinatesView.class.getSimpleName();

    private int linesCount = 5;
    private int paddingX = 50;
    private int paddingY = 150;
    private int yMarkPaddingLine = 20;
    private int lineInterval;
    private int baseLine;

    private int viewWidth = 0;
    private int viewHeight = 0;
    private int animDuration = 400;
    private int xAxisMarkFadeDuration = 250;

    private int yAxisMaxValue;

    private int textColor = Color.GRAY;
    private int lineColor = Color.LTGRAY;

    private final Matrix matrix = new Matrix();
    private final Paint textPaint = new Paint();
    private final Path xAxisLinePath = new Path();
    private final Paint xAxisLinePaint = new Paint();
    private final Path yMarkLinesPath = new Path();
    private final AxisMark yStartMark = new AxisMark();

    private ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    private YAxisAnimator currentYAxisAnimator;
    private List<YAxisAnimator> yAxisAnimators = new ArrayList<>();
    private XAxis xAxis;

    private long[] xAxisData;
    private float xAxisDataRangeFrom;
    private float xAxisDataRangeTo;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd");

    // debug
    int debugCircleY;
    Paint dPaint = new Paint(textPaint);

    public CoordinatesView(Context context) {
        super(context);
        init();
    }

    public CoordinatesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CoordinatesView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        lineInterval = (viewHeight - 2 * paddingY) / (linesCount);
        baseLine = viewHeight - paddingY;
        initLines();
        initMarks();
        xAxis.applyXDataRange();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawPath(xAxisLinePath, xAxisLinePaint);
        canvas.drawText(yStartMark.text, yStartMark.x, yStartMark.y, textPaint);

        for (YAxisAnimator animator : yAxisAnimators) {
            animator.draw(canvas);
        }

        xAxis.draw(canvas);

        // debug
        if (yAxisAnimators.isEmpty()) return;
        int chartHeight = baseLine - yAxisAnimators.get(0).yMarks[4].y - yMarkPaddingLine;
        float scale = yAxisMaxValue / Float.parseFloat(yAxisAnimators.get(0).yMarks[4].text);
        debugCircleY = (int) (baseLine - chartHeight * scale);
        canvas.drawCircle(viewWidth / 2, debugCircleY, 10, textPaint);
//        Log.w(TAG, "onDraw: " + yAxisMaxValue + ", chartHeight " + chartHeight + ", scale "
//                + scale + ", debugCircleY " + debugCircleY + ", mark.y " + yAxisAnimators.get(0).yMarks[4].y);
    }

    private void init() {

        textPaint.setColor(textColor);
        textPaint.setTextSize(60);

        xAxisLinePaint.setColor(lineColor);
        xAxisLinePaint.setStyle(Paint.Style.STROKE);
        xAxisLinePaint.setStrokeCap(Paint.Cap.ROUND);
        xAxisLinePaint.setStrokeWidth(3f);

        yStartMark.text = "0";

        xAxis = new XAxis();

        dPaint.setColor(Color.GREEN);
        dPaint.setTextSize(60);
    }

    private void initLines() {
        xAxisLinePath.moveTo(paddingX, baseLine);
        xAxisLinePath.lineTo(viewWidth - paddingX, baseLine);
        for (int i = 1; i <= linesCount; i++) {
            yMarkLinesPath.moveTo(paddingX, baseLine - lineInterval * i);
            yMarkLinesPath.lineTo(viewWidth - paddingX, baseLine - lineInterval * i);
        }
    }

    private void initMarks() {
        yStartMark.x = paddingX;
        yStartMark.y = baseLine - yMarkPaddingLine;
    }

    void setYAxisMaxValue(int newValue) {
        Log.w(TAG, "setYAxisMaxValue: " + newValue + ", yAxisAnimators.size " + yAxisAnimators.size());

        if (newValue == yAxisMaxValue) return;

        int axisMax = (int) (Math.ceil((float)newValue/5)*5);
        int axisInterval = axisMax / linesCount;

        boolean scrollUp = newValue < yAxisMaxValue;
        yAxisMaxValue = newValue;

        AxisMark[] newYMarks = new AxisMark[linesCount];
        for (int i = 0; i < newYMarks.length; i++) {
            newYMarks[i] = new AxisMark();
            newYMarks[i].text = String.valueOf((i + 1) * axisInterval);
            newYMarks[i].x = paddingX;
        }
        YAxisAnimator yAxisAnimator = new YAxisAnimator(newYMarks);
        yAxisAnimator.slideIn(scrollUp);
        if (currentYAxisAnimator != null) {
            currentYAxisAnimator.slideOut(scrollUp);
        }
        yAxisAnimators.add(yAxisAnimator);
        currentYAxisAnimator = yAxisAnimator;
    }

    void setXAxisData(long[] xAxisData) {
        this.xAxisData = xAxisData;
        xAxis.setData(xAxisData);
    }

    void setXAxisDataRange(float from, float to) {
        xAxisDataRangeFrom = from;
        xAxisDataRangeTo = to;
        xAxis.applyXDataRange();
    }

    class AxisMark {
        String text;
        int x;
        int y;
    }

    class XAxis {

        AxisMark[] xMarks;
        float skipInterval;
        int skip = 1;
        int prevSkip = 1;
        float markWidth = 0;
        ValueAnimator fadeInAnimator = ValueAnimator.ofObject(argbEvaluator, Color.TRANSPARENT, textColor);
        ValueAnimator fadeOutAnimator = ValueAnimator.ofObject(argbEvaluator, textColor, Color.TRANSPARENT);
        Paint fadeOutPaint = new Paint(textPaint);
        Paint fadeInPaint = new Paint(textPaint);
        boolean isFadingOut = false;
        boolean isFadingIn = false;

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

        private void applyXDataRange() {
            if (xAxisData == null || viewWidth == 0) return;

            int xPadding = 50;
            int totalChartWidth = (int) ((viewWidth) / (xAxisDataRangeTo - xAxisDataRangeFrom));// + xPadding * 2;

            float numberOfMarksChartCanFit = (totalChartWidth - markWidth) / markWidth;

            Log.w(TAG, "applyXDataRange: skip " + skip );
            for (int i = 0; i < xMarks.length; i++) {
                xMarks[i].y = baseLine + 80;
                xMarks[i].x = (int) ((totalChartWidth - xPadding - markWidth * 0.1) / xMarks.length * i - totalChartWidth * xAxisDataRangeFrom) + xPadding;
            }

            skipInterval = xMarks.length / numberOfMarksChartCanFit;

            int newSkip = 2;
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

        void draw(Canvas canvas) {

            if (isFadingOut) {

                for (int i = 0; i < xMarks.length; i += skip) {
                    canvas.drawText(xMarks[i].text, xMarks[i].x, xMarks[i].y, textPaint);
                }
                for (int i = prevSkip; i < xMarks.length; i = i + prevSkip * 2) {
                    canvas.drawText(xMarks[i].text, xMarks[i].x, xMarks[i].y, fadeOutPaint);
                }

            } else if (isFadingIn) {

                for (int i = 0; i < xMarks.length; i += prevSkip) {
                    canvas.drawText(xMarks[i].text, xMarks[i].x, xMarks[i].y, textPaint);
                }
                for (int i = skip; i < xMarks.length; i = i + skip * 2) {
                    canvas.drawText(xMarks[i].text, xMarks[i].x, xMarks[i].y, fadeInPaint);
                }

            } else {

                for (int i = 0; i < xMarks.length; i += skip) {
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

    class YAxisAnimator {

        AxisMark[] yMarks;
        Paint linePaint = new Paint(xAxisLinePaint);
        Paint markPaint = new Paint(textPaint);

        ScaleAnimationListener scaleAnimationListener;
        ValueAnimator markFadeAnimator;
        ValueAnimator lineFadeAnimator;
        ValueAnimator scaleAnimator;
        boolean up;

        YAxisAnimator(AxisMark[] marks) {
            yMarks = marks;
            linePaint.setColor(Color.TRANSPARENT);
            scaleAnimationListener = new ScaleAnimationListener(yMarks, yMarkLinesPath);
        }

        void slideIn(boolean scrollUp) {
            up = scrollUp;
            initInAnimators(scrollUp);
            setAnimationsDuration();
            lineFadeAnimator.setStartDelay((long) (animDuration * 0.3));
            scaleAnimator.setStartDelay((long) (animDuration * 0.1));
            animate();
        }

        void slideOut(boolean scrollUp) {
            if (up != scrollUp) {
                reverse();
                return;
            }

            cancelAnimations();
            removeAllAnimationsUpdateListeners();

            Object lastMarkFadeValue = markFadeAnimator.getAnimatedValue();
            Object lastLineFadeValue = lineFadeAnimator.getAnimatedValue();
            Object lastScaleValue = scaleAnimator.getAnimatedValue();

            if (lastMarkFadeValue == null || lastLineFadeValue == null || lastScaleValue == null) {
                yAxisAnimators.remove(this);
                return;
            }

            initOutAnimators(scrollUp, (int) lastMarkFadeValue, (int) lastLineFadeValue, (float) lastScaleValue);
            setAnimationsDuration();
            setAnimationEndListeners();
            animate();
        }

        void reverse() {
            setAnimationEndListeners();
            lineFadeAnimator.setStartDelay(0);
            scaleAnimator.setStartDelay(0);
            markFadeAnimator.reverse();
            lineFadeAnimator.reverse();
            scaleAnimator.reverse();
        }

        void initInAnimators(boolean scrollUp) {
            markFadeAnimator = ValueAnimator.ofObject(argbEvaluator, Color.TRANSPARENT, textColor);
            lineFadeAnimator = ValueAnimator.ofObject(argbEvaluator, Color.TRANSPARENT, lineColor);
            float scaleFrom = scrollUp ? 0.4f : 2.5f;
            scaleAnimator = ValueAnimator.ofFloat(scaleFrom, 1);
        }

        void initOutAnimators(boolean sweepOutUp, int lastMarkFadeValue, int lastLineFadeValue, float lastScaleValue) {
            markFadeAnimator = ValueAnimator.ofObject(argbEvaluator, lastMarkFadeValue, Color.TRANSPARENT);
            lineFadeAnimator = ValueAnimator.ofObject(argbEvaluator, lastLineFadeValue, Color.TRANSPARENT);
            float scaleTo = sweepOutUp ? 2.5f : 0.4f;
            scaleAnimator = ValueAnimator.ofFloat(lastScaleValue, scaleTo);
        }

        void animate() {
            markFadeAnimator.addUpdateListener(animation -> {
                markPaint.setColor((int) animation.getAnimatedValue());
                invalidate();
            });
            lineFadeAnimator.addUpdateListener(animation -> {
                linePaint.setColor((int) animation.getAnimatedValue());
                invalidate();
            });
            scaleAnimator.addUpdateListener(scaleAnimationListener);

            markFadeAnimator.start();
            lineFadeAnimator.start();
            scaleAnimator.start();
        }

        void draw(Canvas canvas) {
            for (AxisMark mark : yMarks) {
                canvas.drawText(mark.text, mark.x, mark.y, markPaint);
            }
            canvas.drawPath(scaleAnimationListener.scaledLines, linePaint);
        }

        void setAnimationsDuration() {
            markFadeAnimator.setDuration(animDuration);
            lineFadeAnimator.setDuration((long) (animDuration * 0.7));
            scaleAnimator.setDuration(animDuration);
        }

        void cancelAnimations() {
            markFadeAnimator.cancel();
            lineFadeAnimator.cancel();
            scaleAnimator.cancel();
        }

        void removeAllAnimationsUpdateListeners() {
            markFadeAnimator.removeAllUpdateListeners();
            lineFadeAnimator.removeAllUpdateListeners();
            scaleAnimator.removeAllUpdateListeners();
        }

        void setAnimationEndListeners() {
            AnimationEndListener listener = new AnimationEndListener(this);
            markFadeAnimator.addListener(listener);
            lineFadeAnimator.addListener(listener);
            scaleAnimator.addListener(listener);
        }
    }

    class AnimationEndListener extends AnimatorListenerAdapter {
        YAxisAnimator yAxisAnimator;
        int counter = 0;

        AnimationEndListener(YAxisAnimator yAxisAnimator) {
            this.yAxisAnimator = yAxisAnimator;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            ((ValueAnimator) animation).removeAllUpdateListeners();
            animation.removeAllListeners();
            if (++counter == 3) yAxisAnimators.remove(yAxisAnimator);
        }
    }

    class ScaleAnimationListener implements ValueAnimator.AnimatorUpdateListener {

        final AxisMark[] marks;
        final Path linesForScale;
        final Path scaledLines = new Path();

        ScaleAnimationListener(AxisMark[] marks, Path linesForScale) {
            this.marks = marks;
            this.linesForScale = linesForScale;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {

            float scaleTo = (float) animation.getAnimatedValue();
            float offsetY = baseLine - baseLine * scaleTo;
            float scaledInterval = lineInterval * scaleTo;

            matrix.setScale(1f, scaleTo);

            scaledLines.set(linesForScale);
            scaledLines.transform(matrix);
            scaledLines.offset(0, offsetY);

            for (int i = 0; i < marks.length; i++) {
                marks[i].y = (int) (baseLine - yMarkPaddingLine - scaledInterval * (i + 1));
            }
            invalidate();
        }
    }
}
