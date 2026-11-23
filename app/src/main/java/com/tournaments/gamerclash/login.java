package com.tournaments.gamerclash;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;
import com.onesignal.OneSignal;
import com.onesignal.OSDeviceState;

import java.text.SimpleDateFormat;
import java.util.*;

public class login extends AppCompatActivity {

    TextView btnLogin, btnSignUp,tvForgotPassword;
    EditText email, password;
    FirebaseAuth auth;
    Dialog dialog;
    DatabaseReference db;
    ImageView btnGoogle;

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(login.this, MainActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignup);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btnGoogle = findViewById(R.id.btnGoogle);

        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ForgotPassActivity.class));
            }
        });

        TextView tvShowPassword = findViewById(R.id.tvShowPassword);
        final boolean[] isPasswordVisible = {false};
        tvShowPassword.setOnClickListener(v -> {
            if (isPasswordVisible[0]) {
                password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                tvShowPassword.setText("SHOW");
            } else {
                password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                tvShowPassword.setText("HIDE");
            }
            password.setSelection(password.getText().length());
            isPasswordVisible[0] = !isPasswordVisible[0];
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_progress_layout);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        btnSignUp.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), signup.class))
        );

        // Google Sign-In Config
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Add to strings.xml
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogle.setOnClickListener(v -> {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if (account != null) {
                // Already signed in
                firebaseAuthWithGoogle(account);
            } else {
                // Start Google sign-in
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        btnLogin.setOnClickListener(v -> {
            String input = email.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (TextUtils.isEmpty(input)) {
                Toast.makeText(this, "Enter Email / Phone / Username", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(pass)) {
                Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.show();

            if (input.contains("@")) {
                loginWithEmail(input, pass);
            } else {
                db.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean found = false;
                        String matchedEmail = null;

                        for (DataSnapshot userSnap : snapshot.getChildren()) {
                            String emailDb = userSnap.child("email").getValue(String.class);
                            String phoneDb = userSnap.child("phone").getValue(String.class);
                            String usernameDb = userSnap.child("username").getValue(String.class);

                            if (input.equalsIgnoreCase(emailDb) || input.equalsIgnoreCase(phoneDb) || input.equalsIgnoreCase(usernameDb)) {
                                matchedEmail = emailDb;
                                found = true;
                                break;
                            }
                        }

                        if (found && matchedEmail != null) {
                            loginWithEmail(matchedEmail, pass);
                        } else {
                            dialog.dismiss();
                            Toast.makeText(login.this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        dialog.dismiss();
                        Toast.makeText(login.this, "Database error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void loginWithEmail(String matchedEmail, String password) {
        auth.signInWithEmailAndPassword(matchedEmail, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            fetchUserDataAndProceed();
                        }
                    } else {
                        dialog.dismiss();
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        dialog.show();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        fetchUserDataAndProceed();
                    } else {
                        dialog.dismiss();
                        Toast.makeText(login.this, "Firebase Auth Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUserDataAndProceed() {
        if (auth.getCurrentUser() == null) {
            dialog.dismiss();
            return;
        }
        String uid = auth.getCurrentUser().getUid();
        waitForOneSignalIdAndProceed(uid, 0);
    }

    private void waitForOneSignalIdAndProceed(String uid, int retryCount) {
        db.child("users").child(uid).child("Id").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && !TextUtils.isEmpty(snapshot.getValue(String.class))) {
                    goToMainScreen(uid);
                } else {
                    OSDeviceState deviceState = OneSignal.getDeviceState();
                    String oneSignalId = deviceState != null ? deviceState.getUserId() : null;

                    if (!TextUtils.isEmpty(oneSignalId)) {
                        db.child("users").child(uid).child("Id").setValue(oneSignalId);
                        goToMainScreen(uid);
                    } else if (retryCount < 10) {
                        new Handler().postDelayed(() -> waitForOneSignalIdAndProceed(uid, retryCount + 1), 1000);
                    } else {
                        dialog.dismiss();
                        auth.signOut();
                        Toast.makeText(login.this, "Login failed: OneSignal ID not found", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            private void goToMainScreen(String uid) {
                db.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        dialog.dismiss();
                        if (snapshot.exists()) {
                            long bonus = snapshot.child("bonus").getValue(Long.class) != null ? snapshot.child("bonus").getValue(Long.class) : 0;
                            long deposit = snapshot.child("deposit").getValue(Long.class) != null ? snapshot.child("deposit").getValue(Long.class) : 0;
                            long winnings = snapshot.child("winnings").getValue(Long.class) != null ? snapshot.child("winnings").getValue(Long.class) : 0;
                            String username = snapshot.child("username").getValue(String.class);
                            String referBy = snapshot.child("referBy").getValue(String.class);

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra("bonus", bonus);
                            intent.putExtra("deposit", deposit);
                            intent.putExtra("winnings", winnings);
                            intent.putExtra("username", username);
                            intent.putExtra("referBy", referBy);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(login.this, "User info not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        dialog.dismiss();
                        Toast.makeText(login.this, "Error loading data", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
                Toast.makeText(login.this, "Error checking OneSignal ID", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
