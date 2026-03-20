package com.tournaments.tuskesport.Adapters;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tournaments.tuskesport.Models.chatModel;
import com.tournaments.tuskesport.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<chatModel> messageList;
    private String currentUserName = "";

    public ChatAdapter(List<chatModel> messageList) {
        this.messageList = messageList;
    }

    public void setCurrentUserName(String name) {
        this.currentUserName = name;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        chatModel currentMessage = messageList.get(position);
        String senderName = currentMessage.getSenderName();
        String messageText = currentMessage.getMessage();

        holder.messageText.setText(messageText);

        boolean isSelf = senderName.equals(currentUserName);

        // Align message bubble and username
        LinearLayout.LayoutParams msgParams = (LinearLayout.LayoutParams) holder.messageText.getLayoutParams();
        LinearLayout.LayoutParams nameParams = (LinearLayout.LayoutParams) holder.senderName.getLayoutParams();

        if (isSelf) {
            holder.messageText.setBackgroundResource(R.drawable.bg_self);
            msgParams.gravity = Gravity.END;
            nameParams.gravity = Gravity.END;
        } else {
            holder.messageText.setBackgroundResource(R.drawable.bg_other);
            msgParams.gravity = Gravity.START;
            nameParams.gravity = Gravity.START;
        }

        holder.messageText.setLayoutParams(msgParams);
        holder.senderName.setLayoutParams(nameParams);

        // Show username only if new sender
        boolean isNewGroup = true;
        if (position > 0) {
            String prevSender = messageList.get(position - 1).getSenderName();
            if (senderName.equals(prevSender)) {
                isNewGroup = false;
            }
        }

        if (isNewGroup) {
            holder.senderName.setVisibility(View.VISIBLE);
            holder.senderName.setText(senderName);
            holder.senderName.setTextColor(isSelf ? Color.parseColor("#007BFF") : Color.parseColor("#28A745"));

            // Add top margin
//            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
//            if (layoutParams != null) {
//                float scale = holder.itemView.getResources().getDisplayMetrics().density;
//                layoutParams.topMargin = (int) (24 * scale + 0.5f);
//                holder.itemView.setLayoutParams(layoutParams);
//            }
        } else {
            holder.senderName.setVisibility(View.GONE);

            // Small margin for continuous message
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
            if (layoutParams != null) {
                float scale = holder.itemView.getResources().getDisplayMetrics().density;
                layoutParams.topMargin = (int) (4 * scale + 0.5f);
                holder.itemView.setLayoutParams(layoutParams);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView senderName, messageText;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            senderName = itemView.findViewById(R.id.senderName);
            messageText = itemView.findViewById(R.id.messageText);
        }
    }
}
