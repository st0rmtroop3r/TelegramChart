package com.st0rmtroop3r.telegramchart;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.st0rmtroop3r.telegramchart.enitity.Chart;
import com.st0rmtroop3r.telegramchart.enitity.ChartLine;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    boolean clickFlag;
    int foo = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity();
//        testActivity();

    }

    private void mainActivity() {
        setContentView(R.layout.activity_main);

        List<Chart> charts = DataProvider.readChartsData(getApplicationContext(), R.raw.chart_data);

        final ReactiveChartView reactiveChartView = findViewById(R.id.reactive);
        ChartWindowSelector chartWindowSelector = findViewById(R.id.selector);
        LinearLayout checkBoxes = findViewById(R.id.ll_checkBoxes);
        CoordinatesView coordinatesView = findViewById(R.id.coordinates);
        LinearLayout checkboxes = findViewById(R.id.grl_checkboxes);

        reactiveChartView.badge = findViewById(R.id.grid);

        Pair<int[], String> y0pair = DataProvider.getY0Data();
        Pair<int[], String> y1pair = DataProvider.getY1Data();

        List<Pair<int[], Integer>> list = new ArrayList<>();
        list.add(new Pair<>(y0pair.first, Color.parseColor(y0pair.second)));
        list.add(new Pair<>(y1pair.first, Color.parseColor(y1pair.second)));

        Chart chart = charts.get(0);



        chartWindowSelector.setChartsData(chart);
        reactiveChartView.setChartsData(chart);
        chartWindowSelector.setSelectionListener((left, right) -> {

            int fromDataIndex = (int) (chart.xData.length * left);
            fromDataIndex = fromDataIndex < 0 ? 0 : fromDataIndex;

            int toDataIndex = (int) (chart.xData.length * right);
            toDataIndex = toDataIndex > chart.xData.length ? chart.xData.length : toDataIndex;

            int chartMaxValue = 0;
            for (ChartLine chartLine : chart.chartLines) {
                for (int i = fromDataIndex; i < toDataIndex; i++) {
                    if (chartLine.yData[i] > chartMaxValue) {
                        chartMaxValue = chartLine.yData[i];
                    }
                }
            }

            int yAxisMax = chartMaxValue / 5 * 5;

            reactiveChartView.setYAxisMaxValue(yAxisMax);
            reactiveChartView.setZoomRange(left, right);
            coordinatesView.setXAxisDataRange(left, right);
            coordinatesView.setYAxisMaxValue(yAxisMax);
        });

//        coordinatesView.setOnClickListener(v -> coordinatesView.setYAxisMaxValue((int) (Math.random() * 100 + 100)));
        coordinatesView.setOnClickListener(v -> coordinatesView.setYAxisMaxValue(foo--));
        coordinatesView.setXAxisData(chart.xData);

//        coordinatesView.setXAxisDataRange(.34f, .78f);

//        coordinatesView.setOnClickListener(v -> {
//            if (!clickFlag) {
//                coordinatesView.setYAxisMaxValue(foo++);
//            } else {
//                coordinatesView.setYAxisMaxValue(foo--);
//            }
//            clickFlag = !clickFlag;
//        });


        for (ChartLine chartLine : chart.chartLines) {
            CheckBox cb = (CheckBox) LayoutInflater.from(this).inflate(R.layout.chart_checkbox, checkboxes, false);
            cb.setText(chartLine.name);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                Drawable drawable = getResources().getDrawable(R.drawable.btn_check);
                drawable.setColorFilter(Color.parseColor(chartLine.color), PorterDuff.Mode.SRC_IN);
                cb.setButtonDrawable(drawable);
            } else {
                cb.setButtonTintList(ColorStateList.valueOf(Color.parseColor(chartLine.color)));
            }

            checkboxes.addView(cb);
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                }
            });
        }

        Log.i(TAG, "mainActivity: xMax.length = " + DataProvider.xMax.length);
    }

}
