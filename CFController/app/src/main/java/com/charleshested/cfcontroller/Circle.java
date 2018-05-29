package com.charleshested.cfcontroller;

public class Circle {
    private final Point c;
    private final double rMeters; // Radius to meters
    private final double rDegrees; // Radius to meters

    private Circle(Point c, double r, boolean meters) {

        if (meters) {
            this.c = c;
            this.rMeters = r;
            this.rDegrees = convertDistFromMetersToDegrees(c.x, c.y, r);

        } else {
            this.c = c;
            this.rMeters = convertDistFromDegreesToMeters(c.y, r);
            this.rDegrees = r;
        }
    }

    public static Circle newCircleFromMeters(Point c, double radiusInMeters) {
        final Circle circle = new Circle(c, radiusInMeters, true);
        return circle;
    }

    public static Circle newCircleFromDegrees(Point c, double radiusInDegrees) {
        final Circle circle = new Circle(c, radiusInDegrees, false);
        return circle;
    }



    public double getDistInDegrees() {
        return rDegrees;
    }


    public double getDistInMeters() {
        return rMeters;
    }

    public Point getC() {
        return c;
    }

    private static double earthRadius(double latitude) {

        // R is the calculated radius
        // B is the latitude
        final double r1 = 6_378_137; // Earth radius at sea level at the equator is 6,378,137 m // At latitude=0
        final double r2 = 6_356_752; // Earth radius at sea level at the poles is 6,356,752 m // At latitude=90
        latitude = Math.toRadians(latitude);


        // Equation: R = √ [ (r1² * cos(B))² + (r2² * sin(B))² ] / [ (r1 * cos(B))² + (r2 * sin(B))² ]
        // Equation: R = √ [ a² + b² ] / [ c² + d² ]
        // Equation: R = √ [ e/f ]

        // a = r1² * cos(B)
        // b = r2² * sin(B)
        // c = r1 * cos(B)
        // d = r2 * sin(B)

        // e = a² + b²
        // f = c² + d²

        final double a = (r1*r1) * Math.cos(latitude);
        final double b = (r2*r2) * Math.sin(latitude);
        final double c = r1 * Math.cos(latitude);
        final double d = r2 * Math.sin(latitude);

        final double e = (a*a) + (b*b);
        final double f = (c*c) + (d*d);

        final double R = Math.sqrt(e/f);

        return R;
    }


    public static double convertDistFromDegreesToMeters(double lat, double dist) {

        // Haversine formula:
        // a = sin²(Δφ/2) + cos φ1 ⋅ cos φ2 ⋅ sin²(Δλ/2)
        // c = 2 ⋅ atan2( √a, √(1−a) )
        // d = R ⋅ c

        // Converting to radians before calculating
        double newVar = Math.toRadians(dist/2); // radians

        final double R = earthRadius(lat); // meters
        final double l = Math.toRadians(lat); // radians

        final double a = Math.cos(l) * Math.cos(l) * Math.sin(newVar) * Math.sin(newVar);
        final double c = 2 * Math.atan2( Math.sqrt(a), Math.sqrt(1-a) );
        final double newDistance = R * c;

        return newDistance;
    }


    //https://www.movable-type.co.uk/scripts/latlong.html
    public static double convertDistFromMetersToDegrees(double lng, double lat, double dist) {

        final double orgLng = lng;
        final double orgLat = lat;
        final double orgDist = dist;

        final double bearing = 90;


        final double d = orgDist; // meters
        final double R = earthRadius(orgLat); // meters

        // δ is the angular distance d/R;
        final double dR = Math.toRadians(d/R); // radians

        // Bearing clockwise from north (90 is east)
        final double b = Math.toRadians(bearing); // radians


        System.out.println("d: " + d);
        System.out.println("R: " + R);
        System.out.println("dR: " + dR);
        System.out.println("b: " + b);

        System.out.println("lng old: " + orgLng);
        System.out.println("lat old: " + orgLat);



        final double lng1 = Math.toRadians(orgLng); // radians
        final double lat1 = Math.toRadians(orgLat); // radians
        System.out.println("lng1 new: " + lng1);
        System.out.println("lat1 new: " + lat1);



        final double lat2 = Math.asin( Math.sin(lat1)*Math.cos(dR) + Math.cos(lat1)*Math.sin(dR)*Math.cos(b) );
        final double lng2 = lng1 + Math.atan2(Math.sin(b)*Math.sin(dR)*Math.cos(lat1), Math.cos(dR)-Math.sin(lat1)*Math.sin(lat2));
        System.out.println("lng2 old: " + lng2);
        System.out.println("lat2 old: " + lat2);



        final double lat3 = Math.toDegrees(lat2); // degrees
        final double lng3 = Math.toDegrees(lng2); // degrees
        System.out.println("lng3 new: " + lng3);
        System.out.println("lat3 new: " + lat3);



        final double newDistance = Math.abs(orgLng-lng3);  // degrees
        System.out.println("newDistance: " + newDistance);



        return Math.toDegrees(newDistance);
    }
}
