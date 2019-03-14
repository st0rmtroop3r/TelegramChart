package com.st0rmtroop3r.telegramchart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class FlexileWindowSelector extends View {

    private static final String TAG = FlexileWindowSelector.class.getSimpleName();

    private WindowFrame window;
    private SelectionListener listener;

    private void init(Context context) {
        window = new WindowFrame(context);
    }

    public FlexileWindowSelector(Context context) {
        super(context);
        init(context);
    }

    public FlexileWindowSelector(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FlexileWindowSelector(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FlexileWindowSelector(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(window.frameRect, window.framePaint);
        canvas.drawRect(window.leftDimRect, window.sideDimPaint);
        canvas.drawRect(window.rightDimRect, window.sideDimPaint);
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

    private void notifyListener(float left, float right) {
//        Log.w(TAG, "notifyListener: left " + left + ", right " + right);
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

        Rect frameRect = new Rect();
        Paint framePaint = new Paint();

        Rect leftDimRect = new Rect();
        Rect rightDimRect = new Rect();
        Paint sideDimPaint = new Paint();

        private int defaultWindowWidth = 400;
        private int minWindowWidth = 300;
        private int frameSideWidth = 30;
        private int frameSideHalfWidth = frameSideWidth / 2;
        private WindowSection section = WindowSection.NONE;
        private int touchOffset;

        WindowFrame(Context context) {
            framePaint.setStyle(Paint.Style.STROKE);
            framePaint.setStrokeWidth(frameSideWidth);
            framePaint.setColor(context.getResources().getColor(R.color.flexile_window_border));

            sideDimPaint.setStyle(Paint.Style.FILL);
            sideDimPaint.setColor(context.getResources().getColor(R.color.flexile_window_dim));

            frameRect.top = -5;
            leftDimRect.top = 0;
            leftDimRect.left = 0;
            rightDimRect.top = 0;
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
//            Log.w(TAG, "captureSection: " + section.name());
        }

        void releaseSection() {
            section = WindowSection.NONE;
        }

        boolean contains(int x) {
            return frameRect.contains(x, frameRect.height() / 2);
        }

        boolean leftBorderContains(int x) {
            return x > (frameRect.left - 50) && x < (frameRect.left + 50);
        }

        boolean rightBorderContains(int x) {
            return x > (frameRect.right - 50) && x < (frameRect.right + 50);
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

        int offset(int x) {
            return x - windowLeft() - windowWidth() / 2;
        }

        void moveWindow(int x) {
//            Log.w(TAG, "moveWindow: to " + x);
            int halfWidth = windowWidth() / 2;
            int left, right;

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
                leftDimRect.right = left;
                changed = true;
            }
            if (right != windowRight()) {
                windowRight(right);
                rightDimRect.left = right;
                changed = true;
            }
            if (changed) {
                invalidate();
                notifyListener(windowLeft(), windowRight());
            }
        }

        void moveLeftBorder(int x) {
//            Log.w(TAG, "moveLeftBorder: " + x);
            if (windowRight() - x >= minWindowWidth) {
                windowLeft(x);

                leftDimRect.right = windowLeft();

                invalidate();
                notifyListener(windowLeft(), windowRight());
            }
        }

        void moveRightBorder(int x) {
//            Log.w(TAG, "moveRightBorder: " + x);
            if (x - windowLeft() >= minWindowWidth) {
                windowRight(x);

                rightDimRect.left = windowRight();

                invalidate();
                notifyListener(windowLeft(), windowRight());
            }
        }

        void onViewSizeChanged(int width, int height) {

            frameRect.bottom = height + 5;
            frameRect.left = width - defaultWindowWidth;
            frameRect.right = width - frameSideHalfWidth;

            leftDimRect.right = frameRect.left;
            leftDimRect.bottom = height;

            rightDimRect.bottom = height;
            rightDimRect.right = width;
            rightDimRect.left = frameRect.right;
        }

        int windowWidth() {
            return frameRect.width() + frameSideWidth;
        }

        int windowLeft() {
            return frameRect.left - frameSideHalfWidth;
        }

        void windowLeft(int x) {
            frameRect.left = x + frameSideHalfWidth;
        }

        int windowRight() {
            return frameRect.right + frameSideHalfWidth;
        }

        void windowRight(int x) {
            frameRect.right = x - frameSideHalfWidth;
        }
    }

    enum WindowSection {
        WINDOW,
        LEFT_BORDER,
        RIGHT_BORDER,
        NONE
    }

    interface SelectionListener {
        void onChange(float left, float right);
    }

}

