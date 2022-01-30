package com.alexpi.whatsappclone.data.remote;

import com.google.firebase.database.PropertyName;


public class RemoteMessage {
    private String senderKey, messageText, media;
    private long timestamp;

    public RemoteMessage(String senderKey, String messageText, String media, long timestamp) {
        this.senderKey = senderKey;
        this.messageText = messageText;
        this.media = media;
        this.timestamp = timestamp;
    }
    public RemoteMessage(){}

    @PropertyName("sender")
    public String getSenderKey() {
        return senderKey;
    }
    @PropertyName("text")
    public String getMessageText() {
        return messageText;
    }
    @PropertyName("media")
    public String getMedia() {
        return media;
    }
    @PropertyName("timestamp")
    public long getTimestamp() {
        return timestamp;
    }

    @PropertyName("sender")
    public void setSenderKey(String senderKey) {
        this.senderKey = senderKey;
    }
    @PropertyName("text")
    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
    @PropertyName("media")
    public void setMedia(String media) {
        this.media = media;
    }
    @PropertyName("timestamp")
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
