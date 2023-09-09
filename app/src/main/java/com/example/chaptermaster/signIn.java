package com.example.chaptermaster;

import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
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
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent mainIntent = new Intent(signIn.this, MainActivity.class);
                            startActivity(mainIntent);
                            finish();
                        } else {
                            FirebaseAuthException e = (FirebaseAuthException) task.getException();
                            String errorMessage = e.getMessage();

                            if (errorMessage.contains("There is no user record corresponding to this identifier")) {
                                showCreateAccountDialog();
                            } else {
                                Toast.makeText(signIn.this, "Authentication failed: " + errorMessage,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            confirmPasswordToggle.setImageResource(R.drawable.ic_eye);
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            confirmPasswordToggle.setImageResource(R.drawable.ic_eye_off);
        }

        etPassword.setSelection(etPassword.getText().length());
    }

    private void showCreateAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You don't have an account yet. Do you want to create one?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent signUpIntent = new Intent(signIn.this, signUp.class);
                        startActivity(signUpIntent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

}
