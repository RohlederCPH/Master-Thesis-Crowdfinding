package com.charleshested.cfcontroller;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static com.charleshested.cfcontroller.MainActivity.DATABASE_CANCEL;
import static com.charleshested.cfcontroller.MainActivity.EXP_CONT;
import static com.charleshested.cfcontroller.MainActivity.EXP_RSSI_ANAL;
import static com.charleshested.cfcontroller.MainActivity.EXP_RUNNING;
import static com.charleshested.cfcontroller.MainActivity.PHONE_NAME_SAMSUNG_6;
import static com.charleshested.cfcontroller.MainActivity.PHONE_NAME_SAMSUNG_8;

public class RssiAnalysisActivity extends AppCompatActivity {
    private static final String TAG = "RssiAnalysisActivity";

    private boolean started;
    private int localDist;
    private int expCount;

    private Button startBtn;
    private EditText inputDist;
    private TextView textViewDist;
    private TextView textViewS6;
    private TextView textViewS8;

    private DatabaseReference databaseReference;
    private Query queryS6;
    private Query queryS8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rssi_anal);

        startBtn = findViewById(R.id.btn_start);
        inputDist = findViewById(R.id.input_dist);
        textViewDist = findViewById(R.id.distTextView);
        textViewS6 = findViewById(R.id.textView_s6);
        textViewS8 = findViewById(R.id.textView_s8);

        databaseReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(EXP_RUNNING);

        queryS6 = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(EXP_RSSI_ANAL)
                .child(PHONE_NAME_SAMSUNG_6)
                .orderByKey()
                .limitToLast(1);

        queryS8 = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(EXP_RSSI_ANAL)
                .child(PHONE_NAME_SAMSUNG_8)
                .orderByKey()
                .limitToLast(1);

        startBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startStop();
            }
        });

        queryS6.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                RssiAnalysisModel model = dataSnapshot.getValue(RssiAnalysisModel.class);
                if (model == null) return;
                int distance = model.getDistance();
                double median = model.getRssiMedian();

                if (distance == localDist
                        && median != 0) {

                    textViewS6.setText(String.valueOf(median));
                    expCount++;

                    if (expCount == EXP_CONT) {
                        expCount = 0;
                        startStop();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        queryS8.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                RssiAnalysisModel model = dataSnapshot.getValue(RssiAnalysisModel.class);
                if (model == null) return;
                int distance = model.getDistance();
                double median = model.getRssiMedian();

                if (distance == localDist
                        && median != 0) {

                    textViewS8.setText(String.valueOf(median));
                    expCount++;

                    if (expCount == EXP_CONT) {
                        expCount = 0;
                        startStop();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void startStop() {
        if (!started) {

            if (inputDist.getText().length() == 0) {
                Toast.makeText(this, "Set Distance", Toast.LENGTH_SHORT).show();
                return;
            }

            localDist = Integer.parseInt(inputDist.getText().toString());
            textViewDist.setText(String.valueOf(localDist));
            textViewS8.setText("N/A");
            textViewS6.setText("N/A");
            databaseReference.setValue(EXP_RSSI_ANAL + "," + localDist + ",none,none");
            started = true;
            startBtn.setText("Stop");
            startBtn.setBackgroundColor(0xff00ff00);
            getWindow().getDecorView().setBackgroundColor(Color.WHITE);

        } else {
            localDist = 0;
            databaseReference.setValue(DATABASE_CANCEL);
            started = false;
            startBtn.setText("Start");
            startBtn.setBackgroundColor(0xffff0000);
            getWindow().getDecorView().setBackgroundColor(Color.GREEN);
        }
    }
}
