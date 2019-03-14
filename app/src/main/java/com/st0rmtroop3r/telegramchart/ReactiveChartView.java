package com.st0rmtroop3r.telegramchart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;

import java.util.LinkedList;

import androidx.annotation.Nullable;

public class ReactiveChartView extends ChartView {

    private static final String TAG = ReactiveChartView.class.getSimpleName();

    private final static int FPS = 60;
//    private final static int FPS = 1;
    private final int animationDuration = 100; //ms
    private final float animationFramesCount = (float) FPS * animationDuration / 1000;

    private float xFrom = 0;
    private float xTo = 1;
    private float canvasXScale = 1;
    private float targetXScale = 1;
    private float canvasXTranslate = 0;
    private float targetXTranslate;
    private float scaleInterval;
    private float translationInterval;
    private Matrix matrix = new Matrix();
    private final LinkedList<Pair<Path, Paint>> scaledPaths = new LinkedList<>();

    LinkedList<Long> times = new LinkedList<>();

    private Path linePath = new Path();
    private Paint linePaint = new Paint();

    public ReactiveChartView(Context context) {
        super(context);
        init(context);
    }

    public ReactiveChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ReactiveChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        linePaint.setColor(Color.LTGRAY);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeWidth(10f);
    }

    void setZoomRange(float fromPercent, float toPercent) {
        xFrom = fromPercent;
        xTo = toPercent;
        updateView();
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        for (Pair<Path, Paint> p : scaledPaths) {
            canvas.drawPath(p.first, p.second);
        }
        canvas.drawPath(linePath, linePaint);

        onDrawFinished();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onActionDown(event.getX());
                return true;
            case MotionEvent.ACTION_MOVE:
                onActionMove(event.getX());
                return true;
            case MotionEvent.ACTION_UP:
                onActionUp();
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private void onActionDown(float x) {

        int nearestXPoint = getNearestXPoint(x);
        float coordX = xAxisInterval * nearestXPoint;
        Log.w(TAG, "onActionDown: " + xAxisInterval + " * " + nearestXPoint + " = " + coordX);

        linePath.moveTo(coordX, 0f);
        linePath.lineTo(coordX, viewHeight);

        invalidate();
    }

    private void onActionMove(float x) {
    }

    private void onActionUp() {
        linePath.reset();
        invalidate();
    }


    private int getNearestXPoint(float x) {
        float xAxisPercent = x / viewWidth;
        Log.w(TAG, "getNearestXPoint: " + x + " / " + viewWidth + " = " + xAxisPercent);
        float xPoint = (xAxisLength) * xAxisPercent;
        Log.w(TAG, "getNearestXPoint: " + (xAxisLength) + " * " + xAxisPercent + " = " + xPoint);
        return Math.round(xPoint);
    }

    private void updateView() {
        calcScale();
        calcTranslation();
        invalidate();
    }

    void calcScale() {
        float range = xTo - xFrom;
        targetXScale = 1 / range;
        if (!floatEquals(targetXScale, canvasXScale, 0.00001f)) {
            float scaleDiff = targetXScale - canvasXScale;
            scaleInterval = scaleDiff / animationFramesCount;
            canvasXScale += scaleInterval;
        }
    }

    void calcTranslation() {
        targetXTranslate = -viewWidth * targetXScale * xFrom;
//        Log.w(TAG, "calcTranslation: " + -viewWidth + " * " + targetXScale + " * " + xFrom + " = " + targetXTranslate);
        if (!floatEquals(targetXTranslate, canvasXTranslate, 0.001f)) {
            float translationDiff = targetXTranslate - canvasXTranslate;
            translationInterval = translationDiff / animationFramesCount;
            canvasXTranslate += translationInterval;
        }
    }

    void scalePaths(float scaleX, float offsetX) {
//        Log.w(TAG, "scalePaths: scaleX " + scaleX + ", offsetX " + offsetX);
        matrix.setScale(scaleX, 1f);
        scaledPaths.clear();
        for (Chart chart : charts) {
            Path path = new Path();
            path.set(chart.path);
            path.transform(matrix);
            path.offset(offsetX, 0);
            scaledPaths.add(new Pair<>(path, chart.paint));
        }
    }

    void onDrawFinished() {
        if (needsMoreTranslationFrames() | needsMoreScaleFrames()) {
            scalePaths(canvasXScale, canvasXTranslate);
            invalidate();
        } else {
            Log.d(TAG, "onDrawFinished: " + targetXTranslate + " == " + canvasXTranslate + ", " + targetXScale + " == " + canvasXScale);

//            Log.d(TAG, "onDrawFinished: frameInterval = " + frameInterval + ", animationFramesCount = " + animationFramesCount);
            if (times.size() == 0) return;
            String s = "";
            long t = times.pollFirst();
            long total = 0;
            while (times.size() > 0) {
                long t1 = times.poll();
                long diff = t1 - t;
                total += diff;
                t = t1;
                s += " + " + diff;
            }
            Log.w(TAG, "onDrawFinished: " + s + " = " + total);
        }
    }

    boolean needsMoreTranslationFrames() {
        if (translationInterval == 0 || canvasXTranslate == targetXTranslate) {
            return false;
        }
        if (floatEquals(canvasXTranslate, targetXTranslate, translationInterval)) {
            canvasXTranslate = targetXTranslate;
        } else {
            canvasXTranslate += translationInterval;
        }
        return true;
    }

    boolean needsMoreScaleFrames() {
        if (scaleInterval == 0 || canvasXScale == targetXScale) return false;
        if (floatEquals(canvasXScale, targetXScale, scaleInterval)) {
            canvasXScale = targetXScale;
        } else {
            canvasXScale += scaleInterval;
        }
        return true;
    }

    boolean floatEquals(float f1, float f2, float gap) {
        return Math.abs(f1 - f2) < Math.abs(gap);
    }
}

