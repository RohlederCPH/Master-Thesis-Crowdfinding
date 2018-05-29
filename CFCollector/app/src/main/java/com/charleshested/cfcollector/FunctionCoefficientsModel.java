package com.charleshested.cfcollector;

public class FunctionCoefficientsModel {
    private double valueA;
    private double valueB;
    private double error;

    public FunctionCoefficientsModel() {
    }

    public FunctionCoefficientsModel(double valueA, double valueB, double error) {
        this.valueA = valueA;
        this.valueB = valueB;
        this.error = error;
    }

    public double getValueA() {
        return valueA;
    }

    public void setValueA(double valueA) {
        this.valueA = valueA;
    }

    public double getValueB() {
        return valueB;
    }

    public void setValueB(double valueB) {
        this.valueB = valueB;
    }

    public double getError() {
        return error;
    }

    public void setError(double error) {
        this.error = error;
    }
}
