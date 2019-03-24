package com.st0rmtroop3r.telegramchart;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.st0rmtroop3r.telegramchart.enitity.Chart;
import com.st0rmtroop3r.telegramchart.enitity.ChartLine;
import com.st0rmtroop3r.telegramchart.views.ChartView;
import com.st0rmtroop3r.telegramchart.views.ChartWindowSelector;
import com.st0rmtroop3r.telegramchart.views.CoordinatesView;
import com.st0rmtroop3r.telegramchart.views.ReactiveChartView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String PREF_THEME_DARK = "PREF_THEME_DARK";
    private static final String BUNDLE_CHART_NUMBER = "BUNDLE_CHART_NUMBER";
    private static final String BUNDLE_X_FROM = "BUNDLE_X_FROM";
    private static final String BUNDLE_X_TO = "BUNDLE_X_TO";
    private static final String BUNDLE_OPTED_OUT_LINES = "BUNDLE_OPTED_OUT_LINES";

    private ReactiveChartView reactiveChartView;
    private CoordinatesView coordinatesView;
    private ChartWindowSelector chartWindowSelector;
    private LinearLayout checkboxes;
    private final CheckboxListener checkboxListener = new CheckboxListener();

    private List<Chart> charts;
    private Chart chart;
    private int selectedChartNumber = 0;
    private float xFrom = 0;
    private float xTo = 1;
    private int chartRangeMaxValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean dark = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(PREF_THEME_DARK, false);
        setTheme(dark ? R.style.AppThemeDark : R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        reactiveChartView = findViewById(R.id.reactive);
        chartWindowSelector = findViewById(R.id.selector);
        coordinatesView = findViewById(R.id.coordinates);
        checkboxes = findViewById(R.id.ll_checkboxes);
        reactiveChartView.setBadge(findViewById(R.id.grid));

        List<String> ids = null;
        if (savedInstanceState != null) {
            selectedChartNumber = savedInstanceState.getInt(BUNDLE_CHART_NUMBER);
            xFrom = savedInstanceState.getFloat(BUNDLE_X_FROM);
            xTo = savedInstanceState.getFloat(BUNDLE_X_TO);
            ids = savedInstanceState.getStringArrayList(BUNDLE_OPTED_OUT_LINES);
        }

        chartWindowSelector.setSelectionListener(this::onSelectedRangeChanged);

        charts = DataProvider.readChartsData(getApplicationContext(), R.raw.chart_data);
        setupSpinner();
        chart = charts.get(selectedChartNumber);
        if (ids != null) {
            for (String id : ids) {
                for (ChartLine line : chart.chartLines) {
                    if (id.equals(line.id)) line.visible = false;
                }
            }
        }
        setupChart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_CHART_NUMBER, selectedChartNumber);
        outState.putFloat(BUNDLE_X_FROM, xFrom);
        outState.putFloat(BUNDLE_X_TO, xTo);
        ArrayList<String> ids = new ArrayList<>();
        for (ChartLine chartLine : chart.chartLines) {
            if (!chartLine.visible) {
                ids.add(chartLine.id);
            }
        }
        outState.putStringArrayList(BUNDLE_OPTED_OUT_LINES, ids);
    }

    private void setupChart() {
        chartWindowSelector.setChartsData(chart);
        reactiveChartView.setChartsData(chart);
        coordinatesView.setXAxisData(chart.xData);
        setupCheckboxes();
    }

    private void setupCheckboxes() {
        checkboxes.removeAllViews();
        for (ChartLine chartLine : chart.chartLines) {
            CheckBox cb = (CheckBox) LayoutInflater.from(this).inflate(R.layout.widget_chart_checkbox, checkboxes, false);
            cb.setText(chartLine.name);
            cb.setButtonTintList(ColorStateList.valueOf(Color.parseColor(chartLine.color)));
            cb.setTag(chartLine);
            cb.setChecked(chartLine.visible);
            cb.setOnCheckedChangeListener(checkboxListener);
            checkboxes.addView(cb);
        }
    }

    private void setupSpinner() {
        Spinner spinner = findViewById(R.id.spn_chart_selector);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.chart_title_color, typedValue, true);
        spinner.getBackground().setColorFilter(typedValue.data, PorterDuff.Mode.MULTIPLY);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.widget_spinner_item);
        spinner.setAdapter(arrayAdapter);
        for (int i = 0; i < charts.size(); i++) {
            arrayAdapter.add(getResources().getString(R.string.chart_name, i));
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedChartNumber = position;
                chart = charts.get(position);
                setupChart();
                onSelectedRangeChanged(xFrom, xTo);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void onSelectedRangeChanged(float left, float right) {
        xFrom = left;
        xTo = right;
        updateYAxisMaxValue();
        int yAxisMax = chartRangeMaxValue / 5 * 5;
        reactiveChartView.setYAxisMaxValue(yAxisMax);
        reactiveChartView.setZoomRange(xFrom, xTo);
        coordinatesView.setXAxisDataRange(xFrom, xTo);
        coordinatesView.setYAxisMaxValue(yAxisMax);
    }

    private void updateYAxisMaxValue() {

        int fromDataIndex = (int) (chart.xData.length * xFrom);
        fromDataIndex = fromDataIndex < 0 ? 0 : fromDataIndex;

        int toDataIndex = (int) (chart.xData.length * xTo);
        toDataIndex = toDataIndex > chart.xData.length ? chart.xData.length : toDataIndex;

        chartRangeMaxValue = 0;
        for (ChartLine chartLine : chart.chartLines) {
            if (!chartLine.visible) continue;
            for (int i = fromDataIndex; i < toDataIndex; i++) {
                if (chartLine.yData[i] > chartRangeMaxValue) {
                    chartRangeMaxValue = chartLine.yData[i];
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_dark_mode:
                switchTheme();
                return true;
            default: return false;
        }
    }

    private void switchTheme() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean dark = sharedPreferences.getBoolean(PREF_THEME_DARK, false);
        sharedPreferences.edit()
                .putBoolean(PREF_THEME_DARK, !dark)
                .commit();
        recreate();
    }

    private class CheckboxListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            ChartLine chartLine = (ChartLine) buttonView.getTag();
            chartLine.visible = isChecked;
            updateYAxisMaxValue();
            int yAxisMax = chartRangeMaxValue / 5 * 5;

            reactiveChartView.setLineVisible(chartLine.id, isChecked);
            chartWindowSelector.setLineVisible(chartLine.id, isChecked);
            int chartWindowSelectorMax = 0;
            for (ChartView.ChartLineView chartLineView : chartWindowSelector.getChartLines()) {
                if (!chartLineView.isVisible()) continue;
                if (chartLineView.getYAxisMax() > chartWindowSelectorMax) {
                    chartWindowSelectorMax = chartLineView.getYAxisMax();
                }
            }
            chartWindowSelector.setYAxisMaxValue(chartWindowSelectorMax);
            reactiveChartView.setYAxisMaxValue(yAxisMax);
            coordinatesView.setYAxisMaxValue(yAxisMax);
        }
    }
}
