package com.alexpi.whatsappclone.utils;

import android.app.Application;

import com.alexpi.whatsappclone.ui.activity.ChatActivity;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

public class MyNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {

    private Application application;

    public MyNotificationOpenedHandler(Application application) {
        this.application = application;
    }

    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        JSONObject data = result.notification.payload.additionalData;
        if (data != null) {
            String chatKey, userKey, chatTitle;
            chatKey = data.optString("chat_key");
            userKey = data.optString("user_key");
            chatTitle = data.optString("chat_title");

            if(!chatKey.isEmpty() && !userKey.isEmpty() && !chatTitle.isEmpty())
                ChatActivity.openChat(application.getApplicationContext(),chatKey,userKey,chatTitle);
        }
    }


}