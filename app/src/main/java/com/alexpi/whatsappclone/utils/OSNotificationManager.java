package com.alexpi.whatsappclone.utils;

import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OSNotificationManager {

    public static void sendNotification(String title, String message, String receiverKey, String chatKey, String userKey){
        JSONObject notification = new JSONObject(),
                contents = new JSONObject(),
                headings = new JSONObject(),
                data = new JSONObject();
        JSONArray includePlayerIds = new JSONArray();
        try{
            contents.put("en",message);

            headings.put("en",title);

            includePlayerIds.put(receiverKey);

            data.put("chat_key",chatKey);
            data.put("chat_title",title);
            data.put("user_key",userKey);

            notification.put("contents",contents);
            notification.put("headings",headings);
            notification.put("include_player_ids",includePlayerIds);
            notification.put("data",data);
            notification.put("android_group","CLONE_APP_GROUP");

        }catch (JSONException e){
            e.printStackTrace();
        }
        finally {
            OneSignal.postNotification(notification,null);
        }
    }
}
