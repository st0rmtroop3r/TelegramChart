package com.st0rmtroop3r.telegramchart;

import android.content.Context;
import android.util.JsonReader;
import android.util.Pair;

import com.st0rmtroop3r.telegramchart.enitity.Chart;
import com.st0rmtroop3r.telegramchart.enitity.ChartLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DataProvider {

    final static String TAG = DataProvider.class.getSimpleName();

    static List<Chart> readChartsData(Context appContext, int resId) {
        List<Chart> charts = new ArrayList<>();
        InputStream in = appContext.getResources().openRawResource(resId);
        try (JsonReader jsonReader = new JsonReader(new InputStreamReader(in))) {
            charts = readCharts(jsonReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return charts;
    }

    private static List<Chart> readCharts(JsonReader jsonReader) throws IOException {
        List<Chart> charts = new ArrayList<>();

        jsonReader.beginArray();

        while (jsonReader.hasNext()) {
            charts.add(readChart(jsonReader));
        }
        jsonReader.endArray();
        return charts;
    }

    private static Chart readChart(JsonReader jsonReader) throws IOException {
        Chart chart = new Chart();
        jsonReader.beginObject();

        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "columns":
                    readColumns(jsonReader, chart);
                    break;
                case "names":
                    readNames(jsonReader, chart);
                    break;
                case "colors":
                    readColors(jsonReader, chart);
                    break;
                default:
                    jsonReader.skipValue();
                    break;
            }
        }

        jsonReader.endObject();
        return chart;
    }

    private static void readColumns(JsonReader jsonReader, Chart chart) throws IOException {
        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            Pair<String, List<Long>> pair = readColumn(jsonReader);
            if (pair.first.startsWith("x")) {
                chart.xData = new long[pair.second.size()];
                for (int i = 0; i < pair.second.size(); i++) {
                    chart.xData[i] = pair.second.get(i);
                }
            }
            if (pair.first.startsWith("y")) {
                ChartLine chartLine = new ChartLine();
                chartLine.id = pair.first;
                chartLine.yData = new int[pair.second.size()];
                for (int i = 0; i < pair.second.size(); i++) {
                    chartLine.yData[i] = pair.second.get(i).intValue();
                }
                chart.chartLines.add(chartLine);
            }
        }
        jsonReader.endArray();
    }

    private static Pair<String, List<Long>> readColumn(JsonReader jsonReader) throws IOException {
        List<Long> longs = new ArrayList<>();
        jsonReader.beginArray();
        String id = jsonReader.nextString();
        while (jsonReader.hasNext()) {
            long nextLong = jsonReader.nextLong();
            longs.add(nextLong);
        }
        jsonReader.endArray();
        return new Pair<>(id, longs);
    }

    private static void readNames(JsonReader jsonReader, Chart chart) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String nextName = jsonReader.nextName();
            for (ChartLine chartLine : chart.chartLines) {
                if (nextName.equals(chartLine.id)) {
                    chartLine.name = jsonReader.nextString();
                }
            }
        }
        jsonReader.endObject();
    }

    private static void readColors(JsonReader jsonReader, Chart chart) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String nextName = jsonReader.nextName();
            for (ChartLine chartLine : chart.chartLines) {
                if (nextName.equals(chartLine.id)) {
                    chartLine.color = jsonReader.nextString();
                }
            }
        }
        jsonReader.endObject();
    }



































}

