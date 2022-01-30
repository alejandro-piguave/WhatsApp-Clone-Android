package com.alexpi.whatsappclone.data.local;

import android.content.Context;

import com.alexpi.whatsappclone.R;
import com.alexpi.whatsappclone.utils.Utils;

import java.util.Date;

public class Chat implements Comparable<Chat>{
    private String key, userKey, name, lastMessage, picture;
    private long lastMessageTimestamp;
    private boolean isLastMessageOwn;


    public Chat(String key, String userKey, String name, String lastMessage, String picture, long lastMessageTimestamp, boolean isLastMessageOwn) {
        this.key = key;
        this.userKey = userKey;
        this.name = name;
        this.lastMessage = lastMessage;
        this.picture = picture;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.isLastMessageOwn = isLastMessageOwn;
    }

    public String getKey() {
        return key;
    }
    public String getName() {
        return name;
    }
    public String getLastMessage() {
        return lastMessage;
    }

    public String getPicture() {
        return picture;
    }

    public String getTimestampText(Context context){
        long elapsedTime = new Date().getTime() - lastMessageTimestamp;
        if(elapsedTime < 86400000)
            return Utils.format1(lastMessageTimestamp);
        else if( elapsedTime < 172800000)
            return context.getString(R.string.yesterday);
        else
            return Utils.format2(lastMessageTimestamp);
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageInfo(String lastMessage, long lastMessageTimestamp, boolean isLastMessageOwn){
        this.lastMessage = lastMessage;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.isLastMessageOwn = isLastMessageOwn;
    }

    public boolean isLastMessageOwn() {
        return isLastMessageOwn;
    }

    public void setLastMessageOwn(boolean lastMessageOwn) {
        isLastMessageOwn = lastMessageOwn;
    }

    public String getUserKey() {
        return userKey;
    }

    @Override
    public int compareTo(Chat chat) {
        return (int) (chat.getLastMessageTimestamp() - lastMessageTimestamp);
    }
}
