package com.charleshested.cfcontroller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    public static final String EXP_RUNNING = "running_experiment";
    public static final String EXP_FUNCTION_COEFFICIENTS = "equation_values";
    public static final String EXP_RSSI_ANAL = "analysis";
    public static final String EXP_RSSI_EVAL = "evaluation";
    public static final String EXP_LOCATION = "location";
    public static final String EXP_AREA = "area";
    public static final String EXP_CANCEL = "cancel";
    public static final String DATABASE_CANCEL = EXP_CANCEL + "," + "0" + "," + "none" + "," + "none";
    public static final String BIKE_LOCK_NAME = "AXA:E99E82D29AA9486F0E10";
    public static final String PHONE_NAME_MOTO_E2 = "motorola MotoE2(4G-LTE)";
    public static final String PHONE_NAME_SAMSUNG_6 = "samsung SM-G928F";
    public static final String PHONE_NAME_SAMSUNG_8 = "samsung SM-G950F";

    public static final int EXP_CONT = 2;

    private String emailString;

    private Button buttonRssiData;
    private Button buttonAccuracyData;
    private Button buttonTest;

    private Button buttonRssiCollect;
    private Button buttonRssiEval;
    private Button buttonLocExp;
    private Button buttonEquationValues;
    private Button buttonMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        emailString = "";

        // ----- RSSI AND DISTANCE

        buttonRssiCollect = findViewById(R.id.EnterRssiCollect);
        buttonRssiCollect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RssiAnalysisActivity.class);
                startActivity(i);
            }
        });

        buttonRssiEval = findViewById(R.id.EnterRssiEval);
        buttonRssiEval.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RssiEvaluationActivity.class);
                startActivity(i);
            }
        });

        buttonEquationValues = findViewById(R.id.button_equation_values);
        buttonEquationValues.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), FunctionCoefficientsActivity.class);
                startActivity(i);
            }
        });

        buttonRssiData = findViewById(R.id.btn_dl_rssi_data);
        buttonRssiData.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                downloadRssiData();
            }
        });

        buttonAccuracyData = findViewById(R.id.btn_dl_ac_data);
        buttonAccuracyData.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                downloadAccuracyData();
            }
        });



        // ----- LOCATION OF THE BICYCLE

        buttonLocExp = findViewById(R.id.EnterLocationExperiment);
        buttonLocExp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), LocationActivity.class);
                startActivity(i);
            }
        });

        buttonMap = findViewById(R.id.button_map);
        buttonMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(i);
            }
        });

        // Test button
        buttonTest = findViewById(R.id.btn_test);
        buttonTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //addBikeLocationToAreas();
                //generateAreaDataAsString();
                //addNewLocations();
            }
        });
        buttonTest.setEnabled(false);
        buttonTest.setVisibility(View.GONE);
    }


    private void downloadRssiData() {

        DatabaseReference s6db = FirebaseDatabase.getInstance()
                .getReference()
                .child(EXP_RSSI_ANAL)
                .child(PHONE_NAME_SAMSUNG_6);

        s6db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                StringBuilder message = new StringBuilder(PHONE_NAME_SAMSUNG_6 + "\n");

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    RssiAnalysisModel model = snapshot.getValue(RssiAnalysisModel.class);
                    if (model != null) {
                        message.append(model.getDistance())
                                .append(",")
                                .append(model.getRssiMedian())
                                .append("\n");
                    }
                }

                if (emailString.length() == 0) emailString = message.toString();
                else {
                    message.append(emailString);
                    final String emailMsg = message.toString();
                    sendEmail(emailMsg, EXP_RSSI_ANAL);
                    emailString ="";
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        DatabaseReference s8db = FirebaseDatabase.getInstance()
                .getReference()
                .child(EXP_RSSI_ANAL)
                .child(PHONE_NAME_SAMSUNG_8);

        s8db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                StringBuilder message = new StringBuilder(PHONE_NAME_SAMSUNG_8 + "\n");

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    RssiAnalysisModel model = snapshot.getValue(RssiAnalysisModel.class);
                    if (model != null) {
                        message.append(model.getDistance())
                                .append(",")
                                .append(model.getRssiMedian())
                                .append("\n");
                    }
                }

                if (emailString.length() == 0) emailString = message.toString();
                else {
                    message.append(emailString);
                    final String emailMsg = message.toString();
                    sendEmail(emailMsg, EXP_RSSI_ANAL);
                    emailString = "";
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private void downloadAccuracyData() {

        DatabaseReference s6db = FirebaseDatabase.getInstance()
                .getReference()
                .child(EXP_RSSI_EVAL)
                .child(PHONE_NAME_SAMSUNG_6);

        s6db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                StringBuilder message = new StringBuilder(PHONE_NAME_SAMSUNG_6 + "\n");

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    RssiEvaluationModel model = snapshot.getValue(RssiEvaluationModel.class);
                    if (model != null) {
                        message.append(model.getDistance())
                                .append(",")
                                .append(model.getMean())
                                .append(",")
                                .append(model.getStdev())
                                .append(",")
                                .append(model.getError())
                                .append("\n");
                    }
                }

                if (emailString.length() == 0) emailString = message.toString();
                else {
                    message.append(emailString);
                    final String emailMsg = message.toString();
                    sendEmail(emailMsg, EXP_RSSI_EVAL);
                    emailString = "";
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        DatabaseReference s8db = FirebaseDatabase.getInstance()
                .getReference()
                .child(EXP_RSSI_EVAL)
                .child(PHONE_NAME_SAMSUNG_8);

        s8db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                StringBuilder message = new StringBuilder(PHONE_NAME_SAMSUNG_8 + "\n");

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    RssiEvaluationModel model = snapshot.getValue(RssiEvaluationModel.class);
                    if (model != null) {
                        message.append(model.getDistance())
                                .append(",")
                                .append(model.getMean())
                                .append(",")
                                .append(model.getStdev())
                                .append(",")
                                .append(model.getError())
                                .append("\n");
                    }
                }

                if (emailString.length() == 0) emailString = message.toString();
                else {
                    message.append(emailString);
                    final String emailMsg = message.toString();
                    sendEmail(emailMsg, EXP_RSSI_EVAL);
                    emailString ="";
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }



    private void sendEmail(String msg, String subject) {
        String[] TO = {"tcha@itu.dk", "jhes@itu.dk"};
        String[] CC = {""};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, msg);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }


    /*
    private void generateAreaDataAsString() {

        String[] exp_names = new String[]{
                "Havnegade 47 - RSSI",
                "Havnegade 47 - PROX",
                "Sværtegade 5 - RSSI",
                "Sværtegade 5 - PROX",
                "Amagertorv - RSSI",
                "Amagertorv - PROX"
        };


        // can only handle 2 strings at a time. Edit 'start' to either 0, 2, or 4.
        final int start = 0;
        final int end = start + 2;
        for (int i = start; i < end; i++) {
            final String name = exp_names[i];

            DatabaseReference db = FirebaseDatabase.getInstance()
                    .getReference()
                    .child(EXP_AREA)
                    .child(name)
                    .child(BIKE_LOCK_NAME);

            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    StringBuilder message = new StringBuilder("timestamp,radius,inArea,estLat,estLng,bikeLat,bikeLng,distToBike" + "\n");
                    List<AreaEstModel> areas = new ArrayList<>();

                    // Add all areas to the list
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        AreaEstModel model = snapshot.getValue(AreaEstModel.class);
                        boolean hasArea = false;

                        if (model != null) {

                            for (int i = 0; i < areas.size(); i++) {
                                AreaEstModel model2 = areas.get(i);

                                if (model.getEstLng() == model2.getEstLng()
                                        && model.getEstLat() == model2.getEstLat()
                                        && model.getRadius() == model2.getRadius()) {

                                    hasArea = true;
                                    break;
                                }
                            }

                            if (!hasArea) areas.add(model);
                        }
                    }


                    // Add each area to the string
                    for (AreaEstModel area : areas) {
                        message.append(area.getTimestamp())
                                .append(",")
                                .append(area.getRadius())
                                .append(",")
                                .append(area.isInArea())
                                .append(",")
                                .append(area.getEstLat())
                                .append(",")
                                .append(area.getEstLng())
                                .append(",")
                                .append(area.getBikeLat())
                                .append(",")
                                .append(area.getBikeLng())
                                .append(",")
                                .append(area.getDistToBike())
                                .append("\n");
                    }

                    sendEmail(message.toString(), name);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
    */






    /*
    private void addBikeLocationToAreas() {
        Log.d(TAG, "addBikeLocationToAreas start");

        final String[] names = new String[]{
                "10",
                "11",
                "12",
                "-10",
                "-11",
                "-12"
        };


        final double[] coordsLat = new double[]{
                55.677984,
                55.681083,
                55.678842,
                55.677984,
                55.681083,
                55.678842
        };

        final double[] coordsLng = new double[]{
                12.592026,
                12.580567,
                12.579377,
                12.592026,
                12.580567,
                12.579377
        };



        for (int i = 0; i < names.length; i++) {
            final String name = names[i];
            final double lng = coordsLng[i];
            final double lat = coordsLat[i];

            final DatabaseReference db = FirebaseDatabase.getInstance()
                    .getReference()
                    .child(EXP_AREA)
                    .child(name)
                    .child(BIKE_LOCK_NAME);

            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "addBikeLocationToAreas onDataChange");

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        AreaEstModel oldArea = snapshot.getValue(AreaEstModel.class);

                        if (oldArea == null) return;

                        AreaEstModel newArea = new AreaEstModel(
                                oldArea.getEstLng(),
                                oldArea.getEstLat(),
                                oldArea.getRadius(),
                                lng,
                                lat);
                        newArea.setTimestamp(oldArea.getTimestamp());


                        String newName = name + "-new";
                        FirebaseDatabase.getInstance()
                                .getReference()
                                .child(EXP_AREA)
                                .child(newName)
                                .child(BIKE_LOCK_NAME)
                                .push()
                                .setValue(newArea);

                        Log.d(TAG, "addBikeLocationToAreas dataSend");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "Data failed: " + name);
                }
            });
        }
    }
    */




    /*
    private void addNewLocations() {
        Log.d(TAG, "addNewLocations start");

        final String[] oldNames = new String[]{
                "10",
                "11",
                "12"
        };

        final String[] newNames = new String[]{
                "Havnegade 47",
                "Sværtegade 5",
                "Amagertorv"
        };



        for (int i = 2; i < 3; i++) {
            final String oldName = oldNames[i];
            final String newNameRssi = newNames[i] + " - RSSI";
            final String newNameprox = newNames[i] + " - PROX";

            final DatabaseReference db = FirebaseDatabase.getInstance()
                    .getReference()
                    .child(EXP_LOCATION)
                    .child(oldName)
                    .child(BIKE_LOCK_NAME);

            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "addNewLocations onDataChange");

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        LocationModel model = snapshot.getValue(LocationModel.class);

                        if (model == null) return;


                        FirebaseDatabase.getInstance()
                                .getReference()
                                .child(EXP_LOCATION)
                                .child(newNameRssi)
                                .child(BIKE_LOCK_NAME)
                                .push()
                                .setValue(model);


                        LocationModel newLocation = new LocationModel(
                                model.getTimestamp(),
                                model.getPhone(),
                                model.getCoordinateX(),
                                model.getCoordinateY(),
                                model.getDistGps(),
                                20);

                        FirebaseDatabase.getInstance()
                                .getReference()
                                .child(EXP_LOCATION)
                                .child(newNameprox)
                                .child(BIKE_LOCK_NAME)
                                .push()
                                .setValue(newLocation);

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "Data failed: " + oldName);
                }
            });
        }
    }
    */

}



