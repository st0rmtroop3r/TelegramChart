package com.st0rmtroop3r.telegramchart.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.st0rmtroop3r.telegramchart.R;

public class BadgeItem extends LinearLayout {

    private TextView txvChartValue;
    private TextView txvChartName;

    public BadgeItem(Context context) {
        super(context);
        init(context);
    }

    public BadgeItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BadgeItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.widget_badge_item, this);
        setOrientation(VERTICAL);
        txvChartValue = findViewById(R.id.txv_chart_line_value);
        txvChartName = findViewById(R.id.txv_chart_line_name);
    }

    public void setData(String value, String name, int color) {
        txvChartValue.setTextColor(color);
        txvChartValue.setText(value);
        txvChartName.setTextColor(color);
        txvChartName.setText(name);
    }
}
