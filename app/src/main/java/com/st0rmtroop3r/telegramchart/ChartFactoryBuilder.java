package com.st0rmtroop3r.telegramchart;

import android.content.Context;

public class ChartFactoryBuilder {

    private Context context;
    private String type;
    private boolean yScaled;
    private boolean stacked;
    private boolean percentage;

    public ChartFactoryBuilder(Context appContext) {
        this.context = appContext;
    }

    public ChartFactoryBuilder type(String type) {
        this.type = type;
        return this;
    }

    public ChartFactoryBuilder isYScaled(boolean yScaled) {
        this.yScaled = yScaled;
        return this;
    }

    public ChartFactoryBuilder isStacked(boolean stacked) {
        this.stacked = stacked;
        return this;
    }

    public ChartFactoryBuilder isPercentage(boolean percentage) {
        this.percentage = percentage;
        return this;
    }

    public ChartFactory build() {
        return new ChartFactory(this);
    }

    public Context getContext() {
        return context;
    }

    public boolean isStacked() {
        return stacked;
    }

    public boolean isYScaled() {
        return yScaled;
    }

    public boolean isPercentage() {
        return percentage;
    }

    public String getType() {
        return type;
    }
}
