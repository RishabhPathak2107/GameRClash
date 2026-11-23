package com.tournaments.gamerclash.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tournaments.gamerclash.Models.WithdrawModel;
import com.tournaments.gamerclash.R;
import com.tournaments.gamerclash.WithdrawForm;

import java.util.ArrayList;

public class WithdrawAdapter extends RecyclerView.Adapter<WithdrawAdapter.ViewHolder> {

    Context context;
    ArrayList<WithdrawModel> list;

    public WithdrawAdapter(Context context, ArrayList<WithdrawModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.withdraw_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WithdrawModel model = list.get(position);

        holder.withdrawType.setText(model.getType().toUpperCase());
        holder.withdrawDetail.setText("Withdraw ₹" + model.getAmount() + " (" + model.getAmount() + " coins)");
        holder.withdrawButton.setText((model.getAmount()+" INR"));

        if (model.getType().equals("UPI")) {
            holder.iconImage.setImageResource(R.drawable.upi_logo);
        } else {
            holder.iconImage.setImageResource(R.drawable.playstore_logo);
        }

        holder.layout.setOnClickListener(v -> {
            Intent intent = new Intent(context, WithdrawForm.class);
            intent.putExtra("type", model.getType());       // "upi" or "google_play"
            intent.putExtra("amount", model.getAmount());   // ₹ value
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView withdrawType, withdrawDetail, withdrawButton;
        ImageView iconImage;
        LinearLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            withdrawType = itemView.findViewById(R.id.withdrawType);
            withdrawDetail = itemView.findViewById(R.id.withdrawDetail);
            withdrawButton = itemView.findViewById(R.id.withdrawButton);
            iconImage = itemView.findViewById(R.id.iconImage);
            layout = itemView.findViewById(R.id.layout);
        }
    }
}

