package com.charleshested.cfcontroller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static com.charleshested.cfcontroller.Circle.newCircleFromDegrees;
import static com.charleshested.cfcontroller.Circle.newCircleFromMeters;
import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class FindingTheBicycleAreaUnitTest {
    private static final double LAT_CPH = 55.676098;
    private static final double RADIUS_CPH = 6_363_591; // in meters
    private static final int LIST_SIZE = 1000;

    // Area coordinates for the test
    private static final double COOR_MIN = 10;
    private static final double COOR_MAX = 990;

    // Bike coordinates. Should be within the test area
    private static final double BIKE_LNG = 500;
    private static final double BIKE_LAT = 500;
    private final String EXP_PATH = "/home/thomas/Documents/Crowdfinding/CFController/app/src/main/java/com/charleshested/cfcontroller/location-export1.txt";



    @Test
    public void testCircle() throws Exception {

        Point p1 = new Point(12, 55);
        Point p2 = new Point(13, 55);

        double d1 = 63.78 * 1000;
        double d2 = 1;

        Circle c1 = Circle.newCircleFromMeters(p1, d1);
        Circle c2 = Circle.newCircleFromDegrees(p1, d2);

        double dist1 = c1.getDistInDegrees();
        double dist2 = c2.getDistInMeters();

        System.out.println("Dist in degrees (should be 1.0): " + dist1);
        System.out.println("Dist in meters (should be 63.78 * 1000): " + dist2);

    }





    @Test
    public void locationListSize() throws Exception {
        List<Circle> randomCircles = createRandomCircles();



        for(Map.Entry<String, List<LocationModel>> entry :  getExperiments(getJsonAsString()).entrySet()) {
            String key = entry.getKey();
            List<LocationModel> value = entry.getValue();
            System.out.println(key);//experiment
            for(int i=0;i<value.size();i++){
                System.out.println("phone " + value.get(i).getPhone());
                System.out.println(value.get(i).getCoordinateY()+";"+value.get(i).getCoordinateX());
                System.out.println("distTotal " + value.get(i).getDistTotal());
                System.out.println("time " + value.get(i).getTimestamp());
            }
            System.out.println("---------------");
        }



        assertEquals(LIST_SIZE, randomCircles.size());
    }


    @Test
    public void intersectionListSize() throws Exception {
        List<Circle> randomCircles = createRandomCircles();
        List<CircleWithIntersections> intersections = createIntersections(randomCircles);
        assertEquals(LIST_SIZE, intersections.size());
    }


    @Test
    public void findHighestIntersections() throws Exception {
        List<Circle> randomCircles = createRandomCircles();
        List<CircleWithIntersections> intersections = createIntersections(randomCircles);
        CircleWithIntersections highest = findCircleWithHighestIntersections(intersections);

        System.out.println("Highest count: " + highest.intersections);
        boolean isHighest = true;
        for (CircleWithIntersections cwi : intersections) {
            if (highest.intersections < cwi.intersections) isHighest = false;
        }

        assertTrue("Has the highest number of intersections: ", isHighest);
    }


    @Test
    public void findCommonIntersectionCount() throws Exception {
        List<Circle> randomCircles = createRandomCircles();
        List<CircleWithIntersections> intersections = createIntersections(randomCircles);
        CircleWithIntersections highest = findCircleWithHighestIntersections(intersections);
        CircleWithIntersections cwi = findCircleWithMostCommonIntersections(highest);
        List<Circle> bestCommonIntersections = mergeTwoListsOfIntersections(highest, cwi);


        System.out.println("bestCommonIntersections size: " + bestCommonIntersections.size());

        assertTrue("Has the highest number of intersections: ", true);
    }


    @Test
    public void testFindIntersectionPointsOfTwoCircles() throws Exception {
        Circle c1 = newCircleFromMeters(new Point(-9, 1), 7);
        Circle c2 = newCircleFromMeters(new Point(5, -5), 18);
        List<Point> points = findIntersectionPointsOfTwoCircles(c1, c2);

        System.out.println("points.size: " + points.size()); // Size = 2

        Point p1 = points.get(0);
        Point p2 = points.get(1);

        System.out.println("p1x: " + p1.x + " - p1y: " + p1.y); // p1x: -12.99820514562319 - p1y: -4.745812006454113
        System.out.println("p2x: " + p2.x + " - p2y: " + p2.y); // p2x: -7.596622440583706 - p2y: 7.857880971971353

        assertTrue("P1 x: ", p1.x > -13.0 || p1.x < -12.9);
        assertTrue("P1 y: ", p1.y > -4.8 || p1.y < -4.7);
        assertTrue("P2 x: ", p2.x > -7.6 || p2.x < -7.5);
        assertTrue("P2 y: ", p2.y > 7.8 || p2.y < 7.9);
    }


    @Test
    public void testEntireFlowOrganized() throws Exception {

        // #0
        List<Circle> circles1 = createOrganizedCircles();
        assertEquals(9, circles1.size());

        // #1
        List<CircleWithIntersections> intersections1 = createIntersections(circles1);
        assertEquals(9, intersections1.size());

        // #2
        CircleWithIntersections cwi1 = findCircleWithHighestIntersections(intersections1);
        assertEquals(3, cwi1.intersections);

        // #3
        CircleWithIntersections cwi2 = findCircleWithMostCommonIntersections(cwi1);
        assertEquals(2, cwi2.intersections);

        // #4
        List<Circle> circles2 = mergeTwoListsOfIntersections(cwi1, cwi2);
        assertEquals(4, circles2.size());

        // #5
        List<Point> points1 = findAllIntersectionPoints(circles2);
        assertEquals(12, points1.size());

        // #6
        List<Point> points2 = findPointsWithinAllIntersections(circles2, points1);
        assertEquals(4, points2.size());

        // #7
        AreaEstModel area1 = generateEstArea(points2);

        if (area1 == null) area1 = new AreaEstModel(-1, -1, -1, BIKE_LNG, BIKE_LAT);

        double x = area1.getEstLng(); // x: 2.5
        double y = area1.getEstLat(); // y: 2.5
        double r = area1.getRadius(); // r: 0.3660254037844384
        // Area: A=PI*r*r    3.1415*0.366*0.366=0.4208


        System.out.println("x: " + x + " - y: " + y + " - r: " + r);

        assertEquals(2.5, x, 0.2);
        assertEquals(2.5, y, 0.2);
        assertTrue(r > 0.36);
        assertTrue(r < 0.37);
    }


    @Test
    public void testEntireFlowRandom() throws Exception {

        // #0
        List<Circle> circles1 = createRandomCirclesInCph();
        assertNotNull(circles1);
        System.out.println("#0 circles1 size: " + circles1.size());

        // #1
        List<CircleWithIntersections> intersections1 = createIntersections(circles1);
        assertNotNull(intersections1);
        System.out.println("#1 intersections1 size: " + intersections1.size());

        // #2
        CircleWithIntersections cwi1 = findCircleWithHighestIntersections(intersections1);
        assertNotNull(cwi1);
        System.out.println("#2 cwi1 intersections: " + cwi1.intersections);

        // #3
        List<Point> points1 = generatePointsToEvaluate(cwi1.self);
        System.out.println("#3 points1 size: " + points1.size());

        // #4
        List<Circle> circles3 = generateCirclesWithMostCommonIntersections(cwi1, points1);
        System.out.println("#4 circles3 size: " + circles3.size());

        // #5
        List<Circle> circles4 = removeEnclosingCircles(circles3);
        assertNotNull(circles4);
        System.out.println("#5 circles4 size: " + circles4.size());

        // #6
        List<Point> points2 = findAllIntersectionPoints(circles4);
        assertNotNull(points2);
        System.out.println("#6 points2 size: " + points2.size());

        // #7
        List<Point> points3 = findPointsWithinAllIntersections(circles4, points2);
        assertNotNull(points3);
        System.out.println("#7 points3 size: " + points3.size());

        // #8
        AreaEstModel area1 = generateEstArea(points3);
        assertNotNull(area1);

        if (area1 == null) area1 = new AreaEstModel(-1, -1, -1, BIKE_LNG, BIKE_LAT);

        double x = area1.getEstLng(); // x: ?
        double y = area1.getEstLat(); // y: ?
        double r = area1.getRadius(); // r: ?


        System.out.println("x: " + x + " - y: " + y + " - r: " + r);
    }





    // #0 - Testing method to create circles with specific parameters
    private List<Circle> createOrganizedCircles() {
        List<Circle> circles = new ArrayList<>();

        // Do not edit
        final Circle c1 = newCircleFromMeters(new Point(2,2), 1);
        final Circle c2 = newCircleFromMeters(new Point(3,2), 1);
        final Circle c3 = newCircleFromMeters(new Point(2,3), 1);
        final Circle c4 = newCircleFromMeters(new Point(3,3), 1);
        final Circle c5 = newCircleFromMeters(new Point(8,8), 2);
        final Circle c6 = newCircleFromMeters(new Point(10,10), 2);
        final Circle c7 = newCircleFromMeters(new Point(22,22), 2);
        final Circle c8 = newCircleFromMeters(new Point(9,2), 2);
        final Circle c9 = newCircleFromMeters(new Point(2,12), 2);

        circles.add(c1);
        circles.add(c2);
        circles.add(c3);
        circles.add(c4);
        circles.add(c5);
        circles.add(c6);
        circles.add(c7);
        circles.add(c8);
        circles.add(c9);

        return circles;
    }

    // #0 - Testing method to create circles with random parameters
    private List<Circle> createRandomCircles() {
        List<Circle> circles = new ArrayList<>();

        for (int i = 0; i < LIST_SIZE; i++) {
            Random r = new Random();


            Map<String, String> timestamp = new HashMap<>();
            String phone = "";
            double coordinateX;
            double coordinateY;
            double distGps;
            double distBle;



            // Assigning a third of the data points to each phone
            if (i % 3 == 0) phone = "phone-AAA";
            if (i % 3 == 1) phone = "phone-BBB";
            if (i % 3 == 2) phone = "phone-CCC";


            // Creates a coordinate between 10 and 1000
            coordinateX = (r.nextDouble() * COOR_MAX) + COOR_MIN;
            coordinateY = (r.nextDouble() * COOR_MAX) + COOR_MIN;


            // Creates a distance between 5 and 200
            double gpsMin = 5;
            double gpsMax = 195;
            distGps = (r.nextDouble() * gpsMax) + gpsMin;


            // Creates a distance between 1 and 20
            double bleMin = 1;
            double bleMax = 19;
            distBle = (r.nextDouble() * bleMax) + bleMin;


            // Adding the data point to the list
            final LocationModel lm = new LocationModel(timestamp, phone, coordinateX, coordinateY, distGps, distBle);

            final Circle circle = lm.toCircle();
            circles.add(circle);
        }
        return circles;
    }

    // #0 - Testing method to create circles with random real coordinates and dist in meters
    private List<Circle> createRandomCirclesInCph() {
        List<Circle> circles = new ArrayList<>();

        for (int i = 0; i < LIST_SIZE; i++) {
            Random r = new Random();


            Map<String, String> timestamp = new HashMap<>();
            String phone = "";
            double coordinateX;
            double coordinateY;
            double distGps;
            double distBle;

            // Assigning a third of the data points to each phone
            if (i % 3 == 0) phone = "phone-AAA";
            if (i % 3 == 1) phone = "phone-BBB";
            if (i % 3 == 2) phone = "phone-CCC";



            // Creates a coordinate in Copenhagen area

            double xMin = 12.497404;
            double xMax = 12.663942 - xMin;

            double yMin = 55.635509;
            double yMax = 55.708342 - yMin;


            coordinateX = (r.nextDouble() * xMax) + xMin;
            coordinateY = (r.nextDouble() * yMax) + yMin;


            // Creates a distance between 5 and 200
            double gpsMin = 5;
            double gpsMax = 2000 - gpsMin;
            distGps = (r.nextDouble() * gpsMax) + gpsMin;


            // Creates a distance between 1 and 20
            double bleMin = 1;
            double bleMax = 20 - bleMin;
            distBle = (r.nextDouble() * bleMax) + bleMin;


            // Adding the data point to the list
            final LocationModel lm = new LocationModel(timestamp, phone, coordinateX, coordinateY, distGps, distBle);

            final Circle circle = lm.toCircle();
            circles.add(circle);
        }
        return circles;
    }

    // #1 - For each circle, find all other circles it intersects with
    private List<CircleWithIntersections> createIntersections(List<Circle> circles) {
        List<CircleWithIntersections> result = new ArrayList<>();

        for (Circle circle : circles) {
            CircleWithIntersections li = new CircleWithIntersections(circle, circles);
            result.add(li);
        }

        return result;
    }

    // #2 - Find the circle with the highest amount of intersections
    private CircleWithIntersections findCircleWithHighestIntersections(List<CircleWithIntersections> intersections) {
        CircleWithIntersections highest = intersections.get(0);

        for (int i = 1; i < intersections.size(); i++) {
            CircleWithIntersections cwi = intersections.get(i);
            if (highest.intersections < cwi.intersections) highest = cwi;
        }

        return highest;
    }

    // #3 - Creates a list of Circles that has the highest amount of shared intersections
    private CircleWithIntersections findCircleWithMostCommonIntersections(CircleWithIntersections intersections) {
        List<Circle> circles1 = intersections.circles; // List to compare with
        List<CircleWithIntersections> intersectionsList = createIntersections(circles1);

        // Make an array that count how many intersecting circles each circle has
        int[] counts = new int[intersectionsList.size()];


        // Loops through the list of intersecting circles from the MAX circle
        for (int i = 0; i < intersectionsList.size(); i++) {

            // Gets the circles intersection list
            CircleWithIntersections cwi = intersectionsList.get(i);
            List<Circle> circles2 = cwi.circles; // The second list
            counts[i] = countSharedIntersections(circles1, circles2);
        }

        int highestNumber = 0;
        int atIndex = 0;
        for (int i = 0; i < counts.length; i++) {
            //System.out.println("counts[" + i + "] = " + counts[i]);
            if (counts[i] > highestNumber) {
                highestNumber = counts[i];
                atIndex = i;
            }
        }

        return intersectionsList.get(atIndex);


        //return mergeTwoListsOfIntersections(intersections, highest);
    }

    // #4 - Given two CircleWithIntersections, this method finds the circles that intersects with both CircleWithIntersections
    private List<Circle> mergeTwoListsOfIntersections(CircleWithIntersections cwi1, CircleWithIntersections cwi2) {
        Set<Circle> resultSet = new HashSet<>();
        resultSet.add(cwi1.self);
        resultSet.add(cwi2.self);

        List<Circle> circles1 = cwi1.circles;
        List<Circle> circles2 = cwi2.circles;

        for (int i = 0; i < circles1.size(); i++) {
            Circle circle = circles1.get(i);
            if (circles2.contains(circle)) resultSet.add(circle);
        }

        return new ArrayList<>(resultSet);
    }

    // #4.5 - Remove circles that entirely covers another circle
    private List<Circle> removeEnclosingCircles(List<Circle> circles) {
        System.out.println(" - Circles size: " + circles.size());

        for (int i = 0; i < circles.size()-1; i++) {
            Circle c1 = circles.get(i);

            for (int j = i; j < circles.size(); j++) {
                Circle c2 = circles.get(j);

                if (isOneCircleInsideAnother(c1, c2)) {

                    // Removing the largest circle from the list
                    //if (c1.r < c2.r) circles.remove(c2);
                    //else circles.remove(c1);
                    //break;


                    // Removing the smallest circle from the list
                    if (c1.getDistInDegrees() > c2.getDistInDegrees()) circles.remove(c2);
                    else circles.remove(c1);
                    return removeEnclosingCircles(circles);
                    //break;


                }

            }
        }

        System.out.println(" - Circles size: " + circles.size());

        return circles;
    }

    // #5 - The 'polygon' can be found by examining all the possible intersection points for all pairs of circles.
    private List<Point> findAllIntersectionPoints(List<Circle> circles) {
        List<Point> result = new ArrayList<>();

        // Go through all the circles except the last one
        for (int i = 0; i < circles.size()-1; i++) {
            Circle circle1 = circles.get(i);

            // Go through all the remaining circles
            for (int j = i+1; j < circles.size(); j++) {
                Circle circle2 = circles.get(j);

                // First check if the circle is within the other circle
                if (isOneCircleInsideAnother(circle1, circle2)) break;

                List<Point> tempResult = findIntersectionPointsOfTwoCircles(circle1, circle2);
                result.addAll(tempResult);


            }
        }

        // First check if the circle is within the other circle

        // Find intersection point between two circles
        // Each point created must be final


        return result;
    }

    // #5.3 - Test if all circles intersects
    private boolean doAllCircleIntersect(List<Circle> circles) {

        for (int i = 0; i < circles.size(); i++) {
            Circle c1 = circles.get(i);

            for (int j = 0; j < circles.size(); j++) {
                Circle c2 = circles.get(j);

                if (c1 != c2) {
                    if (!intersecting(c1, c2)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    // #5.6 - Test if any circle is within another
    private boolean isAnyCirclesWithin(List<Circle> circles) {

        for (int i = 0; i < circles.size(); i++) {
            Circle c1 = circles.get(i);

            for (int j = 0; j < circles.size(); j++) {
                Circle c2 = circles.get(j);

                if (c1 != c2) {
                    if (isOneCircleInsideAnother(c1, c2)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // #6 - The intersection points that are inside all the circles define the perimeter of the polygon.
    private List<Point> findPointsWithinAllIntersections(List<Circle> circles, List<Point> points) {
        List<Point> result = new ArrayList<>();

        //System.out.println("findPointsWithinAllIntersections circles.size: " + circles.size());
        //System.out.println("findPointsWithinAllIntersections  points.size: " + points.size());



        // Evaluate each point and see if it is within all circles. This can be a bit tricky
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            boolean withinAll = true;

            //System.out.println("findPointsWithinAllIntersections  point: " + point.x + " " + point.y);

            for (int j = 0; j < circles.size(); j++) {
                Circle circle = circles.get(j);
                //System.out.println("findPointsWithinAllIntersections circle: " + circle.c.x + " " + circle.c.y + " " + circle.r);


                if (!isPointInsideCircle(point, circle)) {
                    withinAll = false;
                    break;
                }
            }

            // If the point is within all circles, add it to the list
            if (withinAll) {
                result.add(point);
            }
        }

        return result;
    }

    // #7 - Generate the circle of area estimate from a list of points
    private AreaEstModel generateEstArea(List<Point> points) {
        if (points.size() < 2) return null;

        // Find the points with the largest distance between them
        // Calculate a center point between them
        // Set radius as half of the distance

        // Find the two points with the longest distance
        Point firstPoint = points.get(0);
        Point secondPoint = points.get(1);
        double highestDistance = 0.0;

        for (int i = 0; i < points.size()-1; i++) {
            Point p1 = points.get(i);

            for (int j = i+1; j < points.size(); j++) {
                Point p2 = points.get(j);

                double distance = distanceBetweenTwoPoints(p1, p2);

                if (highestDistance < distance) {
                    highestDistance = distance;
                    firstPoint = p1;
                    secondPoint = p2;
                }
            }
        }

        // Find the center point and the radius
        double estLng = (firstPoint.x + secondPoint.x) / 2;
        double estLat = (firstPoint.y + secondPoint.y) / 2;
        double radius = highestDistance / 2;

        //TODO Work from here. We have radius/distance in "degrees" and we want it to meters before making the AreaModel

        radius = Circle.convertDistFromDegreesToMeters(estLat, radius);

        return new AreaEstModel(estLng, estLat, radius, BIKE_LNG, BIKE_LAT);
    }







    // https://bytes.com/topic/java/answers/645269-circle-circle-intersection-more
    private List<Point> findIntersectionPointsOfTwoCircles(Circle c1, Circle c2) {
        List<Point> result = new ArrayList<>();

        final double x1 = c1.getC().x;
        final double y1 = c1.getC().y;
        final double r1 = c1.getDistInDegrees();

        final double x2 = c2.getC().x;
        final double y2 = c2.getC().y;
        final double r2 = c2.getDistInDegrees();

        // First calculate the distance, 'd', between the center-points of the two circles:
        final double d = distanceBetweenTwoPoints(x1, y1, x2, y2);

        // Now we calculate 'd1':
        // d1 = (r1^2 - r2^2 + d^2) / 2*d
        final double d1 = (r1*r1 - r2*r2 + d*d) / (2*d);

        // Now we solve 'h', which is 1/2 * 'a'
        // h = √(r1^2 - d1^2)
        final double h = Math.sqrt(r1*r1 - d1*d1);

        // To find point P3(x3,y3) which is the intersection of line 'd' and 'a' we use the following formula:
        // x3 = x1 + ((d1 * (x2 - x1)) / d)
        // y3 = y1 + ((d1 * (y2 - y1)) / d)
        final double x3 = x1 + ((d1 * (x2 - x1)) / d);
        final double y3 = y1 + ((d1 * (y2 - y1)) / d);

        // Last but not least, calculate the points P4 and P5 which are the intersection points of the two circles:
        // x4 = x3 + ((h * (y2 - y1)) / d)
        // y4 = y3 - ((h * (x2 - x1)) / d)
        // x5 = x3 - ((h * (y2 - y1)) / d)
        // y5 = y3 + ((h * (x2 - x1)) / d)
        final double x4 = x3 + ((h * (y2 - y1)) / d);
        final double y4 = y3 - ((h * (x2 - x1)) / d);
        final double x5 = x3 - ((h * (y2 - y1)) / d);
        final double y5 = y3 + ((h * (x2 - x1)) / d);

        final Point p4 = new Point(x4, y4);
        final Point p5 = new Point(x5, y5);

        result.add(p4);
        result.add(p5);

        return result;
    }

    private int countSharedIntersections(List<Circle> circles1, List<Circle> circles2) {
        int count = 0;

        // Loops through the list
        for (int i = 0; i < circles1.size(); i++) {
            Circle circle1 = circles1.get(i);

            for (int j = 0; j < circles2.size(); j++) {
                Circle circle2 = circles2.get(j);
                if (circle1.equals(circle2)) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    private double distanceBetweenTwoPoints(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
    }

    private double distanceBetweenTwoPoints(Point p1, Point p2) {
        return distanceBetweenTwoPoints(p1.x, p1.y, p2.x, p2.y);
    }

    private double distanceBetweenTwoPoints(double x, double y, Circle circle) {
        return distanceBetweenTwoPoints(x, y, circle.getC().x, circle.getC().y);
    }

    private boolean isOneCircleInsideAnother(Circle c1, Circle c2) {

        if (c1 == c2) {
            return false;
        }

        // Calculate the distance between center points
        double distance = distanceBetweenTwoPoints(c1.getC(), c2.getC());
        double largestRadius = ((c1.getDistInDegrees() < c2.getDistInDegrees()) ? c2.getDistInDegrees() : c1.getDistInDegrees());

        // Add the lowest radius to the distance
        if (c1.getDistInDegrees() < largestRadius) distance += c1.getDistInDegrees();
        else distance += c2.getDistInDegrees();

        // If the distance is lower than the largest radius, then the circles does not intersect
        return distance < largestRadius;
    }

    private boolean intersecting(double x1, double y1, double r1, double x2, double y2, double r2) {
        double pointDist = distanceBetweenTwoPoints(x1, y1, x2, y2);
        double radiusDist = r1 + r2;
        return pointDist < radiusDist;
    }

    private boolean intersecting(Circle c1, Circle c2) {

        if (c1 == c2) {
            return false;
        }


        double x1 = c1.getC().x;
        double y1 = c1.getC().y;
        double r1 = c1.getDistInDegrees();

        double x2 = c2.getC().x;
        double y2 = c2.getC().y;
        double r2 = c2.getDistInDegrees();

        return intersecting(x1, y1, r1, x2, y2, r2);
    }

    private boolean isPointInsideCircle(Point point, Circle circle) {
        return isPointInsideCircle(point.x, point.y, circle);
    }


    private boolean isPointInsideCircle(double x, double y, Circle circle) {
        double distance = distanceBetweenTwoPoints(x, y, circle);
        return distance <= circle.getDistInDegrees() * 1.001; // TODO Edit this
    }



    // #3 - This method takes the circle with most intersections
    private List<Point> generatePointsToEvaluate(Circle circle) {
        List<Point> result = new ArrayList<>();

        // The amount of detail in the imagined coordinate system
        // This is uses to calculate the amount to move each point in the x/y axis
        int factor = 200;

        // Finding the x min/max bounds
        double xStart = circle.getC().x - circle.getDistInDegrees();
        double xEnd = circle.getC().x + circle.getDistInDegrees();
        double xIncrement = Math.abs((xEnd - xStart)) / factor;

        // Finding the y min/max bounds
        double yStart = circle.getC().y - circle.getDistInDegrees();
        double yEnd = circle.getC().y + circle.getDistInDegrees();
        double yIncrement = Math.abs((yEnd - yStart)) / factor;



        /*
        System.out.println("-----------------");
        System.out.println("west: " + west);
        System.out.println("east: " + east);
        System.out.println("north: " + north);
        System.out.println("south: " + south);
        System.out.println("xIncrement: " + xIncrement);
        System.out.println("yIncrement: " + yIncrement);
        */





        // Loops through the coordinates and evaluates each point
        for (double x = xStart; x < xEnd; x += xIncrement) {
            for (double y = yStart; y < yEnd; y += yIncrement) {

                // If the point is within the circle, then add it to the result list
                if (isPointInsideCircle(x, y, circle)) {
                    result.add(new Point(x, y));
                }
            }
        }

        return result;
    }

    // #4
    private List<Circle> generateCirclesWithMostCommonIntersections(CircleWithIntersections intersections, List<Point> points) {
        List<Circle> result = new ArrayList<>();

        // All the circles that intersects this circle
        List<Circle> circles = intersections.circles;
        circles.add(intersections.self); // Adding itself to the list

        if (circles.size() < 1 || points.size() < 1) {
            return result;
        }

        // Keeps track of the point with most overlapping circles
        int highestCount = 0;
        Point bestPoint = points.get(0);

        // Finds the point with most overlap. Each point gets evaluated against all circles.
        // It counts how many circles overlaps a given point. The point with the highest amount
        // of overlapping circles is saved.
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            int count = 0;


            for (int j = 0; j < circles.size(); j++) {
                Circle circle = circles.get(j);

                if (isPointInsideCircle(point, circle)) {
                    count++;
                }
            }

            if (highestCount < count) {
                highestCount = count;
                bestPoint = point;
            }
        }

        System.out.println("Point count: " + highestCount);

        // After finding the point with the most overlapping circles, the point is evaluated against
        // all the circles to see which once it is within.
        for (int i = 0; i < circles.size(); i++) {
            Circle circle = circles.get(i);

            if (isPointInsideCircle(bestPoint, circle)) {
                result.add(circle);
            }
        }

        return result;
    }





    private class CircleWithIntersections {
        final Circle self;
        final List<Circle> circles;
        final int intersections;

        CircleWithIntersections(Circle model, List<Circle> circles) {
            //circles = new ArrayList<>();
            this.self = model;
            this.circles = createList(circles);
            this.intersections = this.circles.size();
        }

        private List<Circle> createList(List<Circle> circles) {
            List<Circle> result = new ArrayList<>();

            for (Circle model : circles) {
                if (!self.equals(model) && intersecting(self, model)) result.add(model);
            }

            return result;
        }
    }



    @Test
    public void testCircleDistance() throws Exception {


        final double x = 55;
        final double y = 12;
        final double r = 100;

        Circle circle1 = newCircleFromMeters(new Point(x, y), r);

        double distDegrees1 = circle1.getDistInDegrees();
        double distMeters1 = circle1.getDistInMeters();

        System.out.println("distDegrees1: " + distDegrees1);
        System.out.println("distMeters1: " + distMeters1);



        Circle circle2 = newCircleFromDegrees(new Point(x, y), distDegrees1);

        double distDegrees2 = circle2.getDistInDegrees();
        double distMeters2 = circle2.getDistInMeters();

        System.out.println("distDegrees2: " + distDegrees2);
        System.out.println("distMeters2: " + distMeters2);




        Circle circle3 = newCircleFromMeters(new Point(x, y), distMeters2);

        double distDegrees3 = circle3.getDistInDegrees();
        double distMeters3 = circle3.getDistInMeters();

        System.out.println("distDegrees3: " + distDegrees3);
        System.out.println("distMeters3: " + distMeters3);



        //assertEquals(LIST_SIZE, randomCircles.size());
    }





    private String getJsonAsString() throws IOException, JSONException {
        String jsonString = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(EXP_PATH));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            jsonString = sb.toString();

        } finally {
            br.close();
        }


        return jsonString;
    }


    private Map<String, List<LocationModel>> getExperiments(String jsonString) throws JSONException {

        Map<String,List<LocationModel>> modelMap = new HashMap<>();
        List<String> expNames = new ArrayList<String>();
        expNames.add("s8-1");
        expNames.add("s8-2");
        expNames.add("-s8-1");
        expNames.add("-s8-2");
        expNames.add("s6-1");
        expNames.add("s6-2");
        expNames.add("-s6-1");
        expNames.add("-s6-2");
        expNames.add("-S6S8-1");
        expNames.add("-S6S8-2");
        expNames.add("S6S8-1");
        expNames.add("S6S8-2");



        JSONObject reader = new JSONObject(jsonString);

        List<LocationModel> locationModels = new ArrayList<>();

            for(int j=0; j<expNames.size();j++){
                JSONArray main  = reader.getJSONArray(expNames.get(j));

                for(int i=0; i<main.length();i++){
                    Object timestamp = null;
                    String phone= null;
                    double coordinateX =-1;
                    double coordinateY=-1;
                    double distGps=-1;
                    double distBle=-1;
                    double distTotal=-1;

                    JSONObject jsonObj = (JSONObject) main.get(i);

                    timestamp = jsonObj.getLong("timestamp");
                    phone = jsonObj.getString("phone");

                    coordinateX = jsonObj.getDouble("coordinateX");
                    coordinateY = jsonObj.getDouble("coordinateY");

                    distGps = jsonObj.getDouble("distGps");
                    distBle = jsonObj.getDouble("distBle");

                    distTotal = jsonObj.getDouble("distTotal");
                    LocationModel locationModel = new LocationModel();
                    locationModel.setTimestamp(timestamp);
                    locationModel.setPhone(phone);
                    locationModel.setDistBle(distBle);
                    locationModel.setDistGps(distGps);
                    locationModel.setCoordinateX(coordinateX);
                    locationModel.setCoordinateY(coordinateY);
                    locationModel.setDistTotal(distTotal);
                    locationModels.add(locationModel);
                }
                modelMap.put(expNames.get(j),locationModels);
            }


            return modelMap;
    }

}















































