package com.alexpi.whatsappclone.data.remote;

import java.util.HashMap;
import java.util.List;

public class RemoteContact {
    private String name, userKey;
    public RemoteContact(String userKey, String name) {
        this.name = name;
        this.userKey = userKey;
    }

    public String getName() {
        return name;
    }
    public String getUserKey() {
        return userKey;
    }


    static HashMap<String, Object> listToHashMap(List<RemoteContact> remoteContacts){
        HashMap<String, Object> hashMap = new HashMap<>();
        for(RemoteContact remoteContact: remoteContacts)
            hashMap.put(remoteContact.getUserKey(),remoteContact.getName());
        return hashMap;
    }


    public static RemoteContact getContactByUserKey(List<RemoteContact> contacts, String key){
        for(RemoteContact contact: contacts)
            if(contact.getUserKey().equals(key))
                return contact;
        return null;
    }
}
