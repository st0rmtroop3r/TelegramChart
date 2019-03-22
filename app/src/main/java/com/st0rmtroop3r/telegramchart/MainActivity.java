package com.st0rmtroop3r.telegramchart;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.LinearLayout;

import com.st0rmtroop3r.telegramchart.enitity.Chart;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    boolean clickFlag;
    int foo = 150;

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

        reactiveChartView.badge = findViewById(R.id.grid);

        Pair<int[], String> y0pair = DataProvider.getY0Data();
        Pair<int[], String> y1pair = DataProvider.getY1Data();

        List<Pair<int[], Integer>> list = new ArrayList<>();
        list.add(new Pair<>(y0pair.first, Color.parseColor(y0pair.second)));
        list.add(new Pair<>(y1pair.first, Color.parseColor(y1pair.second)));

        chartWindowSelector.setChartsData(list);
        reactiveChartView.setChartsData(list);
        chartWindowSelector.setSelectionListener((left, right) -> {
            reactiveChartView.setZoomRange(left, right);
            coordinatesView.setXAxisDataRange(left, right);
        });

        coordinatesView.setOnClickListener(v -> coordinatesView.setYAxisMaxValue((int) (Math.random() * 100 + 100)));

//        coordinatesView.setOnClickListener(v -> {
//            if (!clickFlag) {
//                coordinatesView.setYAxisMaxValue(foo++);
//            } else {
//                coordinatesView.setYAxisMaxValue(foo--);
//            }
//            clickFlag = !clickFlag;
//        });

        coordinatesView.setXAxisData(DataProvider.x);
        coordinatesView.setXAxisDataRange(.34f, .78f);

        Log.i(TAG, "mainActivity: xMax.length = " + DataProvider.xMax.length);
    }

    private void testActivity() {
        setContentView(R.layout.activity_test);

        SampleView s1 = findViewById(R.id.scale1);
        SampleView s2 = findViewById(R.id.scale2);
        SampleView s3 = findViewById(R.id.scale3);

//        s1.setScale(3f, 1f);
        s1.setRange(0.25f, 0.5f);
//        s2.setScale(2f, 1f);
//        s3.setScale(0.5f, 0.5f);

        s1.setOnClickListener(v -> {
            Log.w(TAG, "onCreate: click");
            if (clickFlag) {
                s1.setRange(0.25f, 0.5f);
            } else {
                s1.setRange(0.1f, 0.9f);
            }
            clickFlag = !clickFlag;
        });
    }
}
