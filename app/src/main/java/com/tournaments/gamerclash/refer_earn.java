package com.tournaments.gamerclash;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class refer_earn extends AppCompatActivity {
    TextView uname;
    Button referBtn;
    ImageView back_btn;
    String username = "",link;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_refer_earn);
        back_btn = findViewById(R.id.back_button);
        back_btn.setOnClickListener(v -> finish());
        DatabaseReference linkRef = FirebaseDatabase.getInstance().getReference("AppDetails").child("AllLinks");
        linkRef.child("AppLink").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String linkValue = dataSnapshot.getValue(String.class);
                    link = linkValue;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });

        uname = findViewById(R.id.uname);
        referBtn = findViewById(R.id.referBtn);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        View rootView = findViewById(R.id.main);
        View toolbar = findViewById(R.id.toolbar);

        // Safe insets handling
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            if (toolbar != null) {
                toolbar.setPadding(
                        toolbar.getPaddingLeft(),
                        systemBars.top,
                        toolbar.getPaddingRight(),
                        toolbar.getPaddingBottom()
                );
            }
            getWindow().setNavigationBarColor(Color.BLACK);
            return WindowInsetsCompat.CONSUMED;
        });

        // 🔁 Firebase: get username
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(uid);

            userRef.child("username").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        username = snapshot.getValue(String.class);
                        uname.setText(username);  // Set username to TextView
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // handle error
                }
            });
        }

        // 🔗 Share Refer Code
        referBtn.setOnClickListener(v -> {
            String shareText = "Addicted to Games? Want to make some cash out of it ?? Try out GameRClash, esport platform. Join daily matches & Get Rewared on Each Kill you score. Get Huge Prize on Winnings. just Download GameRClash Android App & Register using the Promocode given Below to Get 5 bonus coins as newbie signup"
                    + "Use my referral code: " + username + " to get ₹5 bonus!\n\n" +
                    "Download now: " + link;

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Tusk Esport");
            intent.putExtra(Intent.EXTRA_TEXT, shareText);

            startActivity(Intent.createChooser(intent, "Share via"));
        });
    }
}
