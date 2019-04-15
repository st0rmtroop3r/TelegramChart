package com.st0rmtroop3r.telegramchart;

import android.content.Context;
import android.util.JsonReader;
import android.util.Log;
import android.util.Pair;

import com.st0rmtroop3r.telegramchart.enitity.ChartData;
import com.st0rmtroop3r.telegramchart.enitity.ChartYData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DataProvider {

    final static String TAG = DataProvider.class.getSimpleName();

    static List<ChartData> readChartsData(Context appContext, int resId) {
        List<ChartData> charts = new ArrayList<>();
        InputStream in = appContext.getResources().openRawResource(resId);
        try (JsonReader jsonReader = new JsonReader(new InputStreamReader(in))) {
            charts = readCharts(jsonReader);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return charts;
    }

    static ChartData readChartData(Context appContext, int resId) {
        InputStream in = appContext.getResources().openRawResource(resId);
        try (JsonReader jsonReader = new JsonReader(new InputStreamReader(in))) {
            return readChart(jsonReader);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static List<ChartData> readCharts(JsonReader jsonReader) throws IOException {
        List<ChartData> charts = new ArrayList<>();

        jsonReader.beginArray();

        while (jsonReader.hasNext()) {
            charts.add(readChart(jsonReader));
        }
        jsonReader.endArray();
        return charts;
    }

    private static ChartData readChart(JsonReader jsonReader) throws IOException {
        ChartData chart = new ChartData();
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
                case "types":
                    readTypes(jsonReader, chart);
                    break;
                case "y_scaled":
                    chart.yScaled = jsonReader.nextBoolean();
                    break;
                case "stacked":
                    chart.stacked = jsonReader.nextBoolean();
                    break;
                case "percentage":
                    chart.percentage = jsonReader.nextBoolean();
                    break;
                default:
                    jsonReader.skipValue();
                    break;
            }
        }

        jsonReader.endObject();
        return chart;
    }

    private static void readColumns(JsonReader jsonReader, ChartData chart) throws IOException {
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
                ChartYData chartLine = new ChartYData();
                chartLine.id = pair.first;
                chartLine.yData = new int[pair.second.size()];
                for (int i = 0; i < pair.second.size(); i++) {
                    chartLine.yData[i] = pair.second.get(i).intValue();
                }
                chart.yDataList.add(chartLine);
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

    private static void readNames(JsonReader jsonReader, ChartData chart) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String nextName = jsonReader.nextName();
            for (ChartYData chartLine : chart.yDataList) {
                if (nextName.equals(chartLine.id)) {
                    chartLine.name = jsonReader.nextString();
                }
            }
        }
        jsonReader.endObject();
    }

    private static void readTypes(JsonReader jsonReader, ChartData chart) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
//            String ns = jsonReader.nextString();
//            Log.w(TAG, "readTypes: " + ns);
            boolean consumed = false;
            String nextName = jsonReader.nextName();
            for (ChartYData chartLine : chart.yDataList) {
                if (nextName.equals(chartLine.id)) {
                    chartLine.type = jsonReader.nextString();
                    consumed = true;
                }
            }
            if (!consumed) {
                Log.w(TAG, "readNames: skip '" + jsonReader.nextString() + "'");
            }
        }
        jsonReader.endObject();
    }

    private static void readColors(JsonReader jsonReader, ChartData chart) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String nextName = jsonReader.nextName();
            for (ChartYData chartLine : chart.yDataList) {
                if (nextName.equals(chartLine.id)) {
                    chartLine.color = jsonReader.nextString();
                }
            }
        }
        jsonReader.endObject();
    }



































}

