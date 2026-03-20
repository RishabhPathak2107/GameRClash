package com.tournaments.tuskesport;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class signup extends AppCompatActivity {

    EditText email, password, username, phone, referCodeInput, firstName, lastName;
    TextView btnSignup, btnLogin;
    FirebaseAuth auth;
    DatabaseReference db;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (currentUser.isEmailVerified()) {
                startActivity(new Intent(signup.this, MainActivity.class));
                finish();
                return;
            } else {
                Toast.makeText(this, "Please verify your email to continue", Toast.LENGTH_LONG).show();
                FirebaseAuth.getInstance().signOut();
            }
        }

        setContentView(R.layout.activity_signup);
        getSupportActionBar().hide();

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        username = findViewById(R.id.username);
        phone = findViewById(R.id.mobile);
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        referCodeInput = findViewById(R.id.referCode);

        btnSignup = findViewById(R.id.btnSignUp);
        btnLogin = findViewById(R.id.btnLogin);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_progress_layout);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), login.class));
        });

        btnSignup.setOnClickListener(v -> registerUser());

        TextView tvShowPassword = findViewById(R.id.tvShowPassword);

        final boolean[] isPasswordVisible = {false};

        tvShowPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible[0]) {
                    // Hide password
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    tvShowPassword.setText("SHOW");
                } else {
                    // Show password
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    tvShowPassword.setText("HIDE");
                }

                // Move cursor to end
                password.setSelection(password.getText().length());

                // Toggle state
                isPasswordVisible[0] = !isPasswordVisible[0];
            }
        });
    }

    private void registerUser() {
        String fname = firstName.getText().toString().trim();
        String lname = lastName.getText().toString().trim();
        String mail = email.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String uname = username.getText().toString().trim().toUpperCase();
        String phoneNumber = phone.getText().toString().trim();
        String referCode = referCodeInput.getText().toString().trim();

        if (TextUtils.isEmpty(fname) || fname.length() < 3) {
            firstName.setError("Enter valid first name");
            return;
        }

        if (TextUtils.isEmpty(lname) || lname.length() < 3) {
            lastName.setError("Enter valid last name");
            return;
        }

        if (TextUtils.isEmpty(mail) || !Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            email.setError("Enter valid email");
            return;
        }

        if (TextUtils.isEmpty(pass) || pass.length() < 6) {
            password.setError("Password must be 6+ characters");
            return;
        }

        if (TextUtils.isEmpty(uname)) {
            username.setError("Enter username");
            return;
        }

        if (TextUtils.isEmpty(phoneNumber) || phoneNumber.length() != 10) {
            phone.setError("Enter valid 10-digit phone number");
            return;
        }

        dialog.show();

        db.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isDuplicate = false;
                for (DataSnapshot child : snapshot.getChildren()) {
                    String existingUsername = child.child("username").getValue(String.class);
                    String existingEmail = child.child("email").getValue(String.class);
                    String existingPhone = child.child("phone").getValue(String.class);

                    if (uname.equalsIgnoreCase(existingUsername)) {
                        username.setError("Username already exists");
                        isDuplicate = true;
                        break;
                    }
                    if (mail.equalsIgnoreCase(existingEmail)) {
                        email.setError("Email already used");
                        isDuplicate = true;
                        break;
                    }
                    if (phoneNumber.equals(existingPhone)) {
                        phone.setError("Phone already registered");
                        isDuplicate = true;
                        break;
                    }
                }

                if (isDuplicate) {
                    dialog.dismiss();
                } else {
                    // Agar refer code diya gaya hai, toh uska validation check karo
                    if (!TextUtils.isEmpty(referCode)) {
                        db.child("refer").child(referCode).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot referSnapshot) {
                                if (referSnapshot.exists()) {
                                    // Valid refer code mila, bonus 5 set karo
                                    createAccount(fname, lname, mail, pass, uname, phoneNumber, referCode, 5);
                                } else {
                                    dialog.dismiss();
                                    referCodeInput.setError("Invalid refer code");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                dialog.dismiss();
                                Toast.makeText(signup.this, "Refer check failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Refer code nahi diya gaya
                        createAccount(fname, lname, mail, pass, uname, phoneNumber, "", 0);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
                Toast.makeText(signup.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createAccount(String fname, String lname, String email, String password, String username, String phone, String referBy, int bonus) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    String uid = user.getUid();
                    HashMap<String, Object> userMap = new HashMap<>();
                    userMap.put("fname", fname);
                    userMap.put("lname", lname);
                    userMap.put("email", email);
                    userMap.put("phone", phone);
                    userMap.put("username", username);
                    userMap.put("bonus", bonus);
                    userMap.put("deposit", 0);
                    userMap.put("winnings", 0);
                    userMap.put("matches_played", 0);
                    userMap.put("kills", 0);
                    userMap.put("total_coins", 0);
                    userMap.put("referby", referBy);
                    db.child("users").child(uid).setValue(userMap).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            db.child("refer").child(username).setValue(uid).addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful()) {

                                    // 👉 ADD WALLET TRANSACTION FOR BONUS
                                    if (bonus > 0) {
                                        long timestamp = System.currentTimeMillis();

                                        // Format the date as "dd-MM-yyyy"
                                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy");
                                        String date = sdf.format(new java.util.Date(timestamp));

                                        // Generate a simple transaction ID
                                        String txnId = generateTxnId();

                                        HashMap<String, Object> txn = new HashMap<>();
                                        txn.put("amount", String.valueOf(bonus));
                                        txn.put("date", date);
                                        txn.put("id", txnId);
                                        txn.put("reason", "Sign Up Bonus");
                                        txn.put("status", "success");

                                        db.child("transactions").child(uid).child(String.valueOf(timestamp)).setValue(txn);
                                    }


                                    user.sendEmailVerification();
                                    dialog.dismiss();
                                    Toast.makeText(signup.this, "Signup successful. Verify your email.", Toast.LENGTH_LONG).show();

                                    Intent intent = new Intent(signup.this, login.class);
                                    intent.putExtra("referCode", referBy);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    dialog.dismiss();
                                    Toast.makeText(signup.this, "Failed to save refer data: " + task2.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            dialog.dismiss();
                            Toast.makeText(signup.this, "Failed to save user data: " + task1.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } else {
                dialog.dismiss();
                Toast.makeText(signup.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String generateTxnId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder txnId = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 6; i++) {
            txnId.append(chars.charAt(random.nextInt(chars.length())));
        }
        return txnId.toString();
    }

}
