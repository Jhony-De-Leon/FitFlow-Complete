package com.example.fitflow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private Context context;
    private List<ChatMessage> messageList;

    public ChatAdapter(Context context, List<ChatMessage> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messageList.get(position);
        if (message.isSentByUser()) {
            return VIEW_TYPE_MESSAGE_SENT;
        }
        return VIEW_TYPE_MESSAGE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_chat_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else { // VIEW_TYPE_MESSAGE_RECEIVED
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_chat_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_MESSAGE_SENT) {
            ((SentMessageViewHolder) holder).bind(message);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.chatMessageTextSent);
            timeText = itemView.findViewById(R.id.chatMessageTimestampSent);
        }

        void bind(ChatMessage message) {
            messageText.setText(message.getMessageText());
            timeText.setText(message.getMessageTime());
        }
    }

    private static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, avatarText; // Assuming avatar is a TextView for initials

        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.chatMessageTextReceived);
            timeText = itemView.findViewById(R.id.chatMessageTimestampReceived);
            avatarText = itemView.findViewById(R.id.chatMessageAvatarReceived);
        }

        void bind(ChatMessage message) {
            messageText.setText(message.getMessageText());
            timeText.setText(message.getMessageTime());
            if (message.getSenderName() != null && !message.getSenderName().isEmpty()) {
                avatarText.setText(message.getSenderName().substring(0, Math.min(message.getSenderName().length(), 2)).toUpperCase());
            } else {
                avatarText.setText("FC"); // Default if no sender name
            }
        }
    }
}
