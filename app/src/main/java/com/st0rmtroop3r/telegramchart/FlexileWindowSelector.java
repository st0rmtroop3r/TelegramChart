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
        private float framePaintStrokeWidth = 30f;
        private WindowSection section = WindowSection.NONE;

        WindowFrame(Context context) {
            framePaint.setStyle(Paint.Style.STROKE);
            framePaint.setStrokeWidth(framePaintStrokeWidth);
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
                    moveWindow(x);
                    break;
                case LEFT_BORDER:
                    moveLeftBorder(x);
                    break;
                case RIGHT_BORDER:
                    moveRightBorder(x);
                    break;
            }
        }

        void moveWindow(int x) {
//            Log.w(TAG, "move: " + x);
            int halfWidth = frameRect.width() / 2;

            if (x - halfWidth > 0 && x + halfWidth < getWidth()) {
                frameRect.left = x - halfWidth;
                frameRect.right = x + halfWidth;

                leftDimRect.right = frameRect.left;
                rightDimRect.left = frameRect.right;

                invalidate();
                notifyListener(frameRect.left, frameRect.right);
            }
        }

        void moveLeftBorder(int x) {
//            Log.w(TAG, "moveLeftBorder: " + x);
            if (frameRect.right - x >= minWindowWidth) {
                frameRect.left = x;

                leftDimRect.right = frameRect.left;

                invalidate();
                notifyListener(frameRect.left, frameRect.right);
            }
        }

        void moveRightBorder(int x) {
//            Log.w(TAG, "moveRightBorder: " + x);
            if (x - frameRect.left >= minWindowWidth) {
                frameRect.right = x;

                rightDimRect.left = frameRect.right;

                invalidate();
                notifyListener(frameRect.left, frameRect.right);
            }
        }

        void onViewSizeChanged(int width, int height) {

            frameRect.bottom = height + 5;
            frameRect.left = width - defaultWindowWidth;
            frameRect.right = (int) (width - framePaintStrokeWidth / 2);

            leftDimRect.right = frameRect.left;
            leftDimRect.bottom = height;

            rightDimRect.bottom = height;
            rightDimRect.right = width;
            rightDimRect.left = frameRect.right;
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

