package com.alexpi.whatsappclone.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alexpi.whatsappclone.data.remote.FirebaseManager;
import com.alexpi.whatsappclone.R;
import com.alexpi.whatsappclone.data.remote.RemoteContact;
import com.alexpi.whatsappclone.listeners.ChatEventListener;
import com.alexpi.whatsappclone.listeners.OnItemClickListener;
import com.alexpi.whatsappclone.data.local.Chat;
import com.alexpi.whatsappclone.listeners.OnLoadCompleteListener;
import com.alexpi.whatsappclone.ui.adapter.ChatAdapter;
import com.alexpi.whatsappclone.utils.MyNotificationOpenedHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ChatAdapter localChatAdapter;
    private RecyclerView chatListView;
    private List<RemoteContact> remoteContacts;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseManager = FirebaseManager.getInstance();
        initOneSignal();
        initRemoteContactList();
        initializeChatView();
    }

    private void initRemoteContactList(){
        firebaseManager.loadRemoteContactList(new OnLoadCompleteListener<List<RemoteContact>>() {
            @Override
            public void onLoadComplete(List<RemoteContact> remoteContacts) {
                MainActivity.this.remoteContacts = remoteContacts;
                initLocalChatList();
            }
        });
    }

    private void initOneSignal(){
        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new MyNotificationOpenedHandler(getApplication()))
                .init();
        OneSignal.setSubscription(true);
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("notification_key").setValue(userId);
            }
        });
        OneSignal.setInFocusDisplaying(OneSignal.OSInFocusDisplayOption.None);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.settings:
                openAppSettings();
                return true;
            case R.id.log_out:
                logOut();
                return true;
        }
        return false;
    }

    private void openAppSettings(){
        startActivity(new Intent(this,SettingsActivity.class));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    private void initLocalChatList(){
        firebaseManager.addChatEventListener(remoteContacts, new ChatEventListener() {
            @Override
            public void onNewChat(Chat element) {
                localChatAdapter.add(element);
                Log.i("CHAT", "Chat added");
            }

            @Override
            public void onChatUpdated(String chatKey,String lastMessage, long timestamp, boolean isOwnMessage) {
                localChatAdapter.update(chatKey,lastMessage,timestamp,isOwnMessage);
                Log.i("CHAT", "Chat updated");
            }


            @Override
            public void onElementRemoved(String elementKey) {
                localChatAdapter.remove(elementKey);
                Log.i("CHAT", "Chat removed");

            }
        });
    }

    private void initializeChatView() {
        chatListView = findViewById(R.id.chat_list);
        chatListView.setNestedScrollingEnabled(false);
        chatListView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,RecyclerView.VERTICAL,false);
        chatListView.setLayoutManager(layoutManager);

        chatListView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        localChatAdapter = new ChatAdapter(this, new ArrayList<Chat>());
        localChatAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Chat chat = localChatAdapter.getList().get(position);
                ChatActivity.openChat(MainActivity.this,chat.getKey(),chat.getUserKey(),chat.getName());

            }
        });
        chatListView.setAdapter(localChatAdapter);
    }


    private void logOut(){
        OneSignal.setSubscription(false);
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void openContacts(View view) {
        Intent intent = new Intent(this, ContactActivity.class);
        startActivity(intent);
    }
}
