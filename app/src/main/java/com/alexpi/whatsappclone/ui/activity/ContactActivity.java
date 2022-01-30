package com.alexpi.whatsappclone.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alexpi.whatsappclone.data.remote.FirebaseManager;
import com.alexpi.whatsappclone.R;
import com.alexpi.whatsappclone.data.remote.RemoteContact;
import com.alexpi.whatsappclone.listeners.OnItemClickListener;
import com.alexpi.whatsappclone.data.local.Contact;
import com.alexpi.whatsappclone.listeners.OnLoadCompleteListener;
import com.alexpi.whatsappclone.ui.adapter.ContactAdapter;

import java.util.ArrayList;
import java.util.List;

public class ContactActivity extends AppCompatActivity {

    private ContactAdapter adapter;
    private RecyclerView contactListView;
    private LinearLayout defaultLayout;
    private FirebaseManager firebaseManager;
    private ProgressBar progressBar;
    private SharedPreferences preferences;

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        defaultLayout = findViewById(R.id.default_layout);
        contactListView = findViewById(R.id.rvContactListView);
        progressBar = findViewById(R.id.progress_bar);
        firebaseManager = FirebaseManager.getInstance();

        preferences = getPreferences(Context.MODE_PRIVATE);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        setTitle(R.string.select_contact);

        initializeRecyclerView();
        loadContactList();
    }

    private void updateContactList(){
        firebaseManager.updateContactListAndLoad(this, new OnLoadCompleteListener<List<Contact>>() {
            @Override
            public void onLoadComplete(List<Contact> updatedRemoteContacts) {
                preferences.edit().putBoolean("contacts_first_time_synced",true).apply();
                setUpActivityInfo(updatedRemoteContacts);
            }
        });
    }
    private void setUpActivityInfo(final List<Contact> contacts){
        if(contacts.isEmpty()){
            contactListView.setVisibility(View.GONE);
            defaultLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }else{
            if(getSupportActionBar() != null){
                int contactsCount = contacts.size();
                getSupportActionBar().setSubtitle(getResources().getQuantityString(R.plurals.numberOfContacts,
                        contactsCount, contactsCount));
            }
            adapter.setList(contacts);
            adapter.notifyDataSetChanged();
            contactListView.setVisibility(View.VISIBLE);
            defaultLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void loadContactList() {
        boolean firstTimeContactsSynced = preferences.getBoolean("contacts_first_time_synced",false);
        if(firstTimeContactsSynced){
            updateContactListSafe();
        }else{
            firebaseManager.loadContactList( new OnLoadCompleteListener<List<Contact>>() {
                @Override
                public void onLoadComplete(List<Contact> contacts) {
                    setUpActivityInfo(contacts);
                }

            });
        }
    }
    private void updateContactListSafe(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            updateContactList();
        }else ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                MY_PERMISSIONS_REQUEST_READ_CONTACTS);
    }
    private void initializeRecyclerView(){
        contactListView = findViewById(R.id.rvContactListView);
        contactListView.setNestedScrollingEnabled(false);
        contactListView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        contactListView.setLayoutManager(layoutManager);

        contactListView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        adapter = new ContactAdapter(this, new ArrayList<Contact>());
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View v, final int position) {
                final String remoteUserKey = adapter.getList().get(position).getUserKey();
                firebaseManager.getChatKeyWithUser(adapter.getList().get(position).getUserKey(),
                        new OnLoadCompleteListener<String>() {
                            @Override
                            public void onLoadComplete(String chatKey) {
                                finish();
                                ChatActivity.openChat(ContactActivity.this,chatKey,remoteUserKey,adapter.getList().get(position).getName());

                            }
                        });
            }
        });
        contactListView.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                updateContactList();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contacts,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case R.id.refresh_contacts:
                updateContactListSafe();
                return true;
            default:
                return false;
        }
    }
}
