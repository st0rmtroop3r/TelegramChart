package com.st0rmtroop3r.telegramchart.enitity;

import java.util.ArrayList;
import java.util.List;

public class ChartData {

    public long[] xData;

    public List<ChartYData> yDataList = new ArrayList<>();

    public boolean yScaled;

    public boolean stacked;

    public boolean percentage;
}
