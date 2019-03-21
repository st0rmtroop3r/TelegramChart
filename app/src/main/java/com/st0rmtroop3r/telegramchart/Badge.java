package com.st0rmtroop3r.telegramchart;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Badge extends LinearLayout {

    TextView txvTitle;
    GridLayout gridLayout;
    int chartWidth;

    public Badge(Context context) {
        super(context);
        init(context);
    }

    public Badge(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Badge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    public void setX(float x) {
        int leftMargin = ((RelativeLayout.LayoutParams)getLayoutParams()).leftMargin;
        int rightMargin = ((RelativeLayout.LayoutParams)getLayoutParams()).rightMargin;
        if (getWidth() + leftMargin + rightMargin > chartWidth - x) {
            x = (int) (x - getWidth() - rightMargin);
        } else {
            x = x + leftMargin;
        }
        super.setX(x);
    }

    void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.widget_badge, this);
        setOrientation(VERTICAL);
        txvTitle = findViewById(R.id.txv_badge_title);
        gridLayout = findViewById(R.id.grl_badge_values);
    }

    void setTitle(String newTitle) {
        txvTitle.setText(newTitle);
    }

    void addValue(String value, String name, int color) {
        BadgeItem badgeItem = new BadgeItem(getContext());
        badgeItem.setData(value, name, color);
        if (gridLayout.getChildCount() % 2 == 0) {
            badgeItem.setPadding(0, 0, 24, 0);
        } else {
            badgeItem.setPadding(24, 0, 0, 0);
        }
        gridLayout.addView(badgeItem);
    }

    void removeValues() {
        gridLayout.removeAllViews();
    }
}
