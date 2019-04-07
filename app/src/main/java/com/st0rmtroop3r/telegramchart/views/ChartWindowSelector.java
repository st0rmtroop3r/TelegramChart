package com.st0rmtroop3r.telegramchart.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;

import com.st0rmtroop3r.telegramchart.R;

public class ChartWindowSelector extends ChartView {

    private static final String TAG = ChartWindowSelector.class.getSimpleName();

    private final int touchableHalfWidth = 50;
    private WindowFrame window;
    private SelectionListener listener;
    private int defaultWindowWidth = 400;
    private int minWindowWidth = 300;

    private void init(Context context) {
        Resources resources = context.getResources();
        chartStrokeWidth = resources.getDimension(R.dimen.selector_chart_stroke_width);
        defaultWindowWidth = resources.getDimensionPixelSize(R.dimen.selector_window_default_width);
        minWindowWidth = resources.getDimensionPixelSize(R.dimen.selector_window_min_width);
        window = new WindowFrame(context);
        window.frameSideWidth = resources.getDimension(R.dimen.selector_window_frame_width);
        float frameTopWidth = resources.getDimension(R.dimen.selector_window_frame_top_bottom_width);
        window.setFrameTopWidth(frameTopWidth);
        float dashWidth = resources.getDimension(R.dimen.selector_window_dash_width);
        window.dashPaint.setStrokeWidth(dashWidth);
        window.cornerRadius = resources.getDimension(R.dimen.selector_window_corner_radius);
    }

    public ChartWindowSelector(Context context) {
        super(context);
        init(context);
    }

    public ChartWindowSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChartWindowSelector(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.clipRect(window.innerRect, Region.Op.DIFFERENCE);
        float r = window.cornerRadius;
        canvas.drawRoundRect(window.dimRect, r, r, window.sideDimPaint);
        canvas.drawRoundRect(window.frameRect, r, r, window.framePaint);
        canvas.drawLines(window.dashLines, window.dashPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        window.onViewSizeChanged(w, h);
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

    public void setSelectionListener(SelectionListener selectionListener) {
        listener = selectionListener;
    }

    public void setSideDimColor(int color) {
        window.sideDimPaint.setColor(color);
        invalidate();
    }

    public void setWindowFrameColor(int color) {
        window.framePaint.setColor(color);
        invalidate();
    }

    private void notifyListener(float left, float right) {
        if (listener != null) {
            float leftPercent = left / getWidth();
            float rightPercent = right / getWidth();
            listener.onChange(leftPercent, rightPercent);
        }
    }

    private void onActionDown(float xCoordinate) {
        window.captureSection((int) xCoordinate);
    }

    private void onActionMove(float xCoordinate) {
        window.move((int) xCoordinate);
    }

    private void onActionUp() {
        window.releaseSection();
    }

    private class WindowFrame {

        RectF frameRect = new RectF();
        Paint framePaint = new Paint();
        RectF innerRect = new RectF();
        RectF dimRect = new RectF();
        Paint sideDimPaint = new Paint();
        Paint dashPaint = new Paint();
        float[] dashLines = new float[8];
        private float frameSideWidth = 20;
        private float frameTopWidth = 10;
        float cornerRadius = 20;

        private WindowSection section = WindowSection.NONE;
        private float touchOffset;

        WindowFrame(Context context) {
            Resources.Theme theme = context.getTheme();
            TypedValue typedValue = new TypedValue();
            theme.resolveAttribute(R.attr.window_selector_frame_color, typedValue, true);
            framePaint.setColor(typedValue.data);
            framePaint.setStyle(Paint.Style.FILL_AND_STROKE);

            theme.resolveAttribute(R.attr.window_selector_dim_color, typedValue, true);
            sideDimPaint.setColor(typedValue.data);
            sideDimPaint.setStyle(Paint.Style.FILL);

            dashPaint.setStyle(Paint.Style.STROKE);
            dashPaint.setStrokeCap(Paint.Cap.ROUND);
            dashPaint.setColor(Color.WHITE);

            frameRect.top = 0;
            dimRect.left = 0;
            dimRect.top = 0;
        }

        void setFrameTopWidth(float width) {
            frameTopWidth = width;
            innerRect.top = width;
        }

        void onViewSizeChanged(int width, int height) {

            frameRect.bottom = height;
            innerRect.bottom = height - frameTopWidth;
            windowRight(width);
            windowLeft(windowRight() - defaultWindowWidth);

            dimRect.bottom = height - paddingBottom;
            dimRect.top = paddingTop;
            dimRect.right = width;

            dashLines[1] = frameRect.height() / 2.8f;
            dashLines[5] = dashLines[1];
            dashLines[3] = frameRect.height() - frameRect.height() / 2.8f;
            dashLines[7] = dashLines[3];

            notifyListener(windowLeft(), windowRight());
        }

        void captureSection(int x) {
            if (leftBorderContains(x)) {
                section = WindowSection.LEFT_BORDER;
            } else if (rightBorderContains(x)){
                section = WindowSection.RIGHT_BORDER;
            } else if (contains(x)) {
                section = WindowSection.WINDOW;
                touchOffset = offset(x);
            } else {
                section = WindowSection.NONE;
            }
        }

        void releaseSection() {
            section = WindowSection.NONE;
        }

        boolean contains(int x) {
            return frameRect.contains(x, frameRect.height() / 2);
        }

        boolean leftBorderContains(int x) {
            return x > (frameRect.left - touchableHalfWidth) && x < (frameRect.left + touchableHalfWidth);
        }

        boolean rightBorderContains(int x) {
            return x > (frameRect.right - touchableHalfWidth) && x < (frameRect.right + touchableHalfWidth);
        }

        void move(int x) {
            switch (section) {
                case WINDOW:
                    moveWindow(x - touchOffset);
                    break;
                case LEFT_BORDER:
                    moveLeftBorder(x);
                    break;
                case RIGHT_BORDER:
                    moveRightBorder(x);
                    break;
            }
        }

        float offset(int x) {
            return x - windowLeft() - windowWidth() / 2;
        }

        void moveWindow(float x) {

            float halfWidth = windowWidth() / 2;
            float left, right;

            if (x - halfWidth < 0) {
                left = 0;
                right = windowWidth();
            } else if (x + halfWidth > getWidth()) {
                left = getWidth() - windowWidth();
                right = getWidth();
            } else {
                left = x - halfWidth;
                right = x + halfWidth;
            }

            boolean changed = false;

            if (left != windowLeft()) {
                windowLeft(left);
                changed = true;
            }
            if (right != windowRight()) {
                windowRight(right);
                changed = true;
            }
            if (changed) {
                invalidate();
                notifyListener(windowLeft(), windowRight());
            }
        }

        void moveLeftBorder(int x) {
            float left = windowRight() - minWindowWidth;
            if (x < 0) {
                left = 0;
            } else if (windowRight() - x >= minWindowWidth) {
                left = x;
            }
            windowLeft(left);
            invalidate();
            notifyListener(windowLeft(), windowRight());
        }

        void moveRightBorder(int x) {
            float right = windowLeft() + minWindowWidth;
            if (x > viewWidth) {
                right = viewWidth;
            } else if (x - windowLeft() >= minWindowWidth) {
                right = x;
            }
            windowRight(right);
            invalidate();
            notifyListener(windowLeft(), windowRight());
        }

        float windowWidth() {
            return frameRect.width();
        }

        float windowLeft() {
            return frameRect.left;
        }

        void windowLeft(float x) {
            frameRect.left = x;
            innerRect.left = x + frameSideWidth;
            dashLines[0] = x + frameSideWidth / 2;
            dashLines[2] = dashLines[0];
        }

        float windowRight() {
            return frameRect.right;
        }

        void windowRight(float x) {
            frameRect.right = x;
            innerRect.right = x - frameSideWidth;
            dashLines[4] = x - frameSideWidth / 2;
            dashLines[6] = dashLines[4];
        }
    }

    enum WindowSection {
        WINDOW,
        LEFT_BORDER,
        RIGHT_BORDER,
        NONE
    }

    public interface SelectionListener {
        void onChange(float left, float right);
    }


}
