package com.alexpi.whatsappclone.ui.custom;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

public class CloneApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //Fresco.initialize(this);
        EmojiManager.install(new IosEmojiProvider());

        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

    }
}
