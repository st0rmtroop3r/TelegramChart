package com.st0rmtroop3r.telegramchart.views.charts;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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

public class YAxis2 extends YAxis {

    private final static String TAG = YAxis2.class.getSimpleName();

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

    private int leftTextColor = Color.GRAY;
    private int rightTextColor = Color.GRAY;
    private int lineColor = Color.LTGRAY;
    private boolean leftEnabled = true;
    private boolean rightEnabled = true;

    private final Paint leftTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rightTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint yAxisGridLinePaint = new Paint();
    private final int yAxisLinesCount = 5;
    private final float[] yAxisLines = new float[yAxisLinesCount * 4];
    private final AxisMark leftStartMark = new AxisMark();
    private final AxisMark rightStartMark = new AxisMark();

//    private ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    private YAxisAnimator currentYAxisAnimator;
    private List<YAxisAnimator> yAxisAnimators = new ArrayList<>();
    private int viewWidth;
    private int viewHeight;

//    public View viewHolder;

    public YAxis2() {
        yAxisGridLinePaint.setColor(lineColor);
        yAxisGridLinePaint.setStyle(Paint.Style.STROKE);
        yAxisGridLinePaint.setStrokeWidth(5);

        leftStartMark.text = "0";
        rightStartMark.text = "0";
    }

    public void setSize(int w, int h, int pLeft, int pTop, int pRight, int pBottom) {
//    public void setBounds(int left, int top, int right, int bottom) {
//        viewWidth = right - left;
//        viewHeight = bottom - top;
//        paddingTop = top;
//        paddingBottom = top;
        viewWidth = w;
        viewHeight = h;
        paddingTop = pTop;
        paddingBottom = pBottom;
        lineInterval = (viewHeight - paddingBottom - paddingTop) / (linesCount);
        baseLine = viewHeight - paddingBottom;
        lineInterval = (viewHeight - paddingBottom - paddingTop) / (linesCount);
        baseLine = viewHeight - paddingBottom;

        leftStartMark.x = paddingX;
        leftStartMark.y = baseLine - yMarkPaddingLine;
        rightStartMark.x = viewWidth - paddingX;
        rightStartMark.y = baseLine - yMarkPaddingLine;

        for (int i = 0; i < yAxisLinesCount; i++) {
            float y = baseLine - lineInterval - lineInterval * i;
            int j = i * 4;
            yAxisLines[j] = paddingX;
            yAxisLines[j + 1] = y;
            yAxisLines[j + 2] = viewWidth - paddingX;
            yAxisLines[j + 3] = y;
        }
    }

    public void setLeftTextColor(int color) {
        leftTextColor = color;
        leftTextPaint.setColor(color);
        for (YAxisAnimator animator : yAxisAnimators) {
            animator.leftMarkPaint.setColor(color);
        }
    }

    public void setRightTextColor(int color) {
        rightTextColor = color;
        rightTextPaint.setColor(color);
        for (YAxisAnimator animator : yAxisAnimators) {
            animator.rightMarkPaint.setColor(color);
        }
    }

    public void setLeftValuesEnabled(boolean enabled) {
        leftEnabled = enabled;
        viewHolder.invalidate();
    }

    public void setRightValuesEnabled(boolean enabled) {
        rightEnabled = enabled;
        viewHolder.invalidate();
    }

    @Override
    public void setTextColor(int color) {

    }

    public void setTextSize(float size) {
        leftTextPaint.setTextSize(size);
        rightTextPaint.setTextSize(size);
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
//        invalidate();
    }

    public void setGridLineStrokeWidth(float width) {
        yAxisGridLinePaint.setStrokeWidth(width);
    }

    public void draw(Canvas canvas) {

        canvas.drawText(leftStartMark.text, leftStartMark.x, leftStartMark.y, leftTextPaint);
        canvas.drawText(rightStartMark.text, rightStartMark.x, rightStartMark.y, rightTextPaint);

        for (YAxisAnimator animator : yAxisAnimators) {
            animator.draw(canvas);
        }
    }

    @Override
    public void setMinMaxValues(int[] values) {
        setYAxisMaxValue(values[0], values[1], values[2], values[3]);
    }

    private void setYAxisMaxValue(int newMinL, int newMaxL, int newMinR, int newMaxR) {
        if (newMinL == yAxisMinValue && newMaxL == yAxisMaxValue) {
            return;
        }

        boolean scrollUp = newMaxL < yAxisMaxValue;
        yAxisMaxValue = newMaxL;

        leftStartMark.text = String.valueOf(newMinL);
        rightStartMark.text = String.valueOf(newMinR);
        rightStartMark.x = (int) (viewWidth -paddingX - rightTextPaint.measureText(rightStartMark.text));
        int leftAxisInterval = (newMaxL - newMinL) / linesCount;
        int rightAxisInterval = (newMaxR - newMinR) / linesCount;

        AxisMark[] newLeftMarks = new AxisMark[linesCount];
        for (int i = 0; i < newLeftMarks.length; i++) {
            newLeftMarks[i] = new AxisMark();
            newLeftMarks[i].text = String.valueOf(newMinR + (i + 1) * leftAxisInterval);
            newLeftMarks[i].x = paddingX;
        }

        AxisMark[] newRightMarks = new AxisMark[linesCount];
        for (int i = 0; i < newRightMarks.length; i++) {
            newRightMarks[i] = new AxisMark();
            newRightMarks[i].text = String.valueOf(newMinL + (i + 1) * rightAxisInterval);
            newRightMarks[i].x = (int) (viewWidth -paddingX - rightTextPaint.measureText(newRightMarks[i].text));
        }
        YAxisAnimator yAxisAnimator = new YAxisAnimator(newLeftMarks, newRightMarks);
        yAxisAnimator.slideIn(scrollUp);
        if (currentYAxisAnimator != null) {
            currentYAxisAnimator.slideOut(scrollUp);
        }
        yAxisAnimators.add(yAxisAnimator);
        currentYAxisAnimator = yAxisAnimator;
    }

    class YAxisAnimator {

        AxisMark[] leftMarks;
        AxisMark[] rightMarks;
        Paint linePaint = new Paint(yAxisGridLinePaint);
        Paint leftMarkPaint = new Paint(leftTextPaint);
        Paint rightMarkPaint = new Paint(rightTextPaint);

        ScaleAnimationListener scaleAnimationListener;
        ValueAnimator markFadeAnimator;
        ValueAnimator lineFadeAnimator;
        ValueAnimator scaleAnimator;
        Interpolator accelerateInterpolator = new AccelerateInterpolator();
        Interpolator decelerateInterpolator = new DecelerateInterpolator();
        boolean up;

        YAxisAnimator(AxisMark[] lMarks, AxisMark[] rMarks) {
            leftMarks = lMarks;
            rightMarks = rMarks;
            linePaint.setColor(Color.TRANSPARENT);
            scaleAnimationListener = new ScaleAnimationListener(leftMarks, rightMarks, yAxisLines);
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
            markFadeAnimator = ValueAnimator.ofInt(0, 255);
            markFadeAnimator.setInterpolator(accelerateInterpolator);
            lineFadeAnimator = ValueAnimator.ofInt(10, 50);
            float scaleFrom = scrollUp ? 0.8f : 1.5f;
            scaleAnimator = ValueAnimator.ofFloat(scaleFrom, 1);
        }

        void initOutAnimators(boolean sweepOutUp, int lastMarkFadeValue, int lastLineFadeValue, float lastScaleValue) {
            markFadeAnimator = ValueAnimator.ofInt(lastMarkFadeValue, 10);
            markFadeAnimator.setInterpolator(decelerateInterpolator);
            lineFadeAnimator = ValueAnimator.ofInt(lastLineFadeValue, 10);
            float scaleTo = sweepOutUp ? 1.5f : 0.8f;
            scaleAnimator = ValueAnimator.ofFloat(lastScaleValue, scaleTo);
        }

        void animate() {
            markFadeAnimator.addUpdateListener(animation -> {
                leftMarkPaint.setAlpha((int) animation.getAnimatedValue());
                rightMarkPaint.setAlpha((int) animation.getAnimatedValue());
//            invalidate();
                viewHolder.invalidate();
            });
            lineFadeAnimator.addUpdateListener(animation -> {
                linePaint.setAlpha((int) animation.getAnimatedValue());
//            invalidate();
                viewHolder.invalidate();
            });
            scaleAnimator.addUpdateListener(scaleAnimationListener);

            markFadeAnimator.start();
            lineFadeAnimator.start();
            scaleAnimator.start();
        }

        void draw(Canvas canvas) {
            if (leftEnabled) {
                for (AxisMark mark : leftMarks) {
                    canvas.drawText(mark.text, mark.x, mark.y, leftMarkPaint);
                }
            }
            if (rightEnabled) {
                for (AxisMark mark : rightMarks) {
                    canvas.drawText(mark.text, mark.x, mark.y, rightMarkPaint);
                }
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

        final AxisMark[] leftMarks;
        final AxisMark[] rightMarks;
        final float[] linesForScale;
        int drawOffset = 0;
        int drawCount = yAxisLinesCount;

        ScaleAnimationListener(AxisMark[] leftMarks, AxisMark[] rightMarks, float[] linesForScale) {
            this.leftMarks = leftMarks;
            this.rightMarks = rightMarks;
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

            for (int i = 0; i < leftMarks.length; i++) {
                leftMarks[i].y = (int) (baseLine - yMarkPaddingLine - scaledInterval * (i + 1));
                rightMarks[i].y = (int) (baseLine - yMarkPaddingLine - scaledInterval * (i + 1));
            }
//            invalidate();
            viewHolder.invalidate();
        }
    }
}
