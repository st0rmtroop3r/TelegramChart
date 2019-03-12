package com.st0rmtroop3r.telegramchart;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Pair;
import android.widget.FrameLayout;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ChartWindowSelector extends FrameLayout {

    private static final String TAG = ChartWindowSelector.class.getSimpleName();

    private FlexileWindowSelector windowSelector;
    private ChartView chartView;

    public ChartWindowSelector(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ChartWindowSelector(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChartWindowSelector(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    void init(Context context) {
        windowSelector = new FlexileWindowSelector(context);
        chartView = new ChartView(context);

        addView(chartView);
        addView(windowSelector);
    }

    void setChartsData(List<Pair<int[], Integer>> chartsData) {
        chartView.setChartsData(chartsData);
    }

    public void setSelectionListener(FlexileWindowSelector.SelectionListener selectionListener) {
        windowSelector.setSelectionListener(selectionListener);
    }
}
