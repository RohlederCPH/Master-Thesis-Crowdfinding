package com.charleshested.cfcollector;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CollectorActivity extends AppCompatActivity {
    private static final String TAG = "CollectorActivity";
    private static final String BIKE_LOCK_NAME = "AXA:E99E82D29AA9486F0E10";
    private static final String PHONE_NAME_MOTO_E2 = "motorola MotoE2(4G-LTE)";
    private static final String PHONE_NAME_SAMSUNG_6 = "samsung SM-G928F";
    private static final String PHONE_NAME_SAMSUNG_8 = "samsung SM-G950F";

    private static final String EXP_RUNNING = "running_experiment";
    private static final String EXP_RSSI_ANAL = "analysis";
    private static final String EXP_RSSI_EVAL = "evaluation";
    private static final String EXP_LOCATION = "location";
    private static final String EXP_CANCEL = "cancel";
    private static final String EXP_FUNCTION_COEFFICIENTS = "equation_values";
    private static final String DATABASE_CANCEL = EXP_CANCEL + "," + "0" + "," + "none" + "," + "none";

    private static final String PHONE_MODEL = Build.MANUFACTURER + " " + Build.MODEL;

    private static final double MAX_DISTANCE = 20.0;
    private static final int DATA_POINTS = 40;
    private static final int BLE_SCAN_PERIOD = 5_000; // 5.000 ms = 5 s
    private static final int GPS_MIN_INTERVAL = 2_000; // 2.000 ms = 2 s
    private static final int GPS_MAX_INTERVAL = 10_000; // 10.000 ms = 10 s
    private static final int SOUND_ERROR = 201;
    private static final int SOUND_START = 202;
    private static final int SOUND_SUCCESS = 203;
    private static final int SOUND_BIP = 204;
    private static final int LOCATION_PERMISSION_CODE = 301;
    private static final int BLUETOOTH_ENABLE_CODE = 501;
    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
    };

    //UI elements
    private TextView mTextViewPhone;
    private TextView mTextViewExp;
    private TextView mTextViewDist;
    private TextView mTextViewCount;
    private TextView mTextViewTotal;

    private String mExpName;
    private String mLocationExpName;
    private int mExpDistance;
    private int dpCounter;
    private int[] rssiDistanceValues;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private Handler mHandler;
    private SoundPool mSoundPool;
    private BleScanCallback mScanCallback;
    private BleLocationCallback mBleLocationCallback;
    private boolean mScanning;
    private FusedLocationProviderClient mFusedLocationClient;
    private GpsLocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collector);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keeps screen on
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mTextViewPhone = findViewById(R.id.textViewPhone);
        mTextViewExp = findViewById(R.id.textViewExp);
        mTextViewDist = findViewById(R.id.textViewDist);
        mTextViewCount = findViewById(R.id.textViewCount);
        mTextViewTotal = findViewById(R.id.textViewTotal);

        mTextViewPhone.setText(PHONE_MODEL);
        mTextViewTotal.setText(String.valueOf(DATA_POINTS));

        // Instantiating Bluetooth
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) requestPermissions();

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) mBluetoothAdapter = bluetoothManager.getAdapter();

        // Requesting to turn on Bluetooth
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BLUETOOTH_ENABLE_CODE);
        }

        // Creating the sound pool
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build();
        mSoundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(attributes)
                .build();


        dpCounter = 0;
        rssiDistanceValues = new int[200];
        generateDistValues();
        listenForExperiments();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) finish();
    }

    private void stopScan() {
        if (mLocationCallback != null
                && mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }

        if (mScanning
                && mBluetoothAdapter != null
                && mBluetoothAdapter.isEnabled()
                && mBluetoothLeScanner != null) {

            if (mBleLocationCallback != null) mBluetoothLeScanner.stopScan(mBleLocationCallback);
            if (mScanCallback != null) mBluetoothLeScanner.stopScan(mScanCallback);
        }

        mBleLocationCallback = null;
        mScanCallback = null;
        mScanning = false;
        mHandler = null;
    }

    private void startRssiAnalysis(final int distance) {
        playSound(SOUND_START);
        startBleScan();

        Runnable scanRunnable = new Runnable() {
            @Override
            public void run() {
                if (isDataEnough()) {
                    getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.light_green));
                    playSound(SOUND_SUCCESS);
                    List<Integer> result = mScanCallback.getResult(BIKE_LOCK_NAME); // Get the result, then stop scanning
                    Double median = calculateMedian(result);
                    sendAnalysisToServer(median, distance, PHONE_MODEL);
                    stopScan();

                } else if (mHandler != null) mHandler.postDelayed(this, BLE_SCAN_PERIOD);
            }
        };

        mHandler = new Handler();
        mHandler.postDelayed(scanRunnable, BLE_SCAN_PERIOD);
    }

    private void startRssiEvaluation(int distance) {
        playSound(SOUND_START);
        startBleScan();

        Runnable scanRunnable = new Runnable() {
            @Override
            public void run() {
                if (isDataEnough()) {
                    getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.light_green));
                    playSound(SOUND_SUCCESS);
                    List<Integer> rssiResults = mScanCallback.getResult(BIKE_LOCK_NAME); // Get the result, then stop scanning
                    List<Integer> distResults = getDistancesFromRssi(rssiResults);
                    distResults = removeOutliers(distResults);
                    Double mean = calculateMean(distResults);
                    Double stdev = calculateStdev(distResults);

                    sendEvaluationToServer(mean, distance, stdev, PHONE_MODEL);
                    stopScan();

                } else if (mHandler != null) mHandler.postDelayed(this, BLE_SCAN_PERIOD);
            }
        };

        mHandler = new Handler();
        mHandler.postDelayed(scanRunnable, BLE_SCAN_PERIOD);

    }

    private void startLocationExperiment(String locationExpName) {
        if (mScanning) return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        playSound(SOUND_START);

        mLocationCallback = new GpsLocationCallback();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(GPS_MAX_INTERVAL);
        mLocationRequest.setFastestInterval(GPS_MIN_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,  null /* Looper */);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        mBleLocationCallback = new BleLocationCallback(locationExpName);
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeScanner.startScan(new ArrayList<>(), settings, mBleLocationCallback);
        mScanning = true;
    }

    private void listenForExperiments() {
        // Listens for which experiment to start and stop
        DatabaseReference database = FirebaseDatabase.getInstance()
                .getReference()
                .child(EXP_RUNNING);

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String experiment = dataSnapshot.getValue(String.class);

                if (experiment == null) return;

                String[] experimentList = experiment.split(",");

                if (experimentList.length != 4 || experimentList[1] == null) return;

                mExpName = experimentList[0];
                mExpDistance = Integer.valueOf(experimentList[1]);
                mLocationExpName = experimentList[2];

                switch (experimentList[0]) {
                    case EXP_RSSI_ANAL:
                        dpCounter = 0;
                        getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.background_light));
                        startRssiAnalysis(mExpDistance);
                        break;

                    case EXP_RSSI_EVAL:
                        dpCounter = 0;
                        getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.background_light));
                        startRssiEvaluation(mExpDistance);
                        break;

                    case EXP_LOCATION:
                        getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.background_light));
                        startLocationExperiment(mLocationExpName);
                        break;

                    case EXP_CANCEL:
                        stopScan();
                        break;

                    default:
                        break;
                }

                updateUi();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void updateUi() {
        mTextViewExp.setText(mExpName);
        mTextViewDist.setText(String.valueOf(mExpDistance));
        mTextViewCount.setText(String.valueOf(dpCounter));
    }

    // Start scanning for Bluetooth devices
    private void startBleScan() {
        if (mScanning) return;

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        mScanCallback = new BleScanCallback();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeScanner.startScan(new ArrayList<>(), settings, mScanCallback);
        mScanning = true;
    }

    // Stop Bluetooth scanning
    private void stopBleScan() {
        if (mScanning && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(mScanCallback);
        }

        mScanCallback = null;
        mScanning = false;
        mHandler = null;
    }

    private boolean isDataEnough() {
        if (mScanCallback == null) return false;

        List<Integer> result = mScanCallback.getResult(BIKE_LOCK_NAME);
        return result != null && mScanCallback.getResult(BIKE_LOCK_NAME).size() >= DATA_POINTS;
    }

    private double calculateMedian(@NonNull List<Integer> rssiData) {
        int halfSize = rssiData.size() / 2;
        Collections.sort(rssiData);

        if (rssiData.size() % 2 == 0) return (rssiData.get(halfSize - 1) + rssiData.get(halfSize)) * 0.5;
        return rssiData.get(halfSize);
    }

    private double calculateMean(@NonNull List<Integer> rssiData) {
        double sum = 0;
        for (int i = 0; i < rssiData.size(); i++) sum += rssiData.get(i);

        double mean = sum/rssiData.size();
        return mean;
    }

    private double calculateStdev(@NonNull List<Integer> rssiData) {
        double mean = calculateMean(rssiData);
        double sum = 0;
        for (int i = 0; i < rssiData.size(); i++) sum += ((rssiData.get(i) - mean) * (rssiData.get(i) - mean));
        return Math.sqrt(sum/(rssiData.size()));
    }

    private List<Integer> getDistancesFromRssi(@NonNull List<Integer> rssiData) {
        List<Integer> distances = new ArrayList<>();

        for (int i = 0; i < rssiData.size(); i++) {
            int rssi = rssiData.get(i);
            int distance = rssiDistanceValues[rssi];

            distances.add(distance);
        }
        return distances;
    }


    // Calculating the correspondent distance for each rssi value
    // Equation: x = exp( (y-a)/b )
    // where x is distance in cm and y is the rssi value.
    // a is a constant and b is the coefficient.
    private void generateDistValues() {

        DatabaseReference database = FirebaseDatabase.getInstance()
                .getReference()
                .child(EXP_FUNCTION_COEFFICIENTS)
                .child(PHONE_MODEL);

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FunctionCoefficientsModel values = dataSnapshot.getValue(FunctionCoefficientsModel.class);

                if (values == null) return;

                double a = values.getValueA();
                double b = values.getValueB();
                double error = values.getError();

                for (int i = 0; i < rssiDistanceValues.length; i++) {
                    double distance = (Math.exp((i-a)/b)) * error;

                    if (distance > MAX_DISTANCE) distance = MAX_DISTANCE;
                    if (distance < 1) distance = 1;

                    rssiDistanceValues[i] = (int)distance;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void playSound(int soundName) {

        switch (soundName) {
            case SOUND_ERROR:
                final int soundId1 = mSoundPool.load(this, R.raw.error, 1);
                mSoundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> mSoundPool.play(soundId1, 1, 1, 1, 0, 1));
                break;

            case SOUND_START:
                final int soundId2 = mSoundPool.load(this, R.raw.start, 1);
                mSoundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> mSoundPool.play(soundId2, 1, 1, 1, 0, 1));
                break;

            case SOUND_SUCCESS:
                final int soundId3 = mSoundPool.load(this, R.raw.success, 1);
                mSoundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> mSoundPool.play(soundId3, 1, 1, 1, 0, 1));
                break;

            case SOUND_BIP:
                final int soundId4 = mSoundPool.load(this, R.raw.bip, 1);
                mSoundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> mSoundPool.play(soundId4, 1, 1, 1, 0, 1));
                break;

            default:
                break;
        }
    }


    private void cancelExperiment() {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(EXP_RUNNING)
                .setValue(DATABASE_CANCEL);

        stopScan();
    }

    private List<Integer> removeOutliers(@NonNull List<Integer> distData) {
        List<Integer> newDistData = new ArrayList<>();
        Collections.sort(distData);

        // Tukey's fences
        int q1 = distData.get(((distData.size()/4) * 1)-1);
        int q3 = distData.get(((distData.size()/4) * 3)-1);
        double k = 1.5; //  John Tukey proposed this test, where k=1.5 indicates an "outlier", and k=3 indicates data that is "far out"

        int from = (int) (q1 - k*(q3-q1));
        int to = (int) (q3 + k*(q3-q1));

        for (int i = 0; i < distData.size(); i++) {
            int value = distData.get(i);
            if (from <= value && value <= to) newDistData.add(value);
        }

        return newDistData;
    }

    private void sendAnalysisToServer(double median, int distance, String phoneModel) {
        RssiAnalysisModel model = new RssiAnalysisModel(distance, median);
        FirebaseDatabase.getInstance()
                .getReference()
                .child(EXP_RSSI_ANAL)
                .child(phoneModel)
                .push()
                .setValue(model);
    }

    private void sendEvaluationToServer(double distMean, int distance, double stdev, String phoneModel) {
        RssiEvaluationModel model = new RssiEvaluationModel(distance, distMean, stdev);
        FirebaseDatabase.getInstance()
                .getReference()
                .child(EXP_RSSI_EVAL)
                .child(phoneModel)
                .push()
                .setValue(model);
    }

    private void sendLocationToServer(String locationExpName, LocationModel model) {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(EXP_LOCATION)
                .child(locationExpName)
                .child(BIKE_LOCK_NAME)
                .push()
                .setValue(model);
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelExperiment();
    }

    // permission check
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermissions() {

        // Guide for permissions https://www.youtube.com/watch?v=C8lUdPVSzDk
        // Check if permissions already has been given
        boolean hasPermissions = true;
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                hasPermissions = false;
            }
        }

        // Check if permissions is granted
        if (!hasPermissions) {
            // Permissions have not been granted
            // Check if additional rationale needs to be provided
            boolean shouldShowRequest = true;
            for (String permission : PERMISSIONS) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    shouldShowRequest = false;
                }
            }

            // If additional rationale needs to be provided, show a dialog and explain why
            if (shouldShowRequest) {
                new AlertDialog.Builder(this)
                        .setTitle("Location")
                        .setMessage("You need to give location permissions")
                        .setPositiveButton("OK", (dialog, which) -> requestPermissions(PERMISSIONS, LOCATION_PERMISSION_CODE))
                        .setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss())
                        .create()
                        .show();
            } else requestPermissions(PERMISSIONS, LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                // User has deny permission and checked 'never show permission dialog' so you can redirect to Application settings page
                Snackbar.make(findViewById(android.R.id.content), "Please enable permission from settings", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", view -> {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, BLUETOOTH_ENABLE_CODE);
                        })
                        .show();
            }
        }
    }


    public class GpsLocationCallback extends LocationCallback {
        private Location mLocation;

        GpsLocationCallback() {
        }

        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult != null) mLocation = locationResult.getLastLocation();
        }

        Location getLocation () {
            return mLocation;
        }
    }



    // Creates a custom ScanCallback to provide a list of the specific data we want
    private class BleScanCallback extends ScanCallback {
        private Map<String, List<Integer>> results;

        BleScanCallback() {
            results = new ArrayMap<>();
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            addScanResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                addScanResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "BLE Scan Failed with code " + errorCode);
        }

        // Method to handle each ScanResult. This contains all the information about the specific device
        private void addScanResult(ScanResult result) {

            if (result.getScanRecord() != null) {
                String name = result.getScanRecord().getDeviceName();
                Integer rssi = Math.abs(result.getRssi()); // Setting the rssi to a positive value

                if (name != null) {

                    if (name.equals(BIKE_LOCK_NAME)) {
                        dpCounter++;
                        playSound(SOUND_BIP);
                        updateUi();
                    }

                    if (results.containsKey(name)) results.get(name).add(rssi);
                    else {
                        List<Integer> rssiData = new ArrayList<>();
                        rssiData.add(rssi);
                        results.put(name, rssiData);
                    }
                }
            }
        }

        Map<String, List<Integer>> getResults() {
            return results;
        }

        List<Integer> getResult(String bluetoothId) {
            if (results.get(bluetoothId) == null) return null;
            return results.get(bluetoothId);
        }
    }

    private class BleLocationCallback extends ScanCallback {
        private final String locationExpName;

        BleLocationCallback(String locationExpName) {
            this.locationExpName = locationExpName;
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            addScanResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) addScanResult(result);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "BLE Scan Failed with code " + errorCode);
        }

        // Method to handle each ScanResult. This contains all the information about the specific device
        private void addScanResult(ScanResult result) {

            if (result.getScanRecord() != null) {
                String name = result.getScanRecord().getDeviceName();
                Integer rssi = Math.abs(result.getRssi()); // Setting the rssi to a positive value

                if (name != null && name.equals(BIKE_LOCK_NAME)) {
                    dpCounter++;
                    playSound(SOUND_BIP);
                    updateUi();

                    Location location = mLocationCallback.getLocation();
                    if (location == null) return;

                    double distRssi = rssiDistanceValues[rssi];
                    double distProx = 20.0;
                    double distGps = location.getAccuracy();
                    double x = location.getLongitude();
                    double y = location.getLatitude();

                    LocationModel rssiModel = new LocationModel(ServerValue.TIMESTAMP, PHONE_MODEL, x, y, distGps, distRssi);
                    LocationModel proxModel = new LocationModel(ServerValue.TIMESTAMP, PHONE_MODEL, x, y, distGps, distProx);

                    sendLocationToServer("RSSI-" + locationExpName, rssiModel);
                    sendLocationToServer("PROX-" + locationExpName, proxModel);
                }
            }
        }
    }
}
