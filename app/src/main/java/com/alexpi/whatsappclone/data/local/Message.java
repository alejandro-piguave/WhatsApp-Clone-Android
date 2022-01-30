package com.alexpi.whatsappclone.data.local;

public class Message {
    private String uid, senderUid, senderName, message, media;
    private long timestamp;

    public Message(String uid, String senderUid, String senderName, String messageContent, long timestamp, String media) {
        this.uid = uid;
        this.senderUid = senderUid;
        this.message = messageContent;
        this.timestamp = timestamp;
        this.media = media;
        this.senderName = senderName;
    }

    public String getSenderName() {
        return senderName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public String getUid() {
        return uid;
    }

    public String getMessage() {
        return message;
    }

    public String getMedia() {
        return media;
    }



}
