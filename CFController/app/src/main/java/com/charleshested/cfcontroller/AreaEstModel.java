package com.charleshested.cfcontroller;

import com.google.firebase.database.Exclude;

public class AreaEstModel {
    private long timestamp;
    private double estLng;
    private double estLat;
    private double radius;
    private double bikeLng;
    private double bikeLat;
    private double distToBike;
    private boolean inArea;

    public AreaEstModel() {
    }

    public AreaEstModel(double estLng, double estLat, double radius, double bikeLng, double bikeLat) {
        this.timestamp = System.currentTimeMillis();
        this.estLat = estLat;
        this.estLng = estLng;
        this.radius = radius;
        this.bikeLat = bikeLat;
        this.bikeLng = bikeLng;
        updateDistToBike();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getEstLng() {
        return estLng;
    }

    public void setEstLng(double estLng) {
        this.estLng = estLng;
    }

    public double getEstLat() {
        return estLat;
    }

    public void setEstLat(double estLat) {
        this.estLat = estLat;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getBikeLng() {
        return bikeLng;
    }

    public void setBikeLng(double bikeLng) {
        this.bikeLng = bikeLng;
    }

    public double getBikeLat() {
        return bikeLat;
    }

    public void setBikeLat(double bikeLat) {
        this.bikeLat = bikeLat;
    }

    public double getDistToBike() {
        return distToBike;
    }

    public void setDistToBike(double distToBike) {
        this.distToBike = distToBike;
    }

    public boolean isInArea() {
        return inArea;
    }

    public void setInArea(boolean inArea) {
        this.inArea = inArea;
    }

    @Exclude
    public void updateDistToBike() {
        double distanceInDegrees = Math.sqrt((bikeLng-estLng)*(bikeLng-estLng) + (bikeLat-estLat)*(bikeLat-estLat));  // Distance in degrees
        this.distToBike = Circle.convertDistFromDegreesToMeters(estLat, distanceInDegrees); // Distance in meters
        this.inArea = distToBike < radius;
    }

    @Exclude
    public Circle toCircle() {
        final Point point = new Point(estLng, estLat);
        return Circle.newCircleFromMeters(point, radius);
    }
}
