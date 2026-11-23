package com.tournaments.gamerclash.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tournaments.gamerclash.Models.LeaderboardModel;
import com.tournaments.gamerclash.R;

import java.util.ArrayList;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    Context context;
    ArrayList<LeaderboardModel> list;

    public LeaderboardAdapter(Context context, ArrayList<LeaderboardModel> list) {
        this.context = context;
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView rank, name, username, totalCoins;
        ImageView profileImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rank = itemView.findViewById(R.id.rank);
            name = itemView.findViewById(R.id.name);
            username = itemView.findViewById(R.id.username);
            totalCoins = itemView.findViewById(R.id.total_coins);
            profileImg = itemView.findViewById(R.id.user_img);
        }
    }

    @NonNull
    @Override
    public LeaderboardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.leaderboard_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardAdapter.ViewHolder holder, int position) {
        LeaderboardModel model = list.get(position);

        // Rank starts from 4 onward
        holder.rank.setText(String.valueOf(position + 4));
        holder.name.setText(model.getFname() + " " + model.getLname());
        holder.username.setText(model.getUsername());
        holder.totalCoins.setText(String.valueOf(model.getTotal_coins()));

            holder.profileImg.setImageResource(R.drawable.sample_avatar);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
