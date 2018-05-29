package com.charleshested.cfcontroller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;

import static com.charleshested.cfcontroller.MainActivity.DATABASE_CANCEL;
import static com.charleshested.cfcontroller.MainActivity.EXP_LOCATION;
import static com.charleshested.cfcontroller.MainActivity.EXP_RUNNING;

public class LocationActivity extends AppCompatActivity {
    private static final String TAG = "LocationActivity";

    private EditText mEditName;
    private Button mStart;
    private boolean started;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        mEditName = findViewById(R.id.inputName);
        mStart = findViewById(R.id.btn_start);

        mStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (started) stopExperiment();
                else startExperiment();
            }
        });
    }


    private void startExperiment() {
        if (mEditName.getText().length() == 0) {
            Toast.makeText(getApplication(), "Enter experiment name", Toast.LENGTH_SHORT).show();
            return;
        }

        String inputText = mEditName.getText().toString();
        String experiment = EXP_LOCATION + "," + "0" + "," + inputText + "," + "none";

        FirebaseDatabase.getInstance()
                .getReference()
                .child(EXP_RUNNING)
                .setValue(experiment);

        mStart.setText("Stop");
        started = true;
    }


    private void stopExperiment() {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(EXP_RUNNING)
                .setValue(DATABASE_CANCEL);

        mStart.setText("Start");
        started = false;
    }
}
