package com.charleshested.cfcollector;

import com.google.firebase.database.Exclude;

public class LocationModel {
    private Object timestamp;
    private String phone;
    private double coordinateX;
    private double coordinateY;
    private double distGps;
    private double distBle;
    private double distTotal;

    public LocationModel() {

    }

    public LocationModel(Object timestamp,
                         String phone,
                         double coordinateX,
                         double coordinateY,
                         double distGps,
                         double distBle) {

        this.timestamp = timestamp;
        this.phone = phone;
        this.coordinateX = coordinateX;
        this.coordinateY = coordinateY;
        this.distGps = distGps;
        this.distBle = distBle;
        this.distTotal = distGps + distBle;
    }

    public long getTimestamp() {
        return (long)timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getCoordinateX() {
        return coordinateX;
    }

    public void setCoordinateX(double coordinateX) {
        this.coordinateX = coordinateX;
    }

    public double getCoordinateY() {
        return coordinateY;
    }

    public void setCoordinateY(double coordinateY) {
        this.coordinateY = coordinateY;
    }

    public double getDistGps() {
        return distGps;
    }

    public void setDistGps(double distGps) {
        this.distGps = distGps;
    }

    public double getDistBle() {
        return distBle;
    }

    public void setDistBle(double distBle) {
        this.distBle = distBle;
    }

    public double getDistTotal() {
        return distTotal;
    }

    public void setDistTotal(double distTotal) {
        this.distTotal = distTotal;
    }

    @Exclude
    public Circle toCircle() {
        final Point point = new Point(coordinateX, coordinateY);
        return Circle.newCircleFromMeters(point, distTotal);
    }
}
