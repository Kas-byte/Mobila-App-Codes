package com.example.fyp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    EditText email;
    EditText password;
    Button loginButton;
    private Button regisButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        regisButton = findViewById(R.id.regisButton);

        mAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredEmail = email.getText().toString();
                String enteredPassword = password.getText().toString();

                if (enteredEmail.isEmpty() || enteredPassword.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                loginUser(enteredEmail, enteredPassword);
            }
        });

        regisButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Register.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Intent intent = new Intent(MainActivity.this, Dashboard.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(this, e -> {
                    if (e instanceof FirebaseAuthException) {
                        Toast.makeText(MainActivity.this, "Authentication error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Login Failed due to an unknown error!", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
