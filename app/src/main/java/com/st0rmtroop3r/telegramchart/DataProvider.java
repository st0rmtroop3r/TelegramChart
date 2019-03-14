package com.st0rmtroop3r.telegramchart;

import android.util.Pair;

import java.util.Arrays;

public class DataProvider {

    static int[] y0 = {
            0, 150, 300,
            37,20,32,39,32,35,19,65,36,62,
            113,69,120,60,51,49,71,122,149,69,
            57,21,33,55,92,62,47,50,56,116,
            63,60,55,65,76,33,45,64,54,81,
            180,123,106,37,60,70,46,68,46,51,
            300,
            33,57,75,70,95,70,50,68,63,66,
            53,38,52,109,121,53,36,71,96,55,
            58,29,31,55,52,44,126,191,73,87,
            255,278,219,170,129,125,126,84,65,53,
            154,57,71,64,75,72,39,47,52,73,

//            89,156,86,105,88,45,33,56,142,124,
            0, 75, 150, 300};

    static int[] y1 = {
            300, 150, 0,
            22,12,30,40,33,23,18,41,45,69,
            57,61,70,47,31,34,40,55,27,57,
            48,32,40,49,54,49,34,51,51,51,
            66,51,94,60,64,28,44,96,49,73,
            30,88,63,42,56,67,52,67,35,61,
            300,
            40,55,63,61,105,59,51,76,63,57,
            47,56,51,98,103,62,54,104,48,41,
            41,37,30,28,26,37,65,86,70,81,
            54,74,70, 50,74,79,85,62,36,46,
            68,43,66,50,28,66,39,23,63,74,

//            83,66,40,60,29,36,27,54,89,50,
            300, 150, 75, 0};

    static String y0color = "#3DC23F";
    static String y1color = "#F34C44";

    static Pair<int[], String> getY0Data() {
        return new Pair<>(y0, y0color);
    }

    static Pair<int[], String> getY1Data() {
        return new Pair<>(y1, y1color);
    }

    static Pair<int[], String> getY0ShortData(int arraySize) {
        return new Pair<>(Arrays.copyOf(y0, arraySize), y0color);
    }

    static Pair<int[], String> getY1ShortData(int arraySize) {
        return new Pair<>(Arrays.copyOf(y1, arraySize), y1color);
    }

    static Pair<int[], String> get10(int arraySize) {
        return new Pair<>(new int[]{1,0}, y1color);
    }


}

