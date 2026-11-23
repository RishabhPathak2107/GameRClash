package com.tournaments.gamerclash.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tournaments.gamerclash.Models.Result_model;
import com.tournaments.gamerclash.R;
import com.tournaments.gamerclash.result;

import java.util.ArrayList;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    ArrayList<Result_model> list;
    Context context;

    String matchCategory; // Global variable add karo

    public ResultAdapter(ArrayList<Result_model> list, Context context, String category) {
        this.context = context;
        this.list = list;
        this.matchCategory = category;
    }
    public ResultAdapter(ArrayList<Result_model> list, Context context) {
        this.list = list;
        this.context = context;
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, prize, perkill, entry, type, map;
        Button watch;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            date = itemView.findViewById(R.id.time);
            prize = itemView.findViewById(R.id.prize_pool);
            perkill = itemView.findViewById(R.id.per_kill); // fixed ID
            entry = itemView.findViewById(R.id.entry_fee);
            type = itemView.findViewById(R.id.type);
            map = itemView.findViewById(R.id.map);
            watch = itemView.findViewById(R.id.watch_match);
        }
    }

    @NonNull
    @Override
    public ResultAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultAdapter.ViewHolder holder, int position) {
        Result_model model = list.get(position);
        holder.title.setText(model.getTitle());
        holder.date.setText(model.getDate());
        holder.prize.setText(model.getPrizepool());
        holder.perkill.setText( model.getPerkill());
        holder.entry.setText( model.getEntryfee());
        holder.type.setText(model.getType());
        holder.map.setText(model.getMap());

        // Handle button click
        holder.watch.setOnClickListener(v -> {
            Intent intent = new Intent(context, result.class);
            intent.putExtra("title", model.getTitle());
            intent.putExtra("date", model.getDate());
            intent.putExtra("prizepool", model.getPrizepool());
            intent.putExtra("perkill", model.getPerkill());
            intent.putExtra("entryfee", model.getEntryfee());
            intent.putExtra("matchId", model.getMatchId()); // Add match ID for Firebase fetch
            context.startActivity(intent);
        });

        // Removed: holder.status.setText("NOT JOINED");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
