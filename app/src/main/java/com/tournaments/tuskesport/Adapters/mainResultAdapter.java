package com.tournaments.tuskesport.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tournaments.tuskesport.Models.mainResultModel;
import com.tournaments.tuskesport.R;

import java.util.ArrayList;

public class mainResultAdapter extends RecyclerView.Adapter<mainResultAdapter.ViewHolder> {

    Context context;
    ArrayList<mainResultModel> list;

    public mainResultAdapter(Context context, ArrayList<mainResultModel> list) {
        this.context = context;
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView pos, username, kills, winnings;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pos = itemView.findViewById(R.id.position);
            username = itemView.findViewById(R.id.username);
            kills = itemView.findViewById(R.id.kills);
            winnings = itemView.findViewById(R.id.winnings);
        }
    }

    @NonNull
    @Override
    public mainResultAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.result_design, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull mainResultAdapter.ViewHolder holder, int position) {
        mainResultModel model = list.get(position);

        holder.pos.setText(model.getPosition());
        holder.username.setText(model.getUsername());
        holder.kills.setText(model.getKills());
        holder.winnings.setText(model.getWinnings());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
