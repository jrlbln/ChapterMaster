package com.example.chaptermaster;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class signIn extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnSignIn;
    private TextView tvSignUp;
    private LinearLayout linearLayout;
    private ImageView confirmPasswordToggle;
    private boolean isPasswordVisible = false;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        confirmPasswordToggle = findViewById(R.id.confirmPasswordToggle);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvSignUp = findViewById(R.id.tvSignUp);
        linearLayout = findViewById(R.id.linearLayout);


        mAuth = FirebaseAuth.getInstance();

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInUser(etEmail.getText().toString(), etPassword.getText().toString());
            }
        });

        confirmPasswordToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to the sign-up activity when "Sign Up" text is clicked
                Intent signUpIntent = new Intent(signIn.this, signUp.class);
                startActivity(signUpIntent);
            }
        });
    }

    private void signInUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign-in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            // Navigate to MainActivity when sign-in is successful
                            Intent mainIntent = new Intent(signIn.this, MainActivity.class);
                            startActivity(mainIntent);
                            finish(); // Close the sign-in activity
                        } else {
                            // If sign-in fails, display a message to the user.
                            FirebaseAuthException e = (FirebaseAuthException) task.getException();
                            Toast.makeText(signIn.this, "Authentication failed: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible; // Toggle the password visibility flag

        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            confirmPasswordToggle.setImageResource(R.drawable.ic_eye); // Use the eye-off icon when password is visible
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            confirmPasswordToggle.setImageResource(R.drawable.ic_eye_off); // Use the eye icon when password is hidden
        }

        etPassword.setSelection(etPassword.getText().length()); // Keep the cursor at the end of the password field
    }
}
