package com.st0rmtroop3r.telegramchart.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.st0rmtroop3r.telegramchart.ChartFactory;
import com.st0rmtroop3r.telegramchart.ChartFactoryBuilder;
import com.st0rmtroop3r.telegramchart.R;
import com.st0rmtroop3r.telegramchart.enitity.ChartData;
import com.st0rmtroop3r.telegramchart.enitity.ChartYData;
import com.st0rmtroop3r.telegramchart.views.charts.ChartAnimator;
import com.st0rmtroop3r.telegramchart.views.charts.YAxis;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;

public class ChartView extends FrameLayout {

    private static final String TAG = ChartView.class.getSimpleName();

    private ChartGraphPreview preview;
    ChartData data;
    private TextView txvChartName;
    private TextView txvDateFrom;
    private TextView txvDateTo;
    private TextView txvDateDash;
    private ChartGraphView reactive;
    private CoordinatesView2 coordinatesView;
    private ToolTipView toolTipView;
    private XAxisMarks xAxisMarks;
    private ChartWindowSelector selector;
    private ButtonsLayout checkboxesLayout;
    private ChartAnimator chartAnimator;
    private final List<CheckBoxButton> checkBoxes = new ArrayList<>();
    private CheckBoxButton touchedCheckBox;
    private boolean longPressed = false;
    private ChartDrawable chartPreview;
    private ChartDrawable chart;
    private RelativeLayout relativeLayout;
    private final List<String> dates = new ArrayList<>();

    public ChartView(Context context) {
        super(context);
        init(context);
    }

    public ChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_chart, this);
        relativeLayout = findViewById(R.id.rl_chart);
        txvChartName = findViewById(R.id.txv_chart_name);
        txvDateFrom = findViewById(R.id.txv_date_from);
        txvDateTo = findViewById(R.id.txv_date_to);
        txvDateDash = findViewById(R.id.txv_dates_dash);
        reactive = findViewById(R.id.reactive);
        coordinatesView = findViewById(R.id.coordinates);
        toolTipView = findViewById(R.id.tool_tip);
        xAxisMarks = findViewById(R.id.x_axis_marks);
        preview = findViewById(R.id.chart_preview);
        selector = findViewById(R.id.selector);
        checkboxesLayout = findViewById(R.id.grv_buttons);
    }

    public void setCharData(ChartData chartData, int position) {
        Log.w(TAG, "setCharData: " + position);
        if (chartData == null) {
            Log.e(TAG, "setCharData: chartData == null" );
            return;
        }

        ChartFactory factory = new ChartFactoryBuilder(getContext())
                .type(chartData.yDataList.get(0).type)
                .isYScaled(chartData.yScaled)
                .isStacked(chartData.stacked)
                .isPercentage(chartData.percentage)
                .build();
        chartPreview = factory.getChartPreview();
        chart = factory.getChart();
        if (chartPreview == null) {
            Log.e(TAG, "setCharData: chartPreview == null" );
            return;
        }
        formatDates(chartData.xData);
        chartPreview.setData(chartData);
        chart.setData(chartData);

        txvChartName.setText(factory.getName());

        YAxis yAxis = factory.getyAxis();
        yAxis.viewHolder = coordinatesView;
        chartAnimator = factory.getAnimator();
        chartAnimator.yAxis = yAxis;
        chartAnimator.setChartPreview(chartPreview);
        chartAnimator.setChart(chart);
        chartAnimator.setGraphPreview(preview);
        chartAnimator.setGraphView(reactive);

        preview.setChart(chartPreview);
        reactive.setChart(chart);
        toolTipView.setChart(chart);
        toolTipView.setChartView(reactive);
        coordinatesView.setYAxis(yAxis);
        xAxisMarks.setXAxisData(chartData.xData);

        toolTipView.setOnToolTipClickListener(dateLong -> {
            Log.w(TAG, "zoom: " + dateLong);
        });

        selector.setSelectionListener((left, right) -> {
            chartAnimator.onSelectedRangeChanged(left, right);
            xAxisMarks.setXAxisDataRange(left, right);
            txvDateFrom.setText(getDateAt(left));
            txvDateTo.setText(getDateAt(right));
            toolTipView.hideToolTip();
        });
        selector.setInitialSelectionListener((left, right) -> {
            xAxisMarks.setXAxisDataRange(left, right);
            chartAnimator.setInitialDataRange(left, right);
            txvDateFrom.setText(getDateAt(left));
            txvDateTo.setText(getDateAt(right));
        });
        setupButtons(chartData.yDataList);
    }

    private void formatDates(long[] xData) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        dates.clear();
        for (long timeMs : xData) {
            dates.add(dateFormat.format(new Date(timeMs)));
        }
    }

    private String getDateAt(float percent) {
        if (dates.size() == 0) return "";
        int index = (int) (dates.size() * percent);
        index = (index < 1) ? 1 : (index > dates.size()) ? dates.size() : index;
        return dates.get(index - 1);
    }

    private void setupButtons(List<ChartYData> yDataList) {
//        Log.w(TAG, "setupButtons: " );
        checkboxesLayout.removeAllViews();
        checkBoxes.clear();
        if (yDataList == null || yDataList.size() < 2) return;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (ChartYData chartLine : yDataList) {
            CheckBoxButton cb = (CheckBoxButton) inflater
                    .inflate(R.layout.widget_chart_checkbox, checkboxesLayout, false);
            cb.setText(chartLine.name);
            cb.setColor(Color.parseColor(chartLine.color));
            cb.setTag(chartLine);
            cb.setChecked(chartLine.visible);
            cb.setOnTouchListener(checkBoxTouchListener);
            cb.setOnCheckedChangeListener(checkboxListener);
            checkboxesLayout.addView(cb);
            checkBoxes.add(cb);
        }

    }
    public void switchTheme(Resources.Theme theme) {

        reactive.applyTheme(theme);
        preview.applyTheme(theme);

        TypedValue value = new TypedValue();

        theme.resolveAttribute(R.attr.chart_background, value, true);
        relativeLayout.setBackgroundColor(value.data);

        theme.resolveAttribute(android.R.attr.textColor, value, true);
        txvChartName.setTextColor(value.data);
        txvDateTo.setTextColor(value.data);
        txvDateFrom.setTextColor(value.data);
        txvDateDash.setTextColor(value.data);

        theme.resolveAttribute(R.attr.window_selector_dim_color, value, true);
        selector.setSideDimColor(value.data);

        theme.resolveAttribute(R.attr.window_selector_frame_color, value, true);
        selector.setWindowFrameColor(value.data);

        theme.resolveAttribute(R.attr.axis_marks_text_color, value, true);
        xAxisMarks.setTextColor(value.data);
        coordinatesView.setTextColor(value.data);

        theme.resolveAttribute(R.attr.axis_lines_color, value, true);
        coordinatesView.setLinesColor(value.data);
        View xLine = findViewById(R.id.view_x_axis_line);
        if (xLine != null) {
            xLine.setBackgroundColor(value.data);
        }
        toolTipView.invalidate();
        invalidate();
    }

    private final CompoundButton.OnCheckedChangeListener checkboxListener = new CompoundButton.OnCheckedChangeListener () {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            ChartYData chartLine = (ChartYData) buttonView.getTag();
            chartLine.visible = isChecked;
            chartAnimator.onSeriesCheckChanged(chartLine.id, isChecked);
        }
    };

    private final GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public void onLongPress(MotionEvent e) {
            Log.w("GestureListener", "onLongPress: " + e.getAction());
            if (touchedCheckBox == null) return;
            longPressed = true;
            ChartYData pressedData = (ChartYData) touchedCheckBox.getTag();
            for (CheckBoxButton button : checkBoxes) {
                ChartYData data = (ChartYData) button.getTag();
                if (data.id.equals(pressedData.id)) {
                    button.setOnCheckedChangeListener(null);
                    button.setChecked(true);
                    button.invalidate();
                    data.visible = true;
                } else {
                    button.setOnCheckedChangeListener(null);
                    button.setChecked(false);
                    button.invalidate();
                    button.setOnCheckedChangeListener(checkboxListener);
                    data.visible = false;
                }
            }
            chartAnimator.setOnlyOneSeriesSelected(pressedData.id);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (touchedCheckBox == null) return false;
            ChartYData touchedChartLine = (ChartYData) touchedCheckBox.getTag();
            int checkedCount = 0;
            CheckBoxButton checkedButton = null;
            for (CheckBoxButton button : checkBoxes) {
                if (button.isChecked()) {
                    checkedCount++;
                    checkedButton = button;
                }
            }
            if (checkedButton == null) return false;
            ChartYData checkedId = (ChartYData) checkedButton.getTag();
            return checkedCount == 1 && touchedChartLine.id.equals(checkedId.id);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return true;
        }
    };

    private final GestureDetector gestureDetector = new GestureDetector(gestureListener);

    private final OnTouchListener checkBoxTouchListener = (v, event) -> {
        try {
            touchedCheckBox = (CheckBoxButton) v;
        } catch (ClassCastException e) {
            return false;
        }
        if (longPressed && event.getAction() == MotionEvent.ACTION_UP) {
            touchedCheckBox.setOnCheckedChangeListener(checkboxListener);
            touchedCheckBox = null;
            longPressed = false;
            return true;
        }
        return gestureDetector.onTouchEvent(event);
    };

}
