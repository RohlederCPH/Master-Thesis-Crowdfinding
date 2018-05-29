package com.charleshested.cfcontroller;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.charleshested.cfcontroller.MainActivity.BIKE_LOCK_NAME;
import static com.charleshested.cfcontroller.MainActivity.EXP_AREA;
import static com.charleshested.cfcontroller.MainActivity.EXP_LOCATION;
import static com.charleshested.cfcontroller.MainActivity.PHONE_NAME_SAMSUNG_6;
import static com.charleshested.cfcontroller.MainActivity.PHONE_NAME_SAMSUNG_8;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener {
    private static final String TAG = "LocationActivity";

    private static final LatLng[] BIKE_LOCATIONS = new LatLng[]{
            new LatLng(55.677984, 12.592026),  // Havnegade 47 - RSSI
            new LatLng(55.677984, 12.592026),  // Havnegade 47 - PROX
            new LatLng(55.681083, 12.580567),  // Sværtegade 5 - RSSI
            new LatLng(55.681083, 12.580567),  // Sværtegade 5 - PROX
            new LatLng(55.678842, 12.579377),  // Amagertorv - RSSI
            new LatLng(55.678842, 12.579377)}; // Amagertorv - PROX

    private static final String[] EXPERIMENTS_NAME = new String[]{
            "Havnegade 47 - RSSI",
            "Havnegade 47 - PROX",
            "Sværtegade 5 - RSSI",
            "Sværtegade 5 - PROX",
            "Amagertorv - RSSI",
            "Amagertorv - PROX"
    };

    private String mExpName;
    private Button mCalcBnt;
    private Button mLocationBnt;
    private Button mAreaBnt;
    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private Spinner mSpinner;
    private boolean mCalculating;
    private Map<String, LatLng> mStreetCoordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
        mCalcBnt = findViewById(R.id.calcBnt);
        mLocationBnt = findViewById(R.id.btn_location);
        mAreaBnt = findViewById(R.id.btn_area);

        mStreetCoordinates = new HashMap<>();
        for (int i = 0; i < EXPERIMENTS_NAME.length; i++) mStreetCoordinates.put(EXPERIMENTS_NAME[i], BIKE_LOCATIONS[i]);

        //Dropdown mSpinner for selecting experiment to display
        mSpinner = findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MapActivity.this,
                android.R.layout.simple_spinner_item,
                EXPERIMENTS_NAME);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(this);

        mCalcBnt.setEnabled(false);
        mCalculating = false;

        mCalcBnt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!mCalculating) {
                    downloadAndCalculateAndUpload();
                }
            }
        });

        mLocationBnt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LatLng bikeLocation = mStreetCoordinates.get(mExpName);
                downloadLocations(bikeLocation.longitude, bikeLocation.latitude);
            }
        });

        mAreaBnt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LatLng bikeLocation = mStreetCoordinates.get(mExpName);
                downloadAreas(bikeLocation.longitude, bikeLocation.latitude);
            }
        });
        enabledButtons(false);
    }


    private void downloadAndCalculateAndUpload() {
        mCalcBnt.setEnabled(false);
        mCalculating = true;

        final DatabaseReference database = FirebaseDatabase.getInstance()
                .getReference()
                .child(EXP_LOCATION)
                .child(mExpName)
                .child(BIKE_LOCK_NAME);

        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<LocationModel> locations = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LocationModel model = snapshot.getValue(LocationModel.class);
                    locations.add(model);
                }


                if (locations.size() > 0) {
                    Toast.makeText(MapActivity.this, "Download complete", Toast.LENGTH_SHORT).show();
                    calculateResult(locations);
                } else {
                    Toast.makeText(MapActivity.this, "Download failed", Toast.LENGTH_SHORT).show();
                    mCalcBnt.setEnabled(true);
                    mCalculating = false;
                }

                Toast.makeText(MapActivity.this, "Calculate complete", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MapActivity.this, "Download failed", Toast.LENGTH_SHORT).show();
                mCalcBnt.setEnabled(true);
                mCalculating = false;
            }
        });
    }


    private void calculateResult(List<LocationModel> locations) {
        if (locations.size() == 0) return;

        // Creating a sublist of Location Models
        for (int i = 1; i < locations.size(); i++) {
            List<LocationModel> tempList;

            if (i == locations.size() - 1) tempList = locations;
            else tempList = locations.subList(0, i);

            List<Circle> circles = new ArrayList<>();

            for (int j = 0; j < tempList.size(); j++) {
                circles.add(tempList.get(j).toCircle());
            }
            //Log.d(TAG, "calculateArea(circles) : " + circles);

            LatLng latLng = mStreetCoordinates.get(mExpName);

            double bikeLng = latLng.longitude;
            double bikeLat = latLng.latitude;

            final AreaEstModel area = calculateArea(circles, bikeLng, bikeLat);

            if (area != null) {
                // Setting the bike location
                sendAreaToServer(mExpName, area);
            }
        }

        mCalculating = false;
        mCalcBnt.setEnabled(true);
    }


    private void sendAreaToServer(String expName, AreaEstModel area) {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(EXP_AREA)
                .child(expName)
                .child(BIKE_LOCK_NAME)
                .push()
                .setValue(area);
    }


    private void downloadLocations(final double bikeLng, final double bikeLat) {
        enabledButtons(false);

        final DatabaseReference database = FirebaseDatabase.getInstance()
                .getReference()
                .child(EXP_LOCATION)
                .child(mExpName)
                .child(BIKE_LOCK_NAME);

        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<LocationModel> locations = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LocationModel model = snapshot.getValue(LocationModel.class);
                    if (model != null) locations.add(model);
                }

                clearMap();
                drawLocationsOnMap(locations);
                moveMapToPosition(bikeLat, bikeLng);
                drawBicycleOnMap(bikeLat, bikeLng);
                enabledButtons(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MapActivity.this, "Download failed", Toast.LENGTH_SHORT).show();
                enabledButtons(true);
            }
        });
    }


    private void downloadAreas(final double bikeLng, final double bikeLat) {
        enabledButtons(false);

        final DatabaseReference database = FirebaseDatabase.getInstance()
                .getReference()
                .child(EXP_AREA)
                .child(mExpName)
                .child(BIKE_LOCK_NAME);

        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<AreaEstModel> areas = new ArrayList<>();
                Log.d(TAG, "dataSnapshot size: " + dataSnapshot.getChildrenCount());

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    AreaEstModel model = snapshot.getValue(AreaEstModel.class);
                    boolean hasArea = false;

                    if (model != null) {

                        // Only adding areas with different coordinates or radius
                        for (int i = 0; i < areas.size(); i++) {
                            AreaEstModel model2 = areas.get(i);

                            if (model.getEstLng() == model2.getEstLng() && model.getEstLat() == model2.getEstLat() && model.getRadius() == model2.getRadius()) {
                                hasArea = true;
                                break;
                            }
                        }

                        if (!hasArea) areas.add(model);
                    }
                }

                clearMap();
                drawAreasOnMap(areas);
                moveMapToPosition(bikeLat, bikeLng);
                drawBicycleOnMap(bikeLat, bikeLng);
                enabledButtons(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MapActivity.this, "Download failed", Toast.LENGTH_SHORT).show();
                enabledButtons(true);

            }
        });
    }


    private void drawLocationsOnMap(List<LocationModel> locations) {
        if (mGoogleMap == null || locations.size() < 1) return;

        for (int i = 0; i < locations.size(); i++) {
            LocationModel location = locations.get(i);

            if (location != null) {

                // Setting the circle color so it is different for each phone
                int color = Color.LTGRAY;
                if (location.getPhone().equals(PHONE_NAME_SAMSUNG_6)) color = Color.MAGENTA;
                if (location.getPhone().equals(PHONE_NAME_SAMSUNG_8)) color = Color.GREEN;

                mGoogleMap.addCircle(new CircleOptions()
                        .center(new LatLng(location.getCoordinateY(), location.getCoordinateX()))
                        .radius(location.getDistTotal())
                        .strokeColor(color)
                        .strokeWidth(3f));
            }
        }
    }


    private void drawAreasOnMap(List<AreaEstModel> areas) {
        if (mGoogleMap == null || areas.size() < 1) return;

        for (int i = 0; i < areas.size(); i++) {
            AreaEstModel area = areas.get(i);

            if (area != null) {
                // Setting the alpha to be different between each data point
                int alpha = 255/areas.size(); // 80
                int min = 100;
                int max = 255;
                int increment = ((max-min)*3) / areas.size();

                // First increment blue. Then decrement green. Then increment red.
                int blue = min;
                int green = max;
                int red = min;

                if ((areas.size()/3) > i) {
                    blue += increment*i;
                    if (blue > max) blue = max;

                } else if ((2*areas.size()/3) > i) {
                    blue = max;
                    green -= increment*i;
                    if (green < min) green = min;

                } else {
                    blue = max;
                    green = min;
                    red += increment*i;
                    if (red > max) red = max;
                }


                int color = Color.argb(alpha, red, green, blue);

                mGoogleMap.addCircle(new CircleOptions()
                        .center(new LatLng(area.getEstLat(), area.getEstLng()))
                        .radius(area.getRadius())
                        .strokeColor(Color.DKGRAY)
                        .strokeWidth(2f)
                        .fillColor(color));
            }
        }
    }


    private void drawBicycleOnMap(double lat, double lng) {
        int color = Color.rgb(255, 1, 1);

        if (mGoogleMap != null) mGoogleMap
                .addCircle(new CircleOptions()
                        .center(new LatLng(lat, lng))
                        .radius(2)
                        .strokeColor(color)
                        .fillColor(color))
                .setZIndex(1.0f);
    }


    private void moveMapToPosition(double lat, double lng) {
        if (mGoogleMap != null) mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 18.0f));
    }


    private void clearMap() {
        if (mGoogleMap != null) mGoogleMap.clear();
    }


    private void enabledButtons(boolean enabled) {
        mLocationBnt.setEnabled(enabled);
        mAreaBnt.setEnabled(enabled);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        mExpName = EXPERIMENTS_NAME[position];
    }


    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        mExpName = EXPERIMENTS_NAME[0];
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng copenhagen = new LatLng(55.681177, 12.581904);
        mGoogleMap = googleMap;
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(copenhagen, 13.0f));
        mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
        enabledButtons(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }




    // #0 - Calculating the estimated area based on a list of circles
    private AreaEstModel calculateArea(List<Circle> circles, double bikeLng, double bikeLat) {

        if (circles.size() == 1) {
            Circle c = circles.get(0);

            AreaEstModel area = new AreaEstModel(
                    c.getC().x,
                    c.getC().y,
                    c.getDistInMeters(),
                    bikeLng,
                    bikeLat);

            return area;
        }


        Log.d(TAG, "--- calculateArea Async. Circles size: " + circles.size());
        // #1
        List<CircleWithIntersections> intersections1 = createIntersections(circles);
        Log.d(TAG, "--- calculateArea Async - intersections1.size: " + intersections1.size());

        // #2
        CircleWithIntersections cwi1 = findCircleWithHighestIntersections(intersections1);
        Log.d(TAG, "--- calculateArea Async - cwi1.intersections: " + cwi1.intersections);

        // #3
        List<Point> points1 = generatePointsToEvaluate(cwi1.self);
        Log.d(TAG, "--- calculateArea Async - points1.size: " + points1.size());

        // #4
        List<Circle> circles1 = generateCirclesWithMostCommonIntersections(cwi1, points1);
        Log.d(TAG, "--- calculateArea Async - circles1.size: " + circles1.size());

        // #5
        List<Circle> circles2 = removeEnclosingCircles(circles1);
        Log.d(TAG, "--- calculateArea Async - circles2.size: " + circles2.size());

        // #6
        List<Point> points2 = findAllIntersectionPoints(circles2);
        Log.d(TAG, "--- calculateArea Async - points2.size: " + points2.size());

        // #7
        List<Point> points3 = findPointsWithinAllIntersections(circles2, points2);
        Log.d(TAG, "--- calculateArea Async - points3.size: " + points3.size());

        // #8
        final AreaEstModel area1 = generateEstArea(points3, bikeLng, bikeLat);
        Log.d(TAG, "--- calculateArea Async - area1 NN: " + (area1 != null));

        return area1;
    }

    // #1 - For each circle, find all other circles it intersects with
    private List<CircleWithIntersections> createIntersections(List<Circle> circles) {
        List<CircleWithIntersections> result = new ArrayList<>();

        for (int i = 0; i < circles.size(); i++) {
            Circle circle = circles.get(i);
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

    // #3 - This method takes the circle with most intersections
    private List<Point> generatePointsToEvaluate(Circle circle) {
        List<Point> result = new ArrayList<>();

        // The amount of detail in the imagined coordinate system
        // This is uses to calculate the amount to move each point in the x/y axis
        int factor = 100;

        // Finding the x min/max bounds
        double xStart = circle.getC().x - circle.getDistInDegrees();
        double xEnd = circle.getC().x + circle.getDistInDegrees();
        double xIncrement = Math.abs((xEnd - xStart)) / factor;

        // Finding the y min/max bounds
        double yStart = circle.getC().y - circle.getDistInDegrees();
        double yEnd = circle.getC().y + circle.getDistInDegrees();
        double yIncrement = Math.abs((yEnd - yStart)) / factor;

        // Loops through the coordinates and evaluates each point
        for (double x = xStart; x < xEnd; x += xIncrement) {
            for (double y = yStart; y < yEnd; y += yIncrement) {

                // If the point is within the circle, then add it to the result list
                if (isPointInsideCircle(x, y, circle)) result.add(new Point(x, y));
            }
        }

        return result;
    }

    // #4 - Given a list of circles where some intersects each other, this method
    //      finds the circles with most shared intersections.
    private List<Circle> generateCirclesWithMostCommonIntersections(CircleWithIntersections intersections, List<Point> points) {
        List<Circle> result = new ArrayList<>();

        // All the circles that intersects this circle
        List<Circle> circles = intersections.circles;
        circles.add(intersections.self); // Adding itself to the list

        if (circles.size() < 1 || points.size() < 1) return result;

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
                if (isPointInsideCircle(point, circle)) count++;
            }

            if (highestCount < count) {
                highestCount = count;
                bestPoint = point;
            }
        }

        // After finding the point with the most overlapping circles, the point is evaluated against
        // all the circles to see which once it is within.
        for (int i = 0; i < circles.size(); i++) {
            Circle circle = circles.get(i);
            if (isPointInsideCircle(bestPoint, circle)) result.add(circle);
        }

        return result;
    }

    // #5 - Remove circles that entirely covers another circle
    private List<Circle> removeEnclosingCircles(List<Circle> circles) {

        for (int i = 0; i < circles.size() - 1; i++) {
            Circle c1 = circles.get(i);

            for (int j = i; j < circles.size(); j++) {
                Circle c2 = circles.get(j);

                if (isOneCircleInsideAnother(c1, c2)) {

                    // Removing the largest circle from the list
                    if (c1.getDistInDegrees() < c2.getDistInDegrees()) circles.remove(c2);
                    else circles.remove(c1);
                    return removeEnclosingCircles(circles);
                }
            }
        }

        return circles;
    }

    // #6 - Finding all the possible intersection points for all pairs of circles.
    private List<Point> findAllIntersectionPoints(List<Circle> circles) {
        List<Point> result = new ArrayList<>();

        // Go through all the circles except the last one
        for (int i = 0; i < circles.size() - 1; i++) {
            Circle circle1 = circles.get(i);

            // Go through all the remaining circles
            for (int j = i + 1; j < circles.size(); j++) {
                Circle circle2 = circles.get(j);

                // First check if the circle is within the other circle
                if (isOneCircleInsideAnother(circle1, circle2)) break;

                List<Point> tempResult = findIntersectionPointsOfTwoCircles(circle1, circle2);
                result.addAll(tempResult);
            }
        }

        return result;
    }

    // #7 - The intersection points that are inside all the circles define the perimeter of the area.
    private List<Point> findPointsWithinAllIntersections(List<Circle> circles, List<Point> points) {
        List<Point> result = new ArrayList<>();

        // If the list only has one circle because the surrounding circles has been removed
        // then we use the two point opposite of each other on the circle periphery
        if (circles.size() == 1) {
            Circle c = circles.get(0);

            double x1 = c.getC().x;
            double y1 = c.getC().y + c.getDistInDegrees();

            double x2 = c.getC().x;
            double y2 = c.getC().y - c.getDistInDegrees();


            Point p1 = new Point(x1, y1);
            Point p2 = new Point(x2, y2);

            result.add(p1);
            result.add(p2);

            return result;
        }

        // Evaluate each point and see if it is within all circles.
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            boolean withinAll = true;

            for (int j = 0; j < circles.size(); j++) {
                Circle circle = circles.get(j);

                if (!isPointInsideCircle(point, circle)) {
                    withinAll = false;
                    break;
                }
            }

            // If the point is within all circles, add it to the list
            if (withinAll) result.add(point);
        }

        return result;
    }

    // #8 - Generate the circle of area estimate from a list of points. This uses the two points
    //      furthers apart and uses this distance as diameter.
    private AreaEstModel generateEstArea(List<Point> points, double bikeLng, double bikeLat) {
        if (points.size() < 2) return null;

        // Find the points with the largest distance between them
        // Calculate a center point between them
        // Set radius as half of the distance

        // Find the two points with the longest distance
        Point firstPoint = points.get(0);
        Point secondPoint = points.get(1);
        double highestDistance = 0.0;

        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);

            for (int j = i + 1; j < points.size(); j++) {
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

        radius = Circle.convertDistFromDegreesToMeters(estLat, radius);

        return new AreaEstModel(estLng, estLat, radius, bikeLng, bikeLat);
    }


    // Reference: http://www.ambrsoft.com/TrigoCalc/Circles2/circle2intersection/CircleCircleIntersection.htm
    private List<Point> findIntersectionPointsOfTwoCircles(Circle c1, Circle c2) {
        List<Point> result = new ArrayList<>();

        final double x1 = c1.getC().x;
        final double y1 = c1.getC().y;
        final double r1 = c1.getDistInDegrees();

        final double x2 = c2.getC().x;
        final double y2 = c2.getC().y;
        final double r2 = c2.getDistInDegrees();

        // Calculate the distance between the center-points of the two circles:
        final double d = distanceBetweenTwoPoints(x1, y1, x2, y2);

        // Calculate d1:
        // d1 = (r1^2 - r2^2 + d^2) / 2*d
        final double d1 = (r1 * r1 - r2 * r2 + d * d) / (2 * d);

        // Calculate h:
        // h = √(r1^2 - d1^2)
        final double h = Math.sqrt(r1 * r1 - d1 * d1);

        // Calculate P3(x3,y3):
        // x3 = x1 + ((d1 * (x2 - x1)) / d)
        // y3 = y1 + ((d1 * (y2 - y1)) / d)
        final double x3 = x1 + ((d1 * (x2 - x1)) / d);
        final double y3 = y1 + ((d1 * (y2 - y1)) / d);

        // Calculate P4(x4,y4) and P5(x5,y5) which are the intersection points of the two circles:
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

    private double distanceBetweenTwoPoints(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    private double distanceBetweenTwoPoints(Point p1, Point p2) {
        return distanceBetweenTwoPoints(p1.x, p1.y, p2.x, p2.y);
    }

    private double distanceBetweenTwoPoints(double x, double y, Circle circle) {
        return distanceBetweenTwoPoints(x, y, circle.getC().x, circle.getC().y);
    }

    private boolean isOneCircleInsideAnother(Circle c1, Circle c2) {

        if (c1 == c2) return false;

        // Calculate the distance between center points
        double distance = distanceBetweenTwoPoints(c1.getC(), c2.getC());
        double largestRadius = ((c1.getDistInDegrees() < c2.getDistInDegrees()) ? c2.getDistInDegrees() : c1.getDistInDegrees());

        // Add the lowest radius to the distance
        if (c1.getDistInDegrees() < largestRadius) distance += c1.getDistInDegrees();
        else distance += c2.getDistInDegrees();

        // If the combined distance is lower than the largest radius, then one circle is fully inside the other
        return distance < largestRadius;
    }

    private boolean intersecting(double x1, double y1, double r1, double x2, double y2, double r2) {
        double pointDist = distanceBetweenTwoPoints(x1, y1, x2, y2);
        double radiusDist = r1 + r2;
        return pointDist < radiusDist;
    }

    private boolean intersecting(Circle c1, Circle c2) {

        if (c1 == c2) return false;

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
        return distance <= circle.getDistInDegrees() * 1.001;
    }

    private class CircleWithIntersections {
        Circle self;
        List<Circle> circles;
        int intersections;

        CircleWithIntersections(Circle model, List<Circle> circles) {
            this.self = model;
            this.circles = createList(circles);
            this.intersections = this.circles.size();
        }

        private List<Circle> createList(List<Circle> circles) {
            List<Circle> result = new ArrayList<>();

            for (int i = 0; i < circles.size(); i++) {
                Circle circle = circles.get(i);

                if (!(self == circle)) {
                    if (intersecting(self, circle)) result.add(circle);
                }
            }

            intersections = result.size();
            return result;
        }
    }
}
