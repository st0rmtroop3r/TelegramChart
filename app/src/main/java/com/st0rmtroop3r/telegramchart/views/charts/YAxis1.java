package com.st0rmtroop3r.telegramchart.views.charts;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.st0rmtroop3r.telegramchart.views.AxisMark;

import java.util.ArrayList;
import java.util.List;

public class YAxis1 extends YAxis {

    private final static String TAG = YAxis1.class.getSimpleName();

    private int linesCount = 5;
    private int paddingX = 50;
    private int paddingTop = 150;
    private int paddingBottom = 100;
    private int yMarkPaddingLine = 20;
    private int lineInterval;
    private int baseLine;

    private int animDuration = 200;

    private int yAxisMaxValue;
    private int yAxisMinValue;

    private int textColor = Color.RED;
    private int lineColor = Color.LTGRAY;

    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint yAxisGridLinePaint = new Paint();
    private final int yAxisLinesCount = 5;
    private final float[] yAxisLines = new float[yAxisLinesCount * 4];
    private final AxisMark yStartMark = new AxisMark();

    private ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    private YAxisAnimator currentYAxisAnimator;
    private List<YAxisAnimator> yAxisAnimators = new ArrayList<>();

//    public View viewHolder;

    public YAxis1() {
        yAxisGridLinePaint.setColor(lineColor);
        yAxisGridLinePaint.setStyle(Paint.Style.STROKE);
        yAxisGridLinePaint.setStrokeWidth(5);

        yStartMark.text = "0";
    }

    public void setSize(int w, int h, int pLeft, int pTop, int pRight, int pBottom) {
//    public void setBounds(int left, int top, int right, int bottom) {
//        viewWidth = right - left;
//        viewHeight = bottom - top;
//        paddingTop = top;
//        paddingBottom = top;
        int viewWidth = w;
        int viewHeight = h;
        paddingTop = pTop;
        paddingBottom = pBottom;
        lineInterval = (viewHeight - paddingBottom - paddingTop) / (linesCount);
        baseLine = viewHeight - paddingBottom;
        lineInterval = (viewHeight - paddingBottom - paddingTop) / (linesCount);
        baseLine = viewHeight - paddingBottom;
//        initLines();
//        initMarks();
        yStartMark.x = paddingX;
        yStartMark.y = baseLine - yMarkPaddingLine;

        for (int i = 0; i < yAxisLinesCount; i++) {
            float y = baseLine - lineInterval - lineInterval * i;
            int j = i * 4;
            yAxisLines[j] = paddingX;
            yAxisLines[j + 1] = y;
            yAxisLines[j + 2] = viewWidth - paddingX;
            yAxisLines[j + 3] = y;
        }
    }

    public void setTextColor(int color) {
        textColor = color;
        textPaint.setColor(color);
        for (YAxisAnimator animator : yAxisAnimators) {
            animator.markPaint.setColor(color);
        }
        if (currentYAxisAnimator != null) {
            currentYAxisAnimator.markPaint.setColor(color);
        }
    }

    public void setTextSize(float size) {
        textPaint.setTextSize(size);
    }

    public void setLinesColor(int color) {
        lineColor = color;
        yAxisGridLinePaint.setColor(color);
        if (currentYAxisAnimator != null) {
            currentYAxisAnimator.linePaint.setColor(color);
        }
        for (YAxisAnimator animator : yAxisAnimators) {
            animator.linePaint.setColor(color);
            Log.w(TAG, "setLinesColor: getAlpha " + animator.linePaint.getAlpha());
        }
    }

    public void setGridLineStrokeWidth(float width) {
        yAxisGridLinePaint.setStrokeWidth(width);
    }

    public void draw(Canvas canvas) {

        canvas.drawText(yStartMark.text, yStartMark.x, yStartMark.y, textPaint);

        for (YAxisAnimator animator : yAxisAnimators) {
            animator.draw(canvas);
        }
    }

    @Override
    public void setMinMaxValues(int[] values) {
        setYAxisMaxValue(values[0], values[1]);
    }

    public void setYAxisMaxValue(int newMin, int newMax) {
//        Log.w(TAG, "setYAxisMaxValue: newMin " + newMin + ", newMax " + newMax);
        if (newMin == yAxisMinValue && newMax == yAxisMaxValue) {
//            Log.d(TAG, "setYAxisMaxValue: newMax == yAxisMaxValue");
            return;
        }

        boolean scrollUp = newMax < yAxisMaxValue;
        yAxisMaxValue = newMax;

        yStartMark.text = String.valueOf(newMin);
        int axisInterval = (newMax - newMin) / linesCount;

        AxisMark[] newYMarks = new AxisMark[linesCount];
        for (int i = 0; i < newYMarks.length; i++) {
            newYMarks[i] = new AxisMark();
            newYMarks[i].text = String.valueOf(newMin + (i + 1) * axisInterval);
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


    class YAxisAnimator {

        AxisMark[] yMarks;
        Paint linePaint = new Paint(yAxisGridLinePaint);
        Paint markPaint = new Paint(textPaint);

        ScaleAnimationListener scaleAnimationListener;
        ValueAnimator markFadeAnimator;
        ValueAnimator lineFadeAnimator;
        ValueAnimator scaleAnimator;
        Interpolator accelerateInterpolator = new AccelerateInterpolator();
        Interpolator decelerateInterpolator = new DecelerateInterpolator();
        boolean up;

        YAxisAnimator(AxisMark[] marks) {
            yMarks = marks;
            linePaint.setColor(Color.TRANSPARENT);
            scaleAnimationListener = new ScaleAnimationListener(yMarks, yAxisLines);
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
            markFadeAnimator.setInterpolator(accelerateInterpolator);
            lineFadeAnimator = ValueAnimator.ofObject(argbEvaluator, Color.TRANSPARENT, lineColor);
            float scaleFrom = scrollUp ? 0.4f : 2.5f;
            scaleAnimator = ValueAnimator.ofFloat(scaleFrom, 1);
        }

        void initOutAnimators(boolean sweepOutUp, int lastMarkFadeValue, int lastLineFadeValue, float lastScaleValue) {
            markFadeAnimator = ValueAnimator.ofObject(argbEvaluator, lastMarkFadeValue, Color.TRANSPARENT);
            markFadeAnimator.setInterpolator(decelerateInterpolator);
            lineFadeAnimator = ValueAnimator.ofObject(argbEvaluator, lastLineFadeValue, Color.TRANSPARENT);
            float scaleTo = sweepOutUp ? 2.5f : 0.4f;
            scaleAnimator = ValueAnimator.ofFloat(lastScaleValue, scaleTo);
        }

        void animate() {
            markFadeAnimator.addUpdateListener(animation -> {
                markPaint.setColor((int) animation.getAnimatedValue());
//            invalidate();
                viewHolder.invalidate();
            });
            lineFadeAnimator.addUpdateListener(animation -> {
                linePaint.setColor((int) animation.getAnimatedValue());
//            invalidate();
                viewHolder.invalidate();
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
            canvas.drawLines(scaleAnimationListener.linesForScale, scaleAnimationListener.drawOffset,
                    scaleAnimationListener.drawCount, linePaint);
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
        final float[] linesForScale;
        int drawOffset = 0;
        int drawCount = yAxisLinesCount;

        ScaleAnimationListener(AxisMark[] marks, float[] linesForScale) {
            this.marks = marks;
            this.linesForScale = linesForScale;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {

            float scaleTo = (float) animation.getAnimatedValue();
            float scaledInterval = lineInterval * scaleTo;

            float yBase = baseLine - scaledInterval;
            for (int i = 0; i < yAxisLinesCount; i++) {

                float y = yBase - scaledInterval * i;
                if (y < 0) {
                    drawCount = (i + 1) << 2;
                    break;
                }
                int j = i * 4;
                linesForScale[j + 1] = y;
                linesForScale[j + 3] = y;
                drawCount = (i + 1) << 2;
            }

            for (int i = 0; i < marks.length; i++) {
                marks[i].y = (int) (baseLine - yMarkPaddingLine - scaledInterval * (i + 1));
            }
//            invalidate();
            viewHolder.invalidate();
        }
    }
}
