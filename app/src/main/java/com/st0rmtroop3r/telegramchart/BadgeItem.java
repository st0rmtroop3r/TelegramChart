package com.st0rmtroop3r.telegramchart;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.widget_badge_item, this);
        setOrientation(VERTICAL);
        txvChartValue = findViewById(R.id.txv_chart_value);
        txvChartName = findViewById(R.id.txv_chart_name);
    }

    void setData(String value, String name, int color) {
        txvChartValue.setTextColor(color);
        txvChartValue.setText(value);
        txvChartName.setTextColor(color);
        txvChartName.setText(name);
    }
}
