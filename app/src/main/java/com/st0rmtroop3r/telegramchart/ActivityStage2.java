package com.st0rmtroop3r.telegramchart;

import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.st0rmtroop3r.telegramchart.enitity.ChartData;
import com.st0rmtroop3r.telegramchart.views.ChartView;
import com.st0rmtroop3r.telegramchart.views.RecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ActivityStage2 extends AppCompatActivity {

    private static final String TAG = ActivityStage2.class.getSimpleName();
    private static final String PREF_THEME_DARK = "PREF_THEME_DARK";
    private boolean dark;
    private final RecyclerViewAdapter adapter = new RecyclerViewAdapter();
    private LinearLayoutManager layoutManager;
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        dark = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(PREF_THEME_DARK, false);
        setTheme(dark ? R.style.AppThemeDark : R.style.AppTheme);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int visibility;
            visibility = dark ? View.SYSTEM_UI_FLAG_LAYOUT_STABLE :
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            getWindow().getDecorView().setSystemUiVisibility(visibility);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_recycler);

        toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        adapter.setTheme(getTheme());
        RecyclerView recyclerView = findViewById(R.id.recycler_main);
        recyclerView.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        List<ChartData> chartDataList = new ArrayList<>();
        ChartData chart;

        chart = DataProvider.readChartData(getApplicationContext(), R.raw.overview1);
        chartDataList.add(chart);
        Log.w(TAG, "onCreate: " + chart.yDataList.get(0).type + ": " + chart.yScaled + ", "
                + chart.stacked + ", " + chart.percentage + ", xData.length " + chart.xData.length);

        chart = DataProvider.readChartData(getApplicationContext(), R.raw.overview2);
        chartDataList.add(chart);
        Log.w(TAG, "onCreate: " + chart.yDataList.get(0).type + ": " + chart.yScaled + ", "
                + chart.stacked + ", " + chart.percentage + ", xData.length " + chart.xData.length);

        chart = DataProvider.readChartData(getApplicationContext(), R.raw.overview3);
        chartDataList.add(chart);
        Log.w(TAG, "onCreate: " + chart.yDataList.get(0).type + ": " + chart.yScaled + ", "
                + chart.stacked + ", " + chart.percentage + ", xData.length " + chart.xData.length);

        chart = DataProvider.readChartData(getApplicationContext(), R.raw.overview4);
        chartDataList.add(chart);
        Log.w(TAG, "onCreate: " + chart.yDataList.get(0).type + ": " + chart.yScaled + ", "
                + chart.stacked + ", " + chart.percentage + ", xData.length " + chart.xData.length);

        chart = DataProvider.readChartData(getApplicationContext(), R.raw.overview5);
        chartDataList.add(chart);
        Log.w(TAG, "onCreate: " + chart.yDataList.get(0).type + ": " + chart.yScaled + ", "
                + chart.stacked + ", " + chart.percentage + ", xData.length " + chart.xData.length);

        adapter.setDataList(chartDataList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_v2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_dark_mode:
                switchTheme(item);
                return true;
            default: return false;
        }
    }

    private void switchTheme(MenuItem item) {
        dark = !dark;
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .edit()
                .putBoolean(PREF_THEME_DARK, dark)
                .apply();

        int style = dark ? R.style.AppThemeDark : R.style.AppTheme;
        Resources.Theme theme = getTheme();
        theme.applyStyle(style, true);

        TypedValue typedValue = new TypedValue();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            int visibility;
            visibility = dark ? View.SYSTEM_UI_FLAG_LAYOUT_STABLE :
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            getWindow().getDecorView().setSystemUiVisibility(visibility);
        }

        theme.resolveAttribute(android.R.attr.colorPrimaryDark, typedValue, true);
        getWindow().setStatusBarColor(typedValue.data);

        theme.resolveAttribute(android.R.attr.textColor, typedValue, true);
        item.getIcon().setTint(typedValue.data);
        toolbar.setTitleTextColor(typedValue.data);

        theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
        toolbar.setBackgroundColor(typedValue.data);

        adapter.switchTheme(theme);
        for (int i = 0; i < layoutManager.getChildCount(); i++) {
            View child = layoutManager.getChildAt(i);
            if (child instanceof ChartView) {
                ((ChartView)child).switchTheme(theme);
            }
        }
        theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true);
        getWindow().setBackgroundDrawable(new ColorDrawable(typedValue.data));


    }

}
