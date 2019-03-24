package com.st0rmtroop3r.telegramchart;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;

import com.st0rmtroop3r.telegramchart.enitity.Chart;
import com.st0rmtroop3r.telegramchart.enitity.ChartLine;

public class ChartWindowSelector extends ChartView {

    private static final String TAG = ChartWindowSelector.class.getSimpleName();

    private final int touchableHalfWidth = 50;
    private WindowFrame window;
    private SelectionListener listener;
    private int defaultWindowWidth = 400;
    private int minWindowWidth = 300;
    private int frameSideWidth = 20;

    private void init(Context context) {
        Resources resources = context.getResources();
        chartStrokeWidth = resources.getDimension(R.dimen.selector_chart_stroke_width);
        defaultWindowWidth = resources.getDimensionPixelSize(R.dimen.selector_window_default_width);
        minWindowWidth = resources.getDimensionPixelSize(R.dimen.selector_window_min_width);
        frameSideWidth = resources.getDimensionPixelSize(R.dimen.selector_window_frame_width);
        frameSideWidth = frameSideWidth / 2 * 2;
        window = new WindowFrame(context);
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
    void setChartsData(Chart chart) {
        xAxisLength = chart.xData.length - 1;
        yAxisMaxValue = 0;
        chartLines.clear();
        for (ChartLine chartLine : chart.chartLines) {
            ChameleonChartLine chartLineView = new ChameleonChartLine(chartLine.yData,
                    Color.parseColor(chartLine.color), chartLine.name, chartLine.id);
            chartLines.add(chartLineView);
            if (yAxisMaxValue < chartLineView.yAxisMax) {
                yAxisMaxValue = chartLineView.yAxisMax;
            }
        }
        if (viewWidth > 0 && viewHeight > 0) {
            updateView();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(window.frameRect, window.framePaint);
        canvas.drawRect(window.leftDimRect, window.sideDimPaint);
        canvas.drawRect(window.rightDimRect, window.sideDimPaint);
        canvas.clipRect(window.windowLeft(), 0, window.windowRight(), viewHeight);
        for (ChartLineView chart : chartLines) {
            if (chart.draw) {
                canvas.drawPath(chart.path, ((ChameleonChartLine)chart).paintSolid);
            }
        }
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

    @Override
    protected void updateView() {
        for (ChartLineView line : chartLines) {
            ((ChameleonChartLine)line).paintSolid.setAlpha(line.paint.getAlpha());
        }
        super.updateView();
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
        Paint framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        Rect leftDimRect = new Rect();
        Rect rightDimRect = new Rect();
        Paint sideDimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        private int frameSideHalfWidth = frameSideWidth / 2;
        private WindowSection section = WindowSection.NONE;
        private int touchOffset;

        WindowFrame(Context context) {
            Resources.Theme theme = context.getTheme();
            TypedValue typedValue = new TypedValue();
            theme.resolveAttribute(R.attr.window_selector_frame_color, typedValue, true);
            framePaint.setColor(typedValue.data);
            framePaint.setStyle(Paint.Style.STROKE);
            framePaint.setStrokeWidth(frameSideWidth);

            theme.resolveAttribute(R.attr.window_selector_dim_color, typedValue, true);
            sideDimPaint.setColor(typedValue.data);
            sideDimPaint.setStyle(Paint.Style.FILL);

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

        int offset(int x) {
            return x - windowLeft() - windowWidth() / 2;
        }

        void moveWindow(int x) {

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
            int left = windowRight() - minWindowWidth;
            if (x < 0) {
                left = 0;
            } else if (windowRight() - x >= minWindowWidth) {
                left = x;
            }
            windowLeft(left);
            leftDimRect.right = windowLeft();
            invalidate();
            notifyListener(windowLeft(), windowRight());
        }

        void moveRightBorder(int x) {
            int right = windowLeft() + minWindowWidth;
            if (x > viewWidth) {
                right = viewWidth;
            } else if (x - windowLeft() >= minWindowWidth) {
                right = x;
            }
            windowRight(right);
            rightDimRect.left = windowRight();
            invalidate();
            notifyListener(windowLeft(), windowRight());
        }

        void onViewSizeChanged(int width, int height) {

            frameRect.bottom = height + 5;
            windowRight(width);
            windowLeft(windowRight() - defaultWindowWidth);

            leftDimRect.right = windowLeft();
            leftDimRect.bottom = height;

            rightDimRect.bottom = height;
            rightDimRect.right = width;
            rightDimRect.left = windowRight();
            notifyListener(windowLeft(), windowRight());
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

    class ChameleonChartLine extends ChartLineView {

        Paint paintSolid;

        ChameleonChartLine(int[] data, int color, String name, String id) {
            super(data, color, name, id);
            paintSolid = new Paint(paint);
            paint.setAlpha((int) (255 * 0.6));
            paintSolid.setAntiAlias(true);
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
