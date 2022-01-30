package com.alexpi.whatsappclone.listeners;

import com.alexpi.whatsappclone.data.local.Chat;

public interface ChatEventListener {
    void onNewChat(Chat chat);
    void onChatUpdated(String chatKey, String lastMessage, long timestamp, boolean isOwnMessage);
    void onElementRemoved(String elementKey);
}
