package com.charleshested.cfcontroller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;

import static com.charleshested.cfcontroller.MainActivity.EXP_FUNCTION_COEFFICIENTS;
import static com.charleshested.cfcontroller.MainActivity.PHONE_NAME_SAMSUNG_6;
import static com.charleshested.cfcontroller.MainActivity.PHONE_NAME_SAMSUNG_8;

public class FunctionCoefficientsActivity extends AppCompatActivity {
    private static final String TAG = "FunctionCoefficientsActivity";

    private EditText editValueA;
    private EditText editValueB;
    private EditText editError;
    private Button btnPhoneA;
    private Button btnPhoneB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_function_coefficients);

        editValueA = findViewById(R.id.editValueA);
        editValueB = findViewById(R.id.editValueB);
        editError = findViewById(R.id.editError);
        btnPhoneA = findViewById(R.id.btn_phone_a);
        btnPhoneB = findViewById(R.id.btn_phone_b);

        btnPhoneA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDataToServer(PHONE_NAME_SAMSUNG_6);
            }
        });

        btnPhoneB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDataToServer(PHONE_NAME_SAMSUNG_8);
            }
        });
    }

    private void sendDataToServer(String phone) {
        // Checks that the input fields has values and are not empty
        if (editValueA.getText().length() == 0
                || editValueB.getText().length() == 0
                || editError.getText().length() == 0) {

            Toast.makeText(this, "Enter values", Toast.LENGTH_SHORT).show();
            return;
        }

        double a = Double.parseDouble(editValueA.getText().toString());
        double b = Double.parseDouble(editValueB.getText().toString());
        double error = Double.parseDouble(editError.getText().toString());

        editValueA.getText().clear();
        editValueB.getText().clear();
        editError.getText().clear();

        FunctionCoefficientsModel model = new FunctionCoefficientsModel(a, b, error);
        FirebaseDatabase.getInstance()
                .getReference()
                .child(EXP_FUNCTION_COEFFICIENTS)
                .child(phone)
                .setValue(model);
    }
}
