package com.example.chaptermaster;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class signUp extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp;
    private ImageView passwordToggle, confirmPasswordToggle;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        passwordToggle = findViewById(R.id.passwordToggle);
        confirmPasswordToggle = findViewById(R.id.confirmPasswordToggle);

        passwordToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });

        confirmPasswordToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleConfirmPasswordVisibility();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = etName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                String emailPattern = "^[A-Za-z0-9+_.-]+@(.+)$";

                if (name.isEmpty()) {
                    Toast.makeText(signUp.this, "Name is required.", Toast.LENGTH_SHORT).show();
                } else if (!isAlpha(name)) {
                    Toast.makeText(signUp.this, "Name can only contain alphabetic characters.", Toast.LENGTH_SHORT).show();
                } else if (email.isEmpty()) {
                    Toast.makeText(signUp.this, "Please Enter Email", Toast.LENGTH_SHORT).show();
                } else if (!email.matches(emailPattern)) {
                    Toast.makeText(signUp.this, "Invalid email format.", Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(signUp.this, "Please Enter Password", Toast.LENGTH_SHORT).show();
                } else if (password.length() < 8) {
                    Toast.makeText(signUp.this, "Password must be 8 or more characters.", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(signUp.this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(signUp.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(signUp.this, "Sign-up successful!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(signUp.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        if (task.getException() instanceof FirebaseAuthException) {
                                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                        }
                                        Toast.makeText(signUp.this, "Sign-up failed. Please try again.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            isPasswordVisible = false;
            passwordToggle.setImageResource(R.drawable.ic_eye_off);
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            isPasswordVisible = true;
            passwordToggle.setImageResource(R.drawable.ic_eye);
        }

        etPassword.setSelection(etPassword.getText().length());
    }

    private void toggleConfirmPasswordVisibility() {
        if (isPasswordVisible) {
            etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            isPasswordVisible = false;
            confirmPasswordToggle.setImageResource(R.drawable.ic_eye_off);
        } else {
            etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            isPasswordVisible = true;
            confirmPasswordToggle.setImageResource(R.drawable.ic_eye);
        }

        etPassword.setSelection(etPassword.getText().length());
    }

    private boolean isAlpha(String s) {
        return s != null && s.matches("^[a-zA-Z.\\s]*$");
    }
}
