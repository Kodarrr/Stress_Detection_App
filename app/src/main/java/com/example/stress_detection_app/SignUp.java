package com.example.stress_detection_app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class SignUp extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private ImageButton eyeIconButton;
    private EditText passwordEditText;
    private ImageButton signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();


        signUpButton = findViewById(R.id.signup_button);
        eyeIconButton = findViewById(R.id.eye_icon);
        passwordEditText = findViewById(R.id.password);



        // Set the initial state (password hidden)
        passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());

        // Toggle password visibility when the eye icon is clicked
        eyeIconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if password is hidden or visible
                if (passwordEditText.getTransformationMethod() instanceof PasswordTransformationMethod) {
                    // Show password
                    passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    eyeIconButton.setImageResource(R.drawable.baseline_visibility_20);
                } else {
                    // Hide password
                    passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    eyeIconButton.setImageResource(R.drawable.baseline_visibility_off_24);
                }
            }
        });
        signUpButton.setOnClickListener(v -> {
            String email = ((EditText) findViewById(R.id.email)).getText().toString().trim();
            String password = ((EditText) findViewById(R.id.password)).getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                registerUser(email, password);
            } else {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            }
        });

        Button toLoginButton = findViewById(R.id.login_button);
        toLoginButton.setOnClickListener(v -> startActivity(new Intent(this, Login.class)));
    }

    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, Login.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

//    private void showDatePicker() {
//        Calendar calendar = Calendar.getInstance();
//        int year = calendar.get(Calendar.YEAR);
//        int month = calendar.get(Calendar.MONTH);
//        int day = calendar.get(Calendar.DAY_OF_MONTH);

//        DatePickerDialog datePickerDialog = new DatePickerDialog(
//                this,
//                (view, selectedYear, selectedMonth, selectedDay) -> {
//                    // Format and set the date
//                    String dob = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
//                    dateOfBirthField.setText(dob);
//                },
//                year, month, day
//        );

//        // Optional: Restrict future dates
//        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
//        datePickerDialog.show();
  //  }
}
