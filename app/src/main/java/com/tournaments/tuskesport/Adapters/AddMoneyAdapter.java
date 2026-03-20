package com.tournaments.tuskesport.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tournaments.tuskesport.Models.AddMoneyModel;
import com.tournaments.tuskesport.Payment;
import com.tournaments.tuskesport.R;

import java.util.ArrayList;

public class AddMoneyAdapter extends RecyclerView.Adapter<AddMoneyAdapter.ViewHolder> {
    Context context;
    ArrayList<AddMoneyModel> list;

    public AddMoneyAdapter(Context context, ArrayList<AddMoneyModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.add_money_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AddMoneyModel model = list.get(position);
        holder.amountText.setText(model.getAmount()+" Coins");
        holder.desc.setText(model.getAmount()+" INR = "+model.getAmount()+" COINS! After successful payment, please make sure to share your correct UTR ID to get your coins.");

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, Payment.class);
            intent.putExtra("amount", model.getAmount());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView amountText,desc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            amountText = itemView.findViewById(R.id.amountText);
            desc = itemView.findViewById(R.id.desc);
        }
    }
}
