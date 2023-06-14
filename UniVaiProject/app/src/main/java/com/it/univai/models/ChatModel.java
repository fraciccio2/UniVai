package com.it.univai.models;

public class ChatModel {

    private String sender;
    private String receiver;
    private String message;
    private boolean seen;

    public ChatModel(String sender, String receiver, String message, boolean seen) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.seen = seen;
    }

    public ChatModel() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
