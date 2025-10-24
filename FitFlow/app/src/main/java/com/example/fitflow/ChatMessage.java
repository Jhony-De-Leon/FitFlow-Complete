package com.example.fitflow;

public class ChatMessage {
    private String messageText;
    private String messageTime; // Can be a long timestamp for sorting, formatted later
    private boolean isSentByUser;
    private String senderName; // e.g., "FlowCoach" or user's name (though usually not shown for user)

    public ChatMessage(String messageText, String messageTime, boolean isSentByUser, String senderName) {
        this.messageText = messageText;
        this.messageTime = messageTime;
        this.isSentByUser = isSentByUser;
        this.senderName = senderName;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime = messageTime;
    }

    public boolean isSentByUser() {
        return isSentByUser;
    }

    public void setSentByUser(boolean sentByUser) {
        isSentByUser = sentByUser;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}
