package com.tournaments.gamerclash;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.HashMap;

public class profile extends AppCompatActivity {

    EditText fname, lname;
    TextView mobile, gmail,username;
    Button saveBtn;
    Toolbar toolbar;
    ImageView back_btn;
    FirebaseUser currentUser;
    DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        back_btn = findViewById(R.id.back_button);
        back_btn.setOnClickListener(v -> {
            onBackPressed();
        });

        // Toolbar setup
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            int statusBarHeight = (resourceId > 0) ? getResources().getDimensionPixelSize(resourceId) : 0;
            toolbar.setPadding(0, statusBarHeight, 0, 0);
            toolbar.setClipToPadding(false);
        }
        getSupportActionBar().hide();

        // Firebase setup
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        // Views
        fname = findViewById(R.id.fname);
        lname = findViewById(R.id.lname);
        username = findViewById(R.id.username);
        mobile = findViewById(R.id.mobile);
        gmail = findViewById(R.id.gmail);
        saveBtn = findViewById(R.id.saveBtn); // Make sure your button ID in XML is `saveBtn`

        // Fetch and show user data
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    fname.setText(snapshot.child("fname").getValue(String.class));
                    lname.setText(snapshot.child("lname").getValue(String.class));
                    username.setText(snapshot.child("username").getValue(String.class));
                    gmail.setText(snapshot.child("email").getValue(String.class));
                    mobile.setText(snapshot.child("phone").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(profile.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });

        // Save updated data
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileData();
            }
        });
    }

    private void saveProfileData() {
        String first = fname.getText().toString().trim();
        String last = lname.getText().toString().trim();
        String user = username.getText().toString().trim();

        if (first.isEmpty() || last.isEmpty() || user.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> updatedData = new HashMap<>();
        updatedData.put("fname", first);
        updatedData.put("lname", last);
        updatedData.put("username", user);

        userRef.updateChildren(updatedData).addOnSuccessListener(unused -> {
            Toast.makeText(profile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(profile.this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
