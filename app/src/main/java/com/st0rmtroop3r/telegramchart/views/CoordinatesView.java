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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.st0rmtroop3r.telegramchart.R;

import java.util.ArrayList;
import java.util.List;

public class CoordinatesView extends View {

    private final static String TAG = CoordinatesView.class.getSimpleName();

    private int linesCount = 5;
    private int paddingX = 50;
    private int paddingTop = 150;
    private int paddingBottom = 100;
    private int yMarkPaddingLine = 20;
    private int lineInterval;
    private int baseLine;

    private int viewWidth = 0;
    private int viewHeight = 0;
    private int animDuration = 300;

    private int yAxisMaxValue;

    private int textColor = Color.GRAY;
    private int lineColor = Color.LTGRAY;

    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint yAxisGridLinePaint = new Paint();
    private final int yAxisLinesCount = 5;
    private final float[] yAxisLines = new float[yAxisLinesCount * 4];
    private final AxisMark yStartMark = new AxisMark();

    private ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    private YAxisAnimator currentYAxisAnimator;
    private List<YAxisAnimator> yAxisAnimators = new ArrayList<>();


    public CoordinatesView(Context context) {
        super(context);
        init(context);
    }

    public CoordinatesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CoordinatesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        paddingTop = getPaddingTop();
        paddingBottom = getPaddingBottom();
        lineInterval = (viewHeight - paddingBottom - paddingTop) / (linesCount);
        baseLine = viewHeight - paddingBottom;
        initLines();
        initMarks();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawText(yStartMark.text, yStartMark.x, yStartMark.y, textPaint);

        for (YAxisAnimator animator : yAxisAnimators) {
            animator.draw(canvas);
        }

    }

    public void setTextColor(int color) {
        textColor = color;
        textPaint.setColor(color);
        for (YAxisAnimator animator : yAxisAnimators) {
            animator.markPaint.setColor(color);
        }
        invalidate();
    }

    public void setLinesColor(int color) {
        lineColor = color;
        yAxisGridLinePaint.setColor(color);
        currentYAxisAnimator.linePaint.setColor(color);
        for (YAxisAnimator animator : yAxisAnimators) {
            animator.linePaint.setColor(color);
        }
        invalidate();
    }

    private void init(Context context) {

        Resources resources = context.getResources();
        Resources.Theme theme = context.getTheme();
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(R.attr.axis_marks_text_color, typedValue, true);
        textColor = typedValue.data;
        theme.resolveAttribute(R.attr.axis_lines_color, typedValue, true);
        lineColor = typedValue.data;

        textPaint.setColor(textColor);
        textPaint.setTextSize(resources.getDimension(R.dimen.coordinates_text_size));

        yAxisGridLinePaint.setColor(lineColor);
        yAxisGridLinePaint.setStyle(Paint.Style.STROKE);
        yAxisGridLinePaint.setStrokeWidth(resources.getDimension(R.dimen.x_axis_line_width));

        yStartMark.text = "0";
    }

    private void initLines() {

        for (int i = 0; i < yAxisLinesCount; i++) {
            float y = baseLine - lineInterval - lineInterval * i;
            int j = i * 4;
            yAxisLines[j] = paddingX;
            yAxisLines[j + 1] = y;
            yAxisLines[j + 2] = viewWidth - paddingX;
            yAxisLines[j + 3] = y;
        }
    }

    private void initMarks() {
        yStartMark.x = paddingX;
        yStartMark.y = baseLine - yMarkPaddingLine;
    }

    public void setYAxisMaxValue(int newValue) {
        if (newValue == yAxisMaxValue) return;

        boolean scrollUp = newValue < yAxisMaxValue;
        yAxisMaxValue = newValue;

        int axisInterval = yAxisMaxValue / linesCount;

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
            invalidate();
        }
    }
}
