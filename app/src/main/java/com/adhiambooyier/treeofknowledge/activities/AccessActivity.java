package com.adhiambooyier.treeofknowledge.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.adhiambooyier.treeofknowledge.R;
import com.adhiambooyier.treeofknowledge.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccessActivity extends AppCompatActivity {
    AppCompatEditText txtFirstName;
    AppCompatEditText txtLastName;
    AppCompatEditText txtEmail;
    AppCompatEditText txtPassword;
    AppCompatEditText txtConfirmPassword;
    AppCompatButton btnSignUp;
    AppCompatButton btnSignIn;

    FirebaseAuth auth;
    FirebaseFirestore db;

    private final String TAG = getClass().getSimpleName();
    private boolean isSignIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access);
        txtFirstName = findViewById(R.id.txtFirstName);
        txtLastName = findViewById(R.id.txtLastName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        txtConfirmPassword = findViewById(R.id.txtConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignIn = findViewById(R.id.btnSignIn);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSignIn) {
                    String email = txtEmail.getText().toString().trim();
                    String password = txtPassword.getText().toString();

                    if (!email.isEmpty() && !password.isEmpty()) {
                        login(email, password);
                    } else {
                        Toast.makeText(AccessActivity.this, "you are missing a field", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String fName = txtFirstName.getText().toString().trim();
                    String lName = txtLastName.getText().toString().trim();
                    String email = txtEmail.getText().toString().trim();
                    String password = txtPassword.getText().toString();
                    String confirmPassword = txtConfirmPassword.getText().toString();

                    if (!email.isEmpty()
                            && !fName.isEmpty()
                            && !lName.isEmpty()
                            && !password.isEmpty()
                            && !confirmPassword.isEmpty()) {
                        register(new User(fName, lName, email), password);
                    } else {
                        Toast.makeText(AccessActivity.this, "you are missing a field", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleState();
            }
        });
    }
    @Override
    protected void onStart() {

        super.onStart();
        if(auth.getCurrentUser() != null){
            Intent i = new Intent(AccessActivity.this, MainActivity.class);
            startActivity(i);
            ActivityCompat.finishAffinity(AccessActivity.this);
        }
    }

    private void toggleState() {
        if (isSignIn) {
            txtFirstName.setVisibility(View.VISIBLE);
            txtLastName.setVisibility(View.VISIBLE);
            txtConfirmPassword.setVisibility(View.VISIBLE);
            btnSignUp.setText("Sign Up");
            btnSignIn.setText("Sign In");
            isSignIn = false;
        } else {
            txtFirstName.setVisibility(View.GONE);
            txtLastName.setVisibility(View.GONE);
            txtConfirmPassword.setVisibility(View.GONE);
            btnSignUp.setText("Sign In");
            btnSignIn.setText("Sign Up");
            isSignIn = true;
        }
    }

    private void login(String email, String password) {
        btnSignIn.setText("Signing in...");
        btnSignIn.setEnabled(false);
        btnSignUp.setEnabled(false);
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(AccessActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent i = new Intent(AccessActivity.this, MainActivity.class);
                            startActivity(i);
                            ActivityCompat.finishAffinity(AccessActivity.this);
                        } else {
                            Log.w(TAG, "Login failed", task.getException());
                            Toast.makeText(AccessActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            btnSignIn.setText("Sign In");
                            btnSignIn.setEnabled(true);
                            btnSignUp.setEnabled(true);
                        }
                    }
                });
    }

    private void register(final User user, String password) {
        btnSignUp.setText("Signing up...");
        btnSignUp.setEnabled(false);
        btnSignIn.setEnabled(false);
        auth.createUserWithEmailAndPassword(user.getEmail(), password)
                .addOnCompleteListener(AccessActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Registration successful");
                            final FirebaseUser fbUser = auth.getCurrentUser();
                            user.setId(fbUser.getUid());
                            db.collection("users")
                                    .document(user.getId())
                                    .set(user)
                                    .addOnCompleteListener(AccessActivity.this, new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "Registration Successful");
                                                Intent intent = new Intent(AccessActivity.this, MainActivity.class);
                                                startActivity(intent);

                                            } else {
                                                Log.w(TAG, "Registration Failed", task.getException());
                                                Toast.makeText(AccessActivity.this, "Error creating user", Toast.LENGTH_SHORT).show();
                                                fbUser.delete();
                                                btnSignUp.setText("Create Account");
                                                btnSignUp.setEnabled(true);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
