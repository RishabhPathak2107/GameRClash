package com.tournaments.gamerclash;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class contactus extends AppCompatActivity {

    Toolbar toolbar;
    ScrollView scrollView;
    TextView gmail, phone;
    ImageView twitter, youtube, instagram, fb,back_btn;
    View tele, whatsapp, discord, insta, facebook;

    DatabaseReference contactRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contactus);
        back_btn = findViewById(R.id.back_button);
        back_btn.setOnClickListener(v -> {
            onBackPressed();
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        toolbar = findViewById(R.id.toolbar);
        int statusBarHeight = getStatusBarHeight();
        toolbar.setPadding(0, statusBarHeight, 0, 0);
        toolbar.setClipToPadding(false);

        scrollView = findViewById(R.id.contactScroll);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            scrollView.setOnApplyWindowInsetsListener((v, insets) -> {
                int bottomInset = insets.getSystemWindowInsetBottom();
                scrollView.setPadding(
                        scrollView.getPaddingLeft(),
                        scrollView.getPaddingTop(),
                        scrollView.getPaddingRight(),
                        bottomInset
                );
                return insets;
            });
        }

        // Initialize Views
        gmail = findViewById(R.id.gmail);
        phone = findViewById(R.id.phone);
        tele = findViewById(R.id.tele);
        whatsapp = findViewById(R.id.whatsapp);
        discord = findViewById(R.id.discord);
        insta = findViewById(R.id.insta);
        facebook = findViewById(R.id.facebook);
        twitter = findViewById(R.id.twitter);
        youtube = findViewById(R.id.youtube);
        instagram = findViewById(R.id.instagram);
        fb = findViewById(R.id.fb);

        ImageView backBtn = findViewById(R.id.back_button);
        backBtn.setOnClickListener(v -> finish());

        // Firebase Reference
        contactRef = FirebaseDatabase.getInstance().getReference().child("AppDetails").child("Contact");

        // Fetch contact links from Firebase
        contactRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Gmail ke liye
                String gmailAddress = snapshot.child("gmail").getValue(String.class);
                if (gmailAddress != null && !gmailAddress.isEmpty()) {
                    gmail.setText(gmailAddress);  // 👈 Show in TextView
                    gmail.setOnClickListener(v -> {
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:" + gmailAddress));
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

                // Phone ke liye
                String phoneNumber = snapshot.child("phone").getValue(String.class);
                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    phone.setText(phoneNumber);  // 👈 Show in TextView
                    phone.setOnClickListener(v -> {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + phoneNumber));
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

                // Baaki sab clickable links
                setClickLink(tele, snapshot.child("tele").getValue(String.class));
                setClickLink(whatsapp, snapshot.child("whatsapp").getValue(String.class));
                setClickLink(discord, snapshot.child("discord").getValue(String.class));
                setClickLink(insta, snapshot.child("insta").getValue(String.class));
                setClickLink(facebook, snapshot.child("facebook").getValue(String.class));
                setClickLink(instagram, snapshot.child("insta").getValue(String.class));
                setClickLink(fb, snapshot.child("facebook").getValue(String.class));
                setClickLink(twitter, snapshot.child("twitter").getValue(String.class));
                setClickLink(youtube, snapshot.child("youtube").getValue(String.class));

        }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors if needed
            }
        });
    }

    private void setClickLink(View view, String url) {
        if (url != null && !url.isEmpty()) {
            view.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            });
        }
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
