package com.tournaments.tuskesport.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.FirebaseDatabase;
import com.tournaments.tuskesport.Models.Match_model;
import com.tournaments.tuskesport.R;
import com.tournaments.tuskesport.details;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class myAdapter extends RecyclerView.Adapter<myAdapter.holder> {

    ArrayList<Match_model> mlist;
    Context mContext;
    Set<String> joinedMatchIds = new HashSet<>();

    private final HashMap<String, ValueEventListener> joiningsListeners = new HashMap<>();
    private final HashMap<String, ValueEventListener> tUserListeners = new HashMap<>();
    private final HashMap<String, ValueEventListener> joinedRefListeners = new HashMap<>();

    String matchCategory;

    public myAdapter(ArrayList<Match_model> list, Context context, Set<String> joinedMatchIds, String category) {
        this.mContext = context;
        this.mlist = list != null ? list : new ArrayList<>();
        if (joinedMatchIds != null) {
            this.joinedMatchIds = joinedMatchIds;
        }
        this.matchCategory = category;
    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.ff_match_deisgn, parent, false);
        return new holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull holder holder, int position) {
        Match_model model = mlist.get(position);

        // Text set
        holder.type.setText(model.getType());
        holder.map.setText(model.getMap());
        holder.title.setText(model.getTitle());
        holder.date.setText(model.getDate());
        holder.prizepool.setText(model.getPrizepool());
        holder.perkill.setText(model.getPerkill());
        holder.entryfee.setText(model.getEntryfee());

        // ✅ IMAGE LOAD FROM URL (GLIDE)
        String imageUrl = model.getImage();
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Glide.with(mContext)
                    .load(imageUrl)
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(holder.matchImage);
        } else {
            holder.matchImage.setImageResource(R.drawable.logo);
        }

        String matchId = model.getMatchid();

        DatabaseReference joiningsRef = FirebaseDatabase.getInstance()
                .getReference("joinings").child(matchId);

        DatabaseReference tUserRef = FirebaseDatabase.getInstance()
                .getReference("All_Matches").child(matchId).child("tUser");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        final long[] currentUsers = {0};
        final long[] totalUsers = {0};
        final boolean[] isJoined = {false};

        // Remove old listeners
        if (joiningsListeners.containsKey(matchId)) {
            joiningsRef.removeEventListener(joiningsListeners.get(matchId));
            joiningsListeners.remove(matchId);
        }

        if (tUserListeners.containsKey(matchId)) {
            tUserRef.removeEventListener(tUserListeners.get(matchId));
            tUserListeners.remove(matchId);
        }

        if (user != null && joinedRefListeners.containsKey(matchId)) {
            DatabaseReference oldRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("joinedMatchIds")
                    .child(matchId);
            oldRef.removeEventListener(joinedRefListeners.get(matchId));
            joinedRefListeners.remove(matchId);
        }

        // Joinings listener
        ValueEventListener joiningsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUsers[0] = snapshot.getChildrenCount();
                updateProgress(holder, currentUsers[0], totalUsers[0]);
                updateJoinButton(holder, currentUsers[0], totalUsers[0], isJoined[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        joiningsRef.addValueEventListener(joiningsListener);
        joiningsListeners.put(matchId, joiningsListener);

        // Total user listener
        ValueEventListener tUserListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String val = snapshot.getValue(String.class);
                totalUsers[0] = val != null ? Long.parseLong(val) : 0;
                updateProgress(holder, currentUsers[0], totalUsers[0]);
                updateJoinButton(holder, currentUsers[0], totalUsers[0], isJoined[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        tUserRef.addValueEventListener(tUserListener);
        tUserListeners.put(matchId, tUserListener);

        // Joined check
        if (user == null) {
            holder.joinBtn.setText("Login Required");
            holder.joinBtn.setEnabled(false);
            holder.joinBtn.setAlpha(0.5f);
        } else {
            DatabaseReference joinedRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("joinedMatchIds")
                    .child(matchId);

            ValueEventListener joinedListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    isJoined[0] = snapshot.exists() &&
                            Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                    updateJoinButton(holder, currentUsers[0], totalUsers[0], isJoined[0]);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            };
            joinedRef.addValueEventListener(joinedListener);
            joinedRefListeners.put(matchId, joinedListener);
        }

        holder.joinBtn.setOnClickListener(v -> {
            String btnText = holder.joinBtn.getText().toString();
            if (btnText.equalsIgnoreCase("Join") || btnText.equalsIgnoreCase("Joined")) {
                Intent intent = new Intent(mContext, details.class);
                intent.putExtra("type", model.getType());
                intent.putExtra("map", model.getMap());
                intent.putExtra("title", model.getTitle());
                intent.putExtra("date", model.getDate());
                intent.putExtra("prizepool", model.getPrizepool());
                intent.putExtra("perkill", model.getPerkill());
                intent.putExtra("entryfee", model.getEntryfee());
                intent.putExtra("totalPrize", model.getTotalPrize());
                intent.putExtra("matchId", model.getMatchid());
                intent.putExtra("image_name", model.getImage()); // URL
                intent.putExtra("matchCategory", matchCategory);
                mContext.startActivity(intent);
            }
        });
    }

    private void updateJoinButton(holder holder, long currentUsers, long totalUsers, boolean isJoined) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            holder.joinBtn.setText("Login Required");
            holder.joinBtn.setEnabled(false);
            holder.joinBtn.setAlpha(0.5f);
            return;
        }

        if (isJoined) {
            holder.joinBtn.setText("Joined");
            holder.joinBtn.setEnabled(true);
            holder.joinBtn.setAlpha(1f);
        } else if (totalUsers > 0 && currentUsers >= totalUsers) {
            holder.joinBtn.setText("Full");
            holder.joinBtn.setEnabled(false);
            holder.joinBtn.setAlpha(0.5f);
        } else {
            holder.joinBtn.setText("Join");
            holder.joinBtn.setEnabled(true);
            holder.joinBtn.setAlpha(1f);
        }

        holder.joinBtn.setBackground(
                ContextCompat.getDrawable(mContext, R.drawable.button_border));
    }

    private void updateProgress(holder holder, long cUser, long tUser) {
        holder.cUser.setText(String.valueOf(cUser));
        holder.tUser.setText(String.valueOf(tUser));
        holder.available.setText(String.valueOf(tUser - cUser));
        holder.progressBar.setMax((int) Math.max(tUser, 1));
        holder.progressBar.setProgress((int) cUser);
    }

    @Override
    public int getItemCount() {
        return mlist.size();
    }

    class holder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;
        TextView type, map, title, date, prizepool, perkill, entryfee, cUser, tUser, available;
        Button joinBtn;
        ImageView matchImage;

        public holder(@NonNull View itemView) {
            super(itemView);
            type = itemView.findViewById(R.id.type);
            map = itemView.findViewById(R.id.map);
            title = itemView.findViewById(R.id.title);
            date = itemView.findViewById(R.id.date);
            prizepool = itemView.findViewById(R.id.prizepool);
            perkill = itemView.findViewById(R.id.perkill);
            entryfee = itemView.findViewById(R.id.entryfee);
            joinBtn = itemView.findViewById(R.id.joinBtn);
            cUser = itemView.findViewById(R.id.cUser);
            tUser = itemView.findViewById(R.id.tUser);
            available = itemView.findViewById(R.id.available);
            progressBar = itemView.findViewById(R.id.progressBar);
            matchImage = itemView.findViewById(R.id.matchImage);
        }
    }
}
