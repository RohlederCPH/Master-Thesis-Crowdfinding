package com.charleshested.cfcollector;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    private static final String TAG = "ExampleUnitTest";
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void calculateMeanStdev() throws Exception {
        // 10,2,38,23,38,23,21,15
        // median 23
        // mean 21.25
        // stdev 11.744679646546

        generateDistValues();

        List<Integer> rssiData = new ArrayList<>();
        rssiData.add(10);
        rssiData.add(2);
        rssiData.add(38);
        rssiData.add(23);
        rssiData.add(38);
        rssiData.add(23);
        rssiData.add(21);
        rssiData.add(15);

        int median = (int)calculateMedian(rssiData);
        int mean = (int)calculateMean(rssiData);
        int stdev = (int)calculateStdev(rssiData);

        assertEquals(22, median);
        assertEquals(21, mean);
        assertEquals(11, stdev);
    }


    private double calculateMedian(List<Integer> rssiData) {
        int halfSize = rssiData.size() / 2;
        Collections.sort(rssiData);

        if (rssiData.size() % 2 == 0) {
            return (rssiData.get(halfSize - 1) + rssiData.get(halfSize)) * 0.5;
        }
        return rssiData.get(halfSize);
    }

    private double calculateMean(List<Integer> rssiData) {
        double sum = rssiData.stream().mapToInt(Integer::intValue).sum();
        return sum/rssiData.size();
    }

    private double calculateStdev(List<Integer> rssiData) {
        double mean = calculateMean(rssiData);
        double sum = rssiData.stream().mapToDouble(i -> (i - mean) * (i - mean)).sum();
        return Math.sqrt(sum/(rssiData.size()));
    }

    private void generateDistValues() {
        int[] rssiDistanceValues = new int[200];

        // S6
        double a = 73.1;
        double b = 5.62;

        // S8
        //double a = 70.0;
        //double b = 8.46;

        //Log.d(TAG, "valueA: " + a + " - valueB: " + b);

        for (int i = 0; i < rssiDistanceValues.length; i++) {

            double ii = i + 0.0;
            double distance = Math.exp((ii-a)/b);

            if (distance > 20.0) distance = 20.0;
            if (distance < 1) distance = 1.0;

            rssiDistanceValues[i] = (int)distance;
            System.out.println("rssiDistanceValues[" + i + "]: " + rssiDistanceValues[i]);
            //Log.d(TAG, "rssiDistanceValues[ " + i + "]: " + rssiDistanceValues[i]);
        }
    }
}