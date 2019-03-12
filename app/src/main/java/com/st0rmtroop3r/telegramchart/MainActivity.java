package com.st0rmtroop3r.telegramchart;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ReactiveChartView reactiveChartView = findViewById(R.id.reactive);
        ChartWindowSelector chartWindowSelector = findViewById(R.id.selector);
        LinearLayout checkBoxes = findViewById(R.id.ll_checkBoxes);

        Pair<int[], String> y0pair = DataProvider.getY0Data();
        Pair<int[], String> y1pair = DataProvider.getY1Data();

        List<Pair<int[], Integer>> list = new ArrayList<>();
        list.add(new Pair<>(y0pair.first, Color.parseColor(y0pair.second)));
        list.add(new Pair<>(y1pair.first, Color.parseColor(y1pair.second)));

        chartWindowSelector.setChartsData(list);
        reactiveChartView.setChartsData(list);
        chartWindowSelector.setSelectionListener((left, right) -> {
            Log.w(TAG, "listener: left: $left, right: $right");
            reactiveChartView.setRange(left, right);

        });
    }
}
