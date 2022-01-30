package com.alexpi.whatsappclone.data.remote;

public class RemoteChat {
    private String userKey, chatKey;
    public RemoteChat(String userKey, String name) {
        this.userKey = name;
        this.chatKey = userKey;
    }
    public String getUserKey() {
        return userKey;
    }

    public String getChatKey() {
        return chatKey;
    }

}
