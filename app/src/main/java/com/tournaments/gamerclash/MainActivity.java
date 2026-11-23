package com.tournaments.gamerclash;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;
import com.tournaments.gamerclash.Models.UserModel;
import com.tournaments.gamerclash.databinding.ActivityMainBinding;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;
    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    TextView totalBalance;
    FirebaseAuth auth;
    ImageView img;
    double total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();

        askNotificationPermission();

        img = findViewById(R.id.notification_img);
        img.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, notification.class);
            startActivity(intent);
        });

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications
        ).build();

        NavController navController = Navigation.findNavController(this, R.id.layoutHolder);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        View toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setPadding(
                    toolbar.getPaddingLeft(),
                    getStatusBarHeight(),
                    toolbar.getPaddingRight(),
                    toolbar.getPaddingBottom()
            );
        }

        getWindow().setNavigationBarColor(Color.BLACK);

        totalBalance = findViewById(R.id.totalBalance);

        // 🔥 USER DATA + DISABLED CHECK
        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(auth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        // 🚫 Disabled Check
                        Boolean disabled = snapshot.child("disabled").getValue(Boolean.class);
                        if (disabled != null && disabled) {
                            showSuspendedDialog();
                            return;
                        }

                        UserModel userModel = snapshot.getValue(UserModel.class);
                        if (userModel != null) {
                            total = userModel.getBonus()
                                    + userModel.getDeposit()
                                    + userModel.getWinnings();

                            totalBalance.setText(
                                    String.format(Locale.getDefault(), "%.0f", total)
                            );
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        totalBalance.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, wallet.class);
            startActivity(intent);
        });

        OneSignal.setNotificationOpenedHandler(result -> {
            String goToUrl = result.getNotification()
                    .getAdditionalData()
                    .optString("goTo", "");

            if (!goToUrl.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(goToUrl));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    // 🚨 NON-CANCELABLE SUSPENDED POPUP
    private void showSuspendedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Account Suspended");
        builder.setMessage("Your ID has been suspended.\nPlease contact support.");

        builder.setCancelable(false); // ❌ Cancel nahi hoga

        builder.setPositiveButton("OK", (dialog, which) -> {
            FirebaseAuth.getInstance().signOut();
            finishAffinity(); // 🔥 App band
        });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // ❌ Outside touch se bhi nahi
        dialog.show();
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted.");
            } else {
                Log.d(TAG, "Notification permission denied.");
            }
        }
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources()
                .getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
