package com.tournaments.tuskesport;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.BulletSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.tournaments.tuskesport.Models.txnModel;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class details extends AppCompatActivity {

    private TextView title, type, map, entryfee, date, totalReward, perkill, totalPrize, roomTxt,roomId,roomPass;
    private Button joinButton;
    private ImageView matchImage;
    private LinearLayout idlayout, passlayout;
    private double depositedCash = 0.0;
    private double winningsCash = 0.0;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef,matchRef;
    private Dialog progressDialog;
    private String matchId, matchCategory,customer;
    TextView account_name,single_IGN,id_pass,map_requirement,level,device,tools,team_up,guns,recording,result,refund ,registration,rights;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        title = findViewById(R.id.title);
        type = findViewById(R.id.type);
        map = findViewById(R.id.map);
        entryfee = findViewById(R.id.entryfee);
        date = findViewById(R.id.date);
        totalReward = findViewById(R.id.totalReward);
        perkill = findViewById(R.id.perkill);
        totalPrize = findViewById(R.id.totalPrize);
        joinButton = findViewById(R.id.joinButton);
        idlayout = findViewById(R.id.idlayout);
        passlayout = findViewById(R.id.passlayout);
        roomTxt = findViewById(R.id.roomTxt);
        matchImage = findViewById(R.id.matchImage);
        roomId = findViewById(R.id.roomId);
        roomPass = findViewById(R.id.roomPass);

        joinButton.setEnabled(false);
        roomTxt.setVisibility(GONE);
        idlayout.setVisibility(GONE);
        passlayout.setVisibility(GONE);

        progressDialog = new Dialog(this);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.custom_progress_layout);
        progressDialog.setCancelable(false);

        TextView roomId = findViewById(R.id.roomId);

        roomId.setOnClickListener(v -> {
            String textToCopy = roomId.getText().toString();

            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Room ID", textToCopy);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(this, "Room ID copied to clipboard", Toast.LENGTH_SHORT).show();
        });



        title.setText(getIntent().getStringExtra("title"));
        type.setText(getIntent().getStringExtra("type"));
        map.setText(getIntent().getStringExtra("map"));
        entryfee.setText(getIntent().getStringExtra("entryfee"));
        date.setText(getIntent().getStringExtra("date"));
        totalReward.setText(getIntent().getStringExtra("prizepool"));
        perkill.setText(getIntent().getStringExtra("perkill"));
        totalPrize.setText(getIntent().getStringExtra("totalPrize"));

        matchId = getIntent().getStringExtra("matchId");
        matchCategory = getIntent().getStringExtra("matchCategory");

        if (matchId == null || matchId.isEmpty()) {
            Toast.makeText(this, "Match ID missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        matchRef = FirebaseDatabase.getInstance().getReference("All_Matches").child(matchId);
        matchRef.child("roomId").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        roomId.setText(snapshot.getValue(String.class));
                    }
                }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        matchRef.child("roomPass").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    roomPass.setText(snapshot.getValue(String.class));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // ***** adding rules *****
        TextView account_name = findViewById(R.id.account_name);
        SpannableString bullet1 = new SpannableString("Enter account name: Provide your FF Max account name during registration. It cannot be changed once submitted.");
        bullet1.setSpan(new BulletSpan(30), 0, bullet1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        bullet1.setSpan(new StyleSpan(Typeface.BOLD), 0, "Enter account name".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        account_name.setText(bullet1);

        TextView single_IGN = findViewById(R.id.single_IGN);
        SpannableString bullet2 = new SpannableString("Use your in-game name: Make sure to enter your in-game name instead of UID.");
        bullet2.setSpan(new BulletSpan(30), 0, bullet2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        bullet2.setSpan(new StyleSpan(Typeface.BOLD), 0, "Use your in-game name".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        single_IGN.setText(bullet2);

        TextView id_pass = findViewById(R.id.id_pass);
        SpannableString bullet3 = new SpannableString("Match Room ID & Pass: Room details will be shared 5-10 minutes before the scheduled match time.");
        bullet3.setSpan(new BulletSpan(30), 0, bullet3.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        bullet3.setSpan(new StyleSpan(Typeface.BOLD), 0, "Match Room ID & Pass".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        id_pass.setText(bullet3);

        TextView map_requirement = findViewById(R.id.map_requirement);
        SpannableString bullet4 = new SpannableString("Map Requirements: Download all necessary maps before entering the custom rooms.");
        bullet4.setSpan(new BulletSpan(30), 0, bullet4.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        bullet4.setSpan(new StyleSpan(Typeface.BOLD), 0, "Map Requirements".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        map_requirement.setText(bullet4);

        TextView level = findViewById(R.id.level);
        SpannableString bullet5 = new SpannableString("Minimum Rank & Level: Only players with level 40+ and rank Platinum+ are eligible to join.");
        bullet5.setSpan(new BulletSpan(30), 0, bullet5.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        bullet5.setSpan(new StyleSpan(Typeface.BOLD), 0, "Minimum Rank & Level".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        level.setText(bullet5);

        TextView device = findViewById(R.id.device);
        SpannableString bullet6 = new SpannableString("Device Requirement: The game must be played on a mobile or tablet. Emulators are not allowed.");
        bullet6.setSpan(new BulletSpan(30), 0, bullet6.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        bullet6.setSpan(new StyleSpan(Typeface.BOLD), 0, "Device Requirement".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        device.setText(bullet6);

        TextView tools = findViewById(R.id.tools);
        SpannableString bullet7 = new SpannableString("Unauthorized Tools: Use of aimbots, no-recoil apps or any unfair software is strictly prohibited.");
        bullet7.setSpan(new BulletSpan(30), 0, bullet7.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        bullet7.setSpan(new StyleSpan(Typeface.BOLD), 0, "Unauthorized Tools".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tools.setText(bullet7);

        TextView team_up = findViewById(R.id.team_up);
        SpannableString bullet8 = new SpannableString("Teaming Up with Opponents: Collaborating with enemies for unfair advantage is not allowed.");
        bullet8.setSpan(new BulletSpan(30), 0, bullet8.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        bullet8.setSpan(new StyleSpan(Typeface.BOLD), 0, "Teaming Up with Opponents".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        team_up.setText(bullet8);

        TextView guns = findViewById(R.id.guns);
        SpannableString bullet9 = new SpannableString("Prohibited Guns: Double Vector and Sniper guns are not allowed during the match.");
        bullet9.setSpan(new BulletSpan(30), 0, bullet9.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        bullet9.setSpan(new StyleSpan(Typeface.BOLD), 0, "Prohibited Guns".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        guns.setText(bullet9);

        TextView recording = findViewById(R.id.recording);
        SpannableString bullet10 = new SpannableString("Mandatory Gameplay Recording: You must record both the match and your room entry using in-game or screen recorder.");
        bullet10.setSpan(new BulletSpan(30), 0, bullet10.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        bullet10.setSpan(new StyleSpan(Typeface.BOLD), 0, "Mandatory Gameplay Recording".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        recording.setText(bullet10);

        TextView result = findViewById(R.id.result);
        SpannableString bullet11 = new SpannableString("Match Result: Results will be declared within 1-2 hours after match ends.");
        bullet11.setSpan(new BulletSpan(30), 0, bullet11.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        bullet11.setSpan(new StyleSpan(Typeface.BOLD), 0, "Match Result".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        result.setText(bullet11);

        TextView refund = findViewById(R.id.refund);
        SpannableString bullet12 = new SpannableString("Refund Policy: No refund for missed matches. Refund only if room didn't meet minimum player requirement.");
        bullet12.setSpan(new BulletSpan(30), 0, bullet12.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        bullet12.setSpan(new StyleSpan(Typeface.BOLD), 0, "Refund Policy".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        refund.setText(bullet12);

        TextView registration = findViewById(R.id.registration);
        SpannableString bullet13 = new SpannableString("Match Registration Restriction: Once joined, registration cannot be cancelled.");
        bullet13.setSpan(new BulletSpan(30), 0, bullet13.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        bullet13.setSpan(new StyleSpan(Typeface.BOLD), 0, "Match Registration Restriction".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        registration.setText(bullet13);

// Report with bullet and bold heading
        TextView report = findViewById(R.id.report);
        FirebaseDatabase.getInstance().getReference("AppDetails").child("Contact").child("insta")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String contact = snapshot.getValue(String.class);
                        String html = "<b>• Report Issues:</b> Any issues or disputes must be reported to <a href='" + contact + "'>Customer Support</a> within 2 hours of the scheduled match.";
                        report.setText(Html.fromHtml(html));
                        report.setMovementMethod(LinkMovementMethod.getInstance());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

// Rights with bullet and bold heading
        TextView rights = findViewById(R.id.rights);
        String appName = getString(R.string.app_name);
        FirebaseDatabase.getInstance().getReference("AppDetails").child("AllLinks").child("AppLink")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String appLink = snapshot.getValue(String.class);
                        String html = "<b>• " + appName + " Rights:</b> <a href='" + appLink + "'>" + appName + "</a> reserves the right to modify match rules, prizes, and policies at its discretion.";
                        rights.setText(Html.fromHtml(html));
                        rights.setMovementMethod(LinkMovementMethod.getInstance());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        String userId = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.child("joinedMatchIds").child(matchId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            joinButton.setText("Joined");
                            joinButton.setEnabled(false);
                            roomTxt.setVisibility(VISIBLE);
                            idlayout.setVisibility(VISIBLE);
                            passlayout.setVisibility(VISIBLE);
                        } else {
                            roomTxt.setVisibility(GONE);
                            idlayout.setVisibility(GONE);
                            passlayout.setVisibility(GONE);
                            loadUserBalance();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(details.this, "Failed to check join status", Toast.LENGTH_SHORT).show();
                    }
                });

        joinButton.setOnClickListener(v -> {
            if (joinButton.isEnabled()) {
                joinButton.setEnabled(false);
                joinMatch();
            }
        });

        String imageName = getIntent().getStringExtra("image_name");
        if (imageName != null && !imageName.trim().isEmpty()) {
            try {
                Bitmap bm = getBitmapFromAsset("images/" + imageName.trim());
                if (bm != null) {
                    matchImage.setImageBitmap(bm);
                } else {
                    matchImage.setImageResource(R.drawable.logo);
                }
            } catch (IOException e) {
                e.printStackTrace();
                matchImage.setImageResource(R.drawable.logo);
            }
        } else {
            matchImage.setImageResource(R.drawable.logo);
        }

    }

    private Bitmap getBitmapFromAsset(String filePath) throws IOException {
        AssetManager assetManager = getAssets();
        InputStream istr = assetManager.open(filePath);
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        istr.close();
        return bitmap;
    }

    private void loadUserBalance() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    depositedCash = snapshot.child("deposit").getValue(Double.class) != null ?
                            snapshot.child("deposit").getValue(Double.class) : 0.0;
                    winningsCash = snapshot.child("winnings").getValue(Double.class) != null ?
                            snapshot.child("winnings").getValue(Double.class) : 0.0;
                } catch (Exception e) {
                    depositedCash = 0.0;
                    winningsCash = 0.0;
                }
                joinButton.setEnabled(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(details.this, "Failed to load balance", Toast.LENGTH_SHORT).show();
                joinButton.setEnabled(false);
            }
        });
    }

    private void joinMatch() {
        String feeStr = entryfee.getText().toString().trim();
        double requiredFee;

        if (feeStr.isEmpty()) {
            Toast.makeText(this, "Entry fee missing", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            requiredFee = Double.parseDouble(feeStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid entry fee", Toast.LENGTH_SHORT).show();
            return;
        }

        if (matchCategory != null && matchCategory.equalsIgnoreCase("Bonus Match")) {
            double bonusShare = requiredFee * 0.80;
            double cashShare = requiredFee * 0.20;

            userRef.child("bonus").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    double bonusBalance = 0.0;
                    if (snapshot.exists()) {
                        try {
                            bonusBalance = snapshot.getValue(Double.class) != null ? snapshot.getValue(Double.class) : 0.0;
                        } catch (Exception e) {
                            bonusBalance = 0.0;
                        }
                    }

                    if (bonusBalance < bonusShare) {
                        Toast.makeText(details.this, "Insufficient bonus balance", Toast.LENGTH_SHORT).show();
                        joinButton.setEnabled(true);
                        return;
                    }

                    if ((depositedCash + winningsCash) < cashShare) {
                        Toast.makeText(details.this, "Not enough deposited/winnings balance", Toast.LENGTH_SHORT).show();
                        joinButton.setEnabled(true);
                        return;
                    }

                    double newDeposit = depositedCash;
                    double newWinnings = winningsCash;

                    if (depositedCash >= cashShare) {
                        newDeposit = depositedCash - cashShare;
                    } else {
                        double remaining = cashShare - depositedCash;
                        newDeposit = 0;
                        newWinnings = winningsCash - remaining;
                    }

                    double newBonus = bonusBalance - bonusShare;

                    showGameUIDDialogWithBonus(newDeposit, newWinnings, newBonus);
                }

                private void showGameUIDDialogWithBonus(double newDeposit, double newWinnings, double newBonus) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(details.this);
                    View dialogView = LayoutInflater.from(details.this).inflate(R.layout.dialog_game_uid, null);
                    builder.setView(dialogView);

                    EditText inputUID = dialogView.findViewById(R.id.inputUID);
                    Button btnCancel = dialogView.findViewById(R.id.btnCancel);
                    Button btnDone = dialogView.findViewById(R.id.btnDone);

                    AlertDialog dialog = builder.create();

                    btnCancel.setOnClickListener(v -> dialog.dismiss());

                    btnDone.setOnClickListener(v -> {
                        String uidInput = inputUID.getText().toString().trim();
                        if (uidInput.isEmpty()) {
                            inputUID.setError("Please enter your UID");
                            return;
                        }

                        progressDialog.show();

                        DatabaseReference matchRef = FirebaseDatabase.getInstance()
                                .getReference("All_Matches")
                                .child(matchId);

                        DatabaseReference joiningsRef = FirebaseDatabase.getInstance()
                                .getReference("joinings")
                                .child(matchId);

                        matchRef.child("tUser").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String tUserStr = snapshot.getValue(String.class);
                                if (tUserStr == null || tUserStr.isEmpty()) {
                                    progressDialog.dismiss();
                                    Toast.makeText(details.this, "Max players info missing", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                int tUser = Integer.parseInt(tUserStr);

                                joiningsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot joinSnapshot) {
                                        long cUser = 0;
                                        for (DataSnapshot child : joinSnapshot.getChildren()) {
                                            if (child.getKey() != null && !child.getKey().equals("0")) {
                                                cUser++;
                                            }
                                        }

                                        if (cUser >= tUser) {
                                            progressDialog.dismiss();
                                            Toast.makeText(details.this, "Match is full! Try another one.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            performJoinLogicWithBonus(newDeposit, newWinnings, newBonus, uidInput, dialog);
                                        }
                                    }

                                    private void performJoinLogicWithBonus(double newDeposit, double newWinnings, double newBonus, String uidInput, AlertDialog dialog) {
                                        String userId = mAuth.getUid();

                                        DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("username");
                                        usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    String username = snapshot.getValue(String.class);

                                                    Map<String, Object> updates = new HashMap<>();
                                                    updates.put("/users/" + userId + "/deposit", roundToTwoDecimals(newDeposit));
                                                    updates.put("/users/" + userId + "/winnings", roundToTwoDecimals(newWinnings));
                                                    updates.put("/users/" + userId + "/bonus", roundToTwoDecimals(newBonus));
                                                    updates.put("/users/" + userId + "/joinedMatchIds/" + matchId, true);


                                                    FirebaseDatabase.getInstance().getReference().updateChildren(updates)
                                                            .addOnCompleteListener(task -> {
                                                                if (task.isSuccessful()) {
                                                                    DatabaseReference joiningRef = FirebaseDatabase.getInstance()
                                                                            .getReference("joinings")
                                                                            .child(matchId)
                                                                            .child(username);

                                                                    joiningRef.setValue(uidInput).addOnCompleteListener(joinTask -> {
                                                                        progressDialog.dismiss();

                                                                        if (joinTask.isSuccessful()) {
                                                                            Toast.makeText(details.this, "Joined match successfully", Toast.LENGTH_SHORT).show();
                                                                            joinButton.setText("Joined");
                                                                            joinButton.setEnabled(false);
                                                                            roomTxt.setVisibility(VISIBLE);
                                                                            idlayout.setVisibility(VISIBLE);
                                                                            passlayout.setVisibility(VISIBLE);

                                                                            String amount = entryfee.getText().toString().trim();
                                                                            String reason = "Joined Bonus Match: " + title.getText().toString().trim();
                                                                            addTransactionToFirebase(userId, amount, reason);

                                                                            dialog.dismiss();

                                                                        } else {
                                                                            Toast.makeText(details.this, "Failed to save join info", Toast.LENGTH_SHORT).show();
                                                                            joinButton.setEnabled(true);
                                                                        }
                                                                    });

                                                                } else {
                                                                    progressDialog.dismiss();
                                                                    Toast.makeText(details.this, "Failed to join match", Toast.LENGTH_SHORT).show();
                                                                    joinButton.setEnabled(true);
                                                                }
                                                            });

                                                } else {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(details.this, "Username not found", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            private double roundToTwoDecimals(double value) {
                                                return Math.round(value * 100.0) / 100.0;
                                            }


                                            @Override
                                            public void onCancelled(DatabaseError error) {
                                                progressDialog.dismiss();
                                                Toast.makeText(details.this, "Error loading username", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }


                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        progressDialog.dismiss();
                                        Toast.makeText(details.this, "Error reading joined users", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                progressDialog.dismiss();
                                Toast.makeText(details.this, "Error reading match info", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });

                    dialog.show();
                }


                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(details.this, "Failed to read bonus balance", Toast.LENGTH_SHORT).show();
                    joinButton.setEnabled(true);
                }
            });

        } else {
            double remainingFee = requiredFee;

            double newDeposit = depositedCash;
            double newWinnings = winningsCash;

            if (depositedCash >= remainingFee) {
                newDeposit = depositedCash - remainingFee;
            } else {
                remainingFee -= depositedCash;
                newDeposit = 0;

                if (winningsCash >= remainingFee) {
                    newWinnings = winningsCash - remainingFee;
                } else {
                    Toast.makeText(this, "Insufficient balance", Toast.LENGTH_SHORT).show();
                    joinButton.setEnabled(true);
                    return;
                }
            }

            showGameUIDDialog(newDeposit, newWinnings);
        }
    }


    private void showGameUIDDialog(double newDeposit, double newWinnings) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_game_uid, null);
        builder.setView(dialogView);

        EditText inputUID = dialogView.findViewById(R.id.inputUID);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnDone = dialogView.findViewById(R.id.btnDone);

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDone.setOnClickListener(v -> {
            String uidInput = inputUID.getText().toString().trim();
            if (uidInput.isEmpty()) {
                inputUID.setError("Please enter your UID");
                return;
            }

            progressDialog.show();

            // 1. Get Firebase paths
            DatabaseReference matchRef = FirebaseDatabase.getInstance()
                    .getReference("All_Matches")
                    .child(matchId);

            DatabaseReference joiningsRef = FirebaseDatabase.getInstance()
                    .getReference("joinings")
                    .child(matchId);

            // 2. Get tUser (limit)
            matchRef.child("tUser").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String tUserStr = snapshot.getValue(String.class);
                    if (tUserStr == null || tUserStr.isEmpty()) {
                        progressDialog.dismiss();
                        Toast.makeText(details.this, "Max players info missing", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int tUser = Integer.parseInt(tUserStr);

                    // 3. Get cUser (joined count)
                    joiningsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot joinSnapshot) {
                            long cUser = 0;
                            for (DataSnapshot child : joinSnapshot.getChildren()) {
                                // Skip any non-user keys (like nulls or unexpected structure)
                                if (child.getKey() != null && !child.getKey().equals("0")) {
                                    cUser++;
                                }
                            }

                            if (cUser >= tUser) {
                                progressDialog.dismiss();
                                Toast.makeText(details.this, "Match is full! Try another one.", Toast.LENGTH_SHORT).show();
                            } else {
                                // ✅ Join allowed
                                performJoinLogic(newDeposit, newWinnings, uidInput, dialog);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            progressDialog.dismiss();
                            Toast.makeText(details.this, "Error reading joined users", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressDialog.dismiss();
                    Toast.makeText(details.this, "Error reading match info", Toast.LENGTH_SHORT).show();
                }
            });
        });


        dialog.show();
    }

    private void performJoinLogic(double newDeposit, double newWinnings, String uidInput, AlertDialog dialog) {
        String userId = mAuth.getUid();

        // 1. Fetch the username from users/{userId}/username
        DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("username");
        usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.getValue(String.class);

                    // 2. Firebase multi-path update
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("/users/" + userId + "/deposit", newDeposit);
                    updates.put("/users/" + userId + "/winnings", newWinnings);
                    updates.put("/users/" + userId + "/joinedMatchIds/" + matchId, true);

                    FirebaseDatabase.getInstance().getReference().updateChildren(updates)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {

                                    // ✅ Save under joinings/matchCategory/matchId/{username} : uidInput
                                    DatabaseReference joiningRef = FirebaseDatabase.getInstance()
                                            .getReference("joinings")
                                            .child(matchId)
                                            .child(username);  // ← key

                                    joiningRef.setValue(uidInput).addOnCompleteListener(joinTask -> {
                                        progressDialog.dismiss();

                                        if (joinTask.isSuccessful()) {
                                            Toast.makeText(details.this, "Joined match successfully", Toast.LENGTH_SHORT).show();
                                            joinButton.setText("Joined");
                                            joinButton.setEnabled(false);
                                            roomTxt.setVisibility(VISIBLE);
                                            idlayout.setVisibility(VISIBLE);
                                            passlayout.setVisibility(VISIBLE);

                                            // 3. Save transaction
                                            String amount = entryfee.getText().toString().trim();
                                            String reason = "Joined Match: " + title.getText().toString().trim();
                                            addTransactionToFirebase(userId, amount, reason);

                                            dialog.dismiss();

                                        } else {
                                            Toast.makeText(details.this, "Failed to save join info", Toast.LENGTH_SHORT).show();
                                            joinButton.setEnabled(true);
                                        }
                                    });

                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(details.this, "Failed to join match", Toast.LENGTH_SHORT).show();
                                    joinButton.setEnabled(true);
                                }
                            });

                } else {
                    progressDialog.dismiss();
                    Toast.makeText(details.this, "Username not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(details.this, "Error loading username", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void addTransactionToFirebase(String userId, String amount, String reason) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("transactions")
                .child(userId);

        String timestampKey = String.valueOf(System.currentTimeMillis());

        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                .format(new Date());

        String txnId = generateRandomTxnId(6);

        // ✅ Set status = "success"
        txnModel model = new txnModel(date, reason, amount, txnId, "success");


            DatabaseReference userRef = com.google.firebase.database.FirebaseDatabase.getInstance()
                    .getReference("users").child(mAuth.getUid()).child("matches_played");

            userRef.runTransaction(new com.google.firebase.database.Transaction.Handler() {
                @NonNull
                @Override
                public com.google.firebase.database.Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    Long currentValue = currentData.getValue(Long.class);
                    if (currentValue == null) {
                        currentData.setValue(1);
                    } else {
                        currentData.setValue(currentValue + 1);
                    }
                    return com.google.firebase.database.Transaction.success(currentData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                    // You can show Toast or log here if needed
                }
            });


    }


    private String generateRandomTxnId(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder txnId = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < length; i++) {
            txnId.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return txnId.toString();
    }
}
