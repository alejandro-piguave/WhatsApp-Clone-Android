package com.alexpi.whatsappclone.data.local;

public class PhoneContact {
    private String name, phoneNumber;
    public PhoneContact(String name, String phoneNumber) {

        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
}
