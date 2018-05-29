package com.charleshested.cfcontroller;

public class RssiEvaluationModel {
    private int distance;
    private double mean;
    private double accuracy;
    private double error;
    private double stdev;
    private int confidence;

    public RssiEvaluationModel() {

    }

    public RssiEvaluationModel(int distance, double mean, double stdev) {
        this.distance = distance;
        this.mean = mean;
        this.accuracy = Math.abs(distance - mean);
        this.error =  Math.abs(distance - mean) / mean;
        this.stdev = stdev;
        this.confidence = (int)(accuracy / stdev) + 1;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public double getError() {
        return error;
    }

    public void setError(double error) {
        this.error = error;
    }

    public double getStdev() {
        return stdev;
    }

    public void setStdev(double stdev) {
        this.stdev = stdev;
    }

    public int getConfidence() {
        return confidence;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }
}
