package com.example.stress_detection_app;

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

import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ImageButton loginButton,eyeIconButton;
    private EditText passwordEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginButton = findViewById(R.id.login_button);
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
                    eyeIconButton.setImageResource(R.drawable.baseline_visibility_off_24);
                } else {
                    // Hide password
                    passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    eyeIconButton.setImageResource(R.drawable.baseline_visibility_20);
                }
            }
        });




        mAuth = FirebaseAuth.getInstance();


        loginButton.setOnClickListener(v -> {
            String email = ((EditText) findViewById(R.id.email)).getText().toString().trim();
            String password = ((EditText) findViewById(R.id.password)).getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                loginUser(email, password);
            } else {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            }


        });
        // Redirect to SignUp activity
        Button signupButton = findViewById(R.id.signup_button);
        signupButton.setOnClickListener(v -> startActivity(new Intent(Login.this, SignUp.class)));

    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class)); // Redirect to MainActivity
                        finish();
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
