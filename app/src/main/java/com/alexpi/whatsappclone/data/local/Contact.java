package com.alexpi.whatsappclone.data.local;

public class Contact {
    private String userKey, name, aboutInfo, logo;

    public Contact(String userKey, String name, String aboutInfo, String pfpUrl) {

        this.userKey = userKey;
        this.name = name;
        this.aboutInfo = aboutInfo;
        this.logo = pfpUrl;
    }
    public String getUserKey() {
        return userKey;
    }
    public String getName() {
        return name;
    }
    public String getAboutInfo() {
        return aboutInfo;
    }

    public String getLogo() {
        return logo;
    }
}
