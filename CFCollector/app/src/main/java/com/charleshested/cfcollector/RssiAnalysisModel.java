package com.charleshested.cfcollector;

public class RssiAnalysisModel {
    private int distance;
    private double rssiMedian;

    public RssiAnalysisModel() {

    }

    public RssiAnalysisModel(int distance, double rssiMedian) {
        this.distance = distance;
        this.rssiMedian = rssiMedian;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public double getRssiMedian() {
        return rssiMedian;
    }

    public void setRssiMedian(double rssiMedian) {
        this.rssiMedian = rssiMedian;
    }
}
