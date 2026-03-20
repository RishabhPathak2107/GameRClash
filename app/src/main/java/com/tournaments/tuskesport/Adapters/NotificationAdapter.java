package com.tournaments.tuskesport.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tournaments.tuskesport.Models.NotificationModel;
import com.tournaments.tuskesport.R;
import com.tournaments.tuskesport.notificationDetail;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<NotificationModel> notifications;
    private Context context;

    public NotificationAdapter(Context context, List<NotificationModel> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.ViewHolder holder, int position) {
        NotificationModel model = notifications.get(position);
        holder.title.setText(model.getTitle());
        holder.time.setText("Time: "+model.getTime());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, notificationDetail.class);
            intent.putExtra("title", model.getTitle());
            intent.putExtra("time", model.getTime());
            intent.putExtra("detail", model.getDetail());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            time = itemView.findViewById(R.id.time);
        }
    }
}
