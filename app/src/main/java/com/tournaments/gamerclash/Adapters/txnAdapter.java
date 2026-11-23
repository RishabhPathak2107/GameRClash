package com.tournaments.gamerclash.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.tournaments.gamerclash.Models.txnModel;
import com.tournaments.gamerclash.R;

import java.util.ArrayList;

public class txnAdapter extends RecyclerView.Adapter<txnAdapter.holder> {
    private ArrayList<txnModel> mlist;
    private Context mContext;

    // ✅ Constructor
    public txnAdapter(Context context, ArrayList<txnModel> list) {
        this.mContext = context;
        this.mlist = list;
    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.transaction_item, parent, false);
        return new holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull holder holder, int position) {
        txnModel model = mlist.get(position);

        holder.amount.setText(model.getAmount());
        holder.date.setText(model.getDate());
        holder.reason.setText(model.getReason());
        holder.status.setText(model.getStatus());
        holder.txnId.setText("#id: " + model.getId());

        // ✅ Optional: Status color
        switch (model.getStatus().toLowerCase()) {
            case "success":
                holder.status.setBackground(ContextCompat.getDrawable(mContext, R.drawable.status_success_bg));
                break;
            case "pending":
                holder.status.setBackground(ContextCompat.getDrawable(mContext, R.drawable.status_pending_bg));
                break;
            case "failed":
                holder.status.setBackground(ContextCompat.getDrawable(mContext, R.drawable.status_failed_bg));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mlist.size();
    }

    class holder extends RecyclerView.ViewHolder {
        TextView date, reason, amount, status, txnId;

        public holder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            reason = itemView.findViewById(R.id.reason);
            amount = itemView.findViewById(R.id.amount);
            status = itemView.findViewById(R.id.status);
            txnId = itemView.findViewById(R.id.id);
        }
    }

}
