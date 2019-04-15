package com.st0rmtroop3r.telegramchart.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.st0rmtroop3r.telegramchart.views.charts.ToolTip;

import androidx.annotation.Nullable;

public class ToolTipView extends View {

    private static final String TAG = ToolTipView.class.getSimpleName();
    boolean showToolTip = false;
    private ChartDrawable chart;
    private ToolTip toolTip;
    private View chartView;
    private OnToolTipClickListener onToolTipClickListener;

    public ToolTipView(Context context) {
        super(context);
        init();
    }

    public ToolTipView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ToolTipView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (toolTip == null) {
            Log.e(TAG, "onDraw: toolTip == null" );
            return;
        }
        toolTip.draw(canvas);
    }

    public void setChart(ChartDrawable newChart) {
        chart = newChart;
        if (chart == null) {
            toolTip = null;
            setOnTouchListener(null);
        } else {
            toolTip = chart.getToolTip();
            setOnTouchListener(touchListener);
        }
    }

    public void setChartView(View view) {
        chartView = view;
    }

    private void invalidateViews() {
        invalidate();
        if (chartView == null) return;
        chartView.invalidate();
    }

    public void hideToolTip() {
        showToolTip = false;
        if (chart != null) {
            chart.onUp();
        }
        invalidateViews();
    }

    private void onActionDown(float x) {
        if (showToolTip) {
            chart.onDown(x);
        } else {
            chart.onUp();
        }
        invalidateViews();
    }

    private void onActionMove(float x) {
        chart.onMove(x);
        invalidateViews();
    }

    private void onActionUp() {
        chart.onUp();
        invalidateViews();
    }

    private final GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (showToolTip && onToolTipClickListener != null && toolTip.clicked(e.getX(), e.getY())) {
                onToolTipClickListener.onToolTipClicked(toolTip.getDateLong());
                return true;
            }
            showToolTip = !showToolTip;
            if (showToolTip) {
                onActionDown(e.getX());
            } else {
                onActionUp();
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (showToolTip) {
                onActionMove(e2.getX());
            }
            return showToolTip;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (showToolTip) {
                onActionMove(e2.getX());
            }
            return showToolTip;
        }
    };

    private final GestureDetector gestureDetector = new GestureDetector(gestureListener);

    private final OnTouchListener touchListener = (v, event) -> {
        if (toolTip == null) return false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_CANCEL:
                if (showToolTip && !toolTip.clicked(event.getX(), event.getY())) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    onActionMove(event.getX());
                    return true;
                }
        }
        boolean result = gestureDetector.onTouchEvent(event);
        getParent().requestDisallowInterceptTouchEvent(result);
        return result;
    };

    public void setOnToolTipClickListener(OnToolTipClickListener onToolTipClickListener) {
        this.onToolTipClickListener = onToolTipClickListener;
    }

    public interface OnToolTipClickListener {
        void onToolTipClicked(long dateLong);
    }
}
