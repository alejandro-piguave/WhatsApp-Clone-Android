package com.alexpi.whatsappclone.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alexpi.whatsappclone.R;
import com.alexpi.whatsappclone.data.local.MediaInfo;
import com.alexpi.whatsappclone.data.remote.FirebaseManager;
import com.alexpi.whatsappclone.data.remote.RemoteContact;
import com.alexpi.whatsappclone.data.remote.RemoteMessage;
import com.alexpi.whatsappclone.listeners.OnLoadCompleteListener;
import com.alexpi.whatsappclone.listeners.OnItemClickListener;
import com.alexpi.whatsappclone.ui.adapter.MediaAdapter;
import com.alexpi.whatsappclone.ui.adapter.MessageAdapter;
import com.alexpi.whatsappclone.utils.OSNotificationManager;
import com.alexpi.whatsappclone.data.local.Message;
import com.alexpi.whatsappclone.utils.Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.theartofdev.edmodo.cropper.CropImage;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private EmojiEditText inputET;
    private ImageButton emojiButton;

    private AppCompatImageButton sendButton;

    private MessageAdapter messageAdapter;
    private LinearLayoutManager messagesLayoutManager;
    private ArrayList<Message> messages;

    private RecyclerView mediaListView;
    private MediaAdapter mediaAdapter;
    private ArrayList<Uri> mediaUriList;

    private EmojiPopup emojiPopup;
    private String chatKey, userKey2, notificationKey2, notificationTitle = "";

    private FirebaseManager firebaseManager;

    private final static int GALLERY_IMAGE_REQUEST_CODE = 3;
    private final int CAMERA_CAPTURE_REQUEST_CODE = 1;

    public final static String CHAT_KEY = "chat_key";
    public final static String USER_KEY = "user_key";
    public final static String CHAT_TITLE = "chat_title";


    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
        @Override
        public void afterTextChanged(Editable editable) {
            if(editable.toString().isEmpty())
                sendButton.setEnabled(false);
            else sendButton.setEnabled(true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        firebaseManager = FirebaseManager.getInstance();

        inputET = findViewById(R.id.edittext_chatbox);
        emojiButton = findViewById(R.id.emoji_button);
        sendButton = findViewById(R.id.button_chatbox_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(view);
            }
        });
        sendButton.setEnabled(false);

        inputET.addTextChangedListener(textWatcher);
        ConstraintLayout rootLayout = findViewById(R.id.root_layout);

        emojiPopup= EmojiPopup.Builder.fromRootView(rootLayout).build(inputET);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        chatKey = getIntent().getStringExtra(CHAT_KEY);
        setTitle(getIntent().getStringExtra(CHAT_TITLE));
        userKey2 = getIntent().getStringExtra(USER_KEY);
        firebaseManager.getNotificationKeyFromUser(userKey2, new OnLoadCompleteListener<String>() {
            @Override
            public void onLoadComplete(String element) {
                notificationKey2 = element;
            }
        });
        firebaseManager.loadRemoteContactList(userKey2, new OnLoadCompleteListener<List<RemoteContact>>() {
            @Override
            public void onLoadComplete(List<RemoteContact> contacts) {
                RemoteContact remoteContact = RemoteContact.getContactByUserKey(contacts,FirebaseAuth.getInstance().getCurrentUser().getUid());
                if(remoteContact == null)
                    notificationTitle = Utils.formatNumberCompat(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                else notificationTitle = remoteContact.getName();
            }
        });



        messages = new ArrayList<>();
        mediaUriList = new ArrayList<>();
        initMessageListView();
        initMediaListView();
        loadMessages();
    }

    public static void openChat(Context context, String chatKey, String userKey, String chatTitle) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(CHAT_KEY,chatKey);
        intent.putExtra(CHAT_TITLE,chatTitle);
        intent.putExtra(USER_KEY,userKey);
        context.startActivity(intent);
        if(context instanceof Activity){
            ((Activity)context).overridePendingTransition(R.anim.slide_up_info,R.anim.no_change);
        }
    }

    private void loadMessages() {
        firebaseManager.loadRemoteContactList( new OnLoadCompleteListener<List<RemoteContact>>() {
            @Override
            public void onLoadComplete(List<RemoteContact> contacts) {
                firebaseManager.loadMessageList(ChatActivity.this, contacts,chatKey, new OnLoadCompleteListener<Message>() {
                    @Override
                    public void onLoadComplete(Message message) {
                        messages.add(message);
                        messagesLayoutManager.scrollToPosition(messages.size()-1);
                        messageAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

    }

    private void initMediaListView(){
        mediaListView = findViewById(R.id.mediaListView);
        mediaListView.setNestedScrollingEnabled(false);
        mediaListView.setHasFixedSize(true);

        LinearLayoutManager mediaLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        mediaListView.setLayoutManager(mediaLayoutManager);

        mediaAdapter = new MediaAdapter(this,mediaUriList);
        mediaAdapter.setDeleteButtonOnClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View v, final int position) {
                new AlertDialog.Builder(ChatActivity.this).setMessage(R.string.delete_media_item_title)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mediaUriList.remove(position);
                                mediaAdapter.notifyItemRemoved(position);

                                if(mediaUriList.isEmpty()){
                                    sendButton.setEnabled(false);
                                    mediaListView.setVisibility(View.GONE);
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no,null)
                        .show();
            }
        });

        mediaListView.setAdapter(mediaAdapter);
    }
    private void initMessageListView() {
        final RecyclerView messageListView = findViewById(R.id.message_list);
        messageListView.setNestedScrollingEnabled(false);
        messageListView.setHasFixedSize(true);

        messagesLayoutManager = new LinearLayoutManager(this,RecyclerView.VERTICAL,false);
        messageListView.setLayoutManager(messagesLayoutManager);

        messageAdapter = new MessageAdapter(this, messages);
        messageAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                List<MediaInfo> mediaInfo = messageAdapter.getMediaInfo();
                int index = MediaInfo.indexOf(mediaInfo,messageAdapter.getList().get(position).getMedia());
               openMediaActivity( messageAdapter.getMediaInfo(), index);
            }
        });

        messageListView.setAdapter(messageAdapter);
    }

    private void openMediaActivity(ArrayList<MediaInfo> mediaList, int position){
        Intent intent = new Intent(this,MediaActivity.class);
        intent.putParcelableArrayListExtra("mediaList",mediaList);
        intent.putExtra("index",position);
        startActivity(intent);
    }

    public void sendMessage(View view) {
        final String messageText = inputET.getText().toString().trim().replace("\n","");
        if(!messageText.isEmpty() || !mediaUriList.isEmpty()){
            if(!mediaUriList.isEmpty()){
                final AlertDialog dialog = new AlertDialog.Builder(this).setView(R.layout.loading_dialog_layout).create();
                dialog.show();
                for(final Uri uri: mediaUriList){
                    String subMessage = mediaUriList.indexOf(uri) == mediaUriList.size() -1 ? messageText: null;
                    firebaseManager.sendMessage(chatKey, userKey2,subMessage, uri, new OnSuccessListener<RemoteMessage>() {
                        @Override
                        public void onSuccess(RemoteMessage message) {
                            if(mediaUriList.indexOf(uri) == mediaUriList.size() -1) {
                                dialog.dismiss();
                                mediaUriList.clear();
                                mediaAdapter.notifyDataSetChanged();
                                OSNotificationManager.sendNotification(notificationTitle,
                                        messageText.isEmpty() ? getString(R.string.image_message) : messageText,
                                        notificationKey2, chatKey, userKey2);
                            }
                        }
                    });
                    inputET.setText(null);
                }
            }else{
                firebaseManager.sendMessage(chatKey, userKey2, messageText, null, new OnSuccessListener<RemoteMessage>() {
                    @Override
                    public void onSuccess(RemoteMessage message) {
                        OSNotificationManager.sendNotification(notificationTitle,messageText,
                                notificationKey2, chatKey, userKey2);
                    }
                });
                inputET.setText(null);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case R.id.view_contact:
                return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat,menu);
        return true;
    }

    public void showEmojiBoard(View view) {
        if(emojiPopup.isShowing()) {
            emojiPopup.dismiss();
            emojiButton.setImageResource(R.drawable.ic_insert_emoticon);
        }
        else{
            emojiPopup.toggle();
            emojiButton.setImageResource(R.drawable.ic_keyboard);
        }
    }

    public void showFilesDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(R.array.attach_actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        openGallery();
                        break;
                    case 1:
                        if (CropImage.isExplicitCameraPermissionRequired(ChatActivity.this))
                            ActivityCompat.requestPermissions(ChatActivity.this,new String[]{Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
                    else openCamera();
                        break;
                }
            }
        }).show();
    }
    private void openCamera(){
        Intent captureIntent = CropImage.getCameraIntent(this,null);
        startActivityForResult(captureIntent, CAMERA_CAPTURE_REQUEST_CODE);
    }
    private void openGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,getString(R.string.send_to,getTitle())),GALLERY_IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == GALLERY_IMAGE_REQUEST_CODE){
                if(!sendButton.isEnabled())
                    sendButton.setEnabled(true);
                if(data.getClipData() == null) {
                    mediaUriList.add(data.getData());
                    mediaAdapter.notifyItemInserted(mediaUriList.size()-1);
                }else {
                    int itemCount = data.getClipData().getItemCount();

                    for(int i = 0; i<itemCount;i++) mediaUriList.add(data.getClipData().getItemAt(i).getUri());

                    mediaAdapter.notifyItemRangeInserted(mediaUriList.size()-1-itemCount,itemCount);
                }
                mediaAdapter.notifyDataSetChanged();
            }else if(requestCode == CAMERA_CAPTURE_REQUEST_CODE){
                Uri uri = CropImage.getCaptureImageOutputUri(this);
                mediaUriList.add(uri);
                mediaAdapter.notifyItemInserted(mediaUriList.size()-1);
            }
            mediaListView.setVisibility(View.VISIBLE);

        }
    }
}
