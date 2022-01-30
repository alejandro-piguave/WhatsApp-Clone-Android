package com.alexpi.whatsappclone.data.remote;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alexpi.whatsappclone.R;
import com.alexpi.whatsappclone.data.local.Chat;
import com.alexpi.whatsappclone.data.local.Contact;
import com.alexpi.whatsappclone.data.local.Message;
import com.alexpi.whatsappclone.data.local.PhoneContact;
import com.alexpi.whatsappclone.listeners.ChatEventListener;
import com.alexpi.whatsappclone.listeners.OnLoadCompleteListener;
import com.alexpi.whatsappclone.utils.Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class FirebaseManager {
    private static FirebaseManager instance = null;

    private FirebaseManager(){
    }

    public static synchronized FirebaseManager getInstance() {
        if(instance == null)
            instance = new FirebaseManager();
        return instance;
    }

    public String getCurrentUserKey() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void loadRemoteContactList(final OnLoadCompleteListener<List<RemoteContact>> listener) {
        loadRemoteContactList(getCurrentUserKey(),listener);
    }

    public void loadRemoteContactList(String userKey, final OnLoadCompleteListener<List<RemoteContact>> listener) {
        final ArrayList<RemoteContact> contacts = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("users").child(userKey)
                .child("contacts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot childSnapshot: dataSnapshot.getChildren()){
                        String contactKey = childSnapshot.getKey();
                        String contactName = childSnapshot.getValue().toString();
                        contacts.add(new RemoteContact(contactKey,contactName));
                    }
                    listener.onLoadComplete(contacts);
                }else listener.onLoadComplete(contacts);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    public void updateContactListAndLoad(Context context, final OnLoadCompleteListener<List<Contact>> listener) {
        loadPhoneContactList(context, new OnLoadCompleteListener<List<PhoneContact>>() {
            @Override
            public void onLoadComplete(final List<PhoneContact> phoneContacts) {
                final ArrayList<RemoteContact> remoteContacts = new ArrayList<>();
                final DatabaseReference contactListReference = FirebaseDatabase.getInstance().getReference().child("users")
                        .child(getCurrentUserKey())
                        .child("contacts");
                contactListReference.removeValue();
                for(final PhoneContact phoneContact: phoneContacts) {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
                    Query query = databaseReference.orderByChild("phone").equalTo(phoneContact.getPhoneNumber());

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists())
                                for(DataSnapshot childSnapshot: dataSnapshot.getChildren())
                                    remoteContacts.add(new RemoteContact(childSnapshot.getKey(),phoneContact.getName()));

                            if(phoneContacts.indexOf(phoneContact) >= phoneContacts.size()-1 ) {
                                contactListReference.updateChildren(RemoteContact.listToHashMap(remoteContacts));
                                loadContactList(remoteContacts,listener);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }
            }


        });

    }

    private void loadPhoneContactList(Context context, OnLoadCompleteListener<List<PhoneContact>> listener){
        String[] projection = {
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
        };
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,null,null);

        HashMap<String, PhoneContact> contacts = new HashMap<>();

        if(cursor == null)
            listener.onLoadComplete(new ArrayList<PhoneContact>());

        while(cursor.moveToNext()) {
            String id = cursor.getString(0),
                    name = cursor.getString(1),
                    phone = cursor.getString(2);


            if (!contacts.containsKey(id)){
                PhoneContact contactInfo = new PhoneContact(name,phone);
                contacts.put(id,contactInfo);
            }
        }
        cursor.close();
        listener.onLoadComplete(new ArrayList<>(contacts.values()));
    }
    public void loadContactList(final OnLoadCompleteListener<List<Contact>> listener){
        loadRemoteContactList(new OnLoadCompleteListener<List<RemoteContact>>() {

            @Override
            public void onLoadComplete(List<RemoteContact> remoteContacts) {
                loadContactList(remoteContacts,listener);
            }
        });
    }

    private void loadContactList(final List<RemoteContact> remoteContacts,final OnLoadCompleteListener<List<Contact>> listener) {
        final ArrayList<Contact> contacts = new ArrayList<>();
        if(remoteContacts.isEmpty())
            listener.onLoadComplete(contacts);
        else {
            for(final RemoteContact remoteContact: remoteContacts){
                FirebaseDatabase.getInstance().getReference().child("users")
                        .child(remoteContact.getUserKey()).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    String  about = "", pfpUrl = "";
                                    if(dataSnapshot.hasChild("about"))
                                        about = dataSnapshot.child("about").getValue().toString();

                                    if(dataSnapshot.hasChild("pfp"))
                                        pfpUrl = dataSnapshot.child("pfp").getValue().toString();

                                    Contact user = new Contact(remoteContact.getUserKey(), remoteContact.getName(),about,pfpUrl);
                                    contacts.add(user);
                                    if(remoteContacts.indexOf(remoteContact) >= remoteContacts.size()-1)
                                        listener.onLoadComplete(contacts);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        }
                );
            }
        }

    }

    private void loadChat(final List<RemoteContact> remoteContacts, final RemoteChat chat,
                          final ChatEventListener listener){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("chats").child(chat.getChatKey());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){


                    boolean isLastMessageOwn = false;
                    String messageText = "";
                    long timestamp = 0;
                    if(dataSnapshot.hasChild("last_message"))
                        messageText = dataSnapshot.child("last_message").getValue().toString();

                    if(dataSnapshot.hasChild("timestamp"))
                        timestamp = Long.parseLong(dataSnapshot.child("timestamp").getValue().toString());

                    if(dataSnapshot.hasChild("sender"))
                        isLastMessageOwn = dataSnapshot.child("sender").getValue().toString().equals(getCurrentUserKey());

                    final String finalMessageText = messageText;
                    final long finalTimestamp = timestamp;
                    final boolean finalIsLastMessageOwn = isLastMessageOwn;
                    FirebaseDatabase.getInstance().getReference().child("users").child(chat.getUserKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            String title = "", pfpUrl = "";

                            RemoteContact contact = RemoteContact.getContactByUserKey(remoteContacts,chat.getUserKey());
                            if(contact != null){
                                title = contact.getName();
                            }else if(dataSnapshot.hasChild("phone")){
                                String phone = dataSnapshot.child("phone").getValue().toString();
                                title = Utils.formatNumberCompat(phone);
                            }
                            if(dataSnapshot.hasChild("pfp")) pfpUrl = dataSnapshot.child("pfp").getValue().toString();
                            Chat item = new Chat(chat.getChatKey(), chat.getUserKey(), title, finalMessageText, pfpUrl, finalTimestamp, finalIsLastMessageOwn);
                            listener.onNewChat(item);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void updateChat(String chatKey, final ChatEventListener listener){
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("chats").child(chatKey);
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    boolean isLastMessageOwn = false;
                    String messageText = "";
                    long timestamp = 0;
                    if(dataSnapshot.hasChild("last_message"))
                        messageText = dataSnapshot.child("last_message").getValue().toString();

                    if(dataSnapshot.hasChild("timestamp"))
                        timestamp = Long.parseLong(dataSnapshot.child("timestamp").getValue().toString());

                    if(dataSnapshot.hasChild("sender"))
                        isLastMessageOwn = dataSnapshot.child("sender").getValue().toString().equals(getCurrentUserKey());

                    listener.onChatUpdated(dataSnapshot.getKey(),messageText,timestamp,isLastMessageOwn);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }


    public void addChatEventListener(final List<RemoteContact> remoteContacts, final ChatEventListener listener){
        final DatabaseReference chatListReference = FirebaseDatabase.getInstance().getReference().child("users").child(getCurrentUserKey()).child("chats");
        chatListReference.keepSynced(true);
        chatListReference.orderByChild("timestamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()) {
                    RemoteChat chat = new RemoteChat(dataSnapshot.getKey(),dataSnapshot.getValue().toString());
                    loadChat(remoteContacts,chat,listener);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    updateChat(dataSnapshot.getKey(),listener);
                }
                Log.i("CHILD","onChildChanged");
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                listener.onElementRemoved(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

    }

    public void loadMessageList(final Context context, final List<RemoteContact> remoteContacts, String remoteChatKey, final OnLoadCompleteListener<Message> onLoadCompleteListener){
        FirebaseDatabase.getInstance().getReference().child("messages").child(remoteChatKey).
                addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()) {
                    String senderUid = "", senderName = "", text= "";
                    String media = "";
                    long timestamp=0;

                    if(dataSnapshot.child("text").getValue() != null)
                        text = dataSnapshot.child("text").getValue().toString();
                    if(dataSnapshot.child("timestamp").getValue() !=null)
                        timestamp = (long)dataSnapshot.child("timestamp").getValue();
                    if(dataSnapshot.child("media").getValue() != null)
                        media = dataSnapshot.child("media").getValue().toString();

                    if(dataSnapshot.child("sender").getValue() !=null)
                        senderUid = dataSnapshot.child("sender").getValue().toString();

                    boolean loadNow = true;
                    if(senderUid.equals(getCurrentUserKey())){
                        senderName = context.getString(R.string.you);
                    }else if(!senderUid.isEmpty()){
                        RemoteContact contact = RemoteContact.getContactByUserKey(remoteContacts,senderUid);
                        if(contact != null){
                            senderName = contact.getName();
                        }
                        else{
                            loadNow = false;
                            final String finalSenderUid = senderUid;
                            final String finalText = text;
                            final long finalTimestamp = timestamp;
                            final String finalMedia = media;
                            getUserPhoneNumber(senderUid, new OnLoadCompleteListener<String>() {
                                @Override
                                public void onLoadComplete(String phone) {
                                    Message message = new Message(dataSnapshot.getKey(), finalSenderUid, phone,
                                            finalText, finalTimestamp, finalMedia);
                                    onLoadCompleteListener.onLoadComplete(message);
                                }
                            });
                        }
                    }

                    if(loadNow){
                        Message message = new Message(dataSnapshot.getKey(),senderUid,senderName,text,timestamp,media);
                        onLoadCompleteListener.onLoadComplete(message);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }
    private void getUserPhoneNumber(String userKey, final OnLoadCompleteListener<String> listener){
        FirebaseDatabase.getInstance().getReference().child("users").child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("phone")){
                        String phone = dataSnapshot.child("phone").getValue().toString();
                        listener.onLoadComplete(phone);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void sendMessage(final String chatKey, final String userKey, final String text, final Uri imageUri, final OnSuccessListener<RemoteMessage> listener){
        if(text == null && imageUri == null)
            return;
        final DatabaseReference messageListReference = FirebaseDatabase.getInstance().getReference().child("messages").child(chatKey);
        final DatabaseReference messageReference = messageListReference.push();
        String messageKey = messageReference.getKey();
        final long timestamp = new Date().getTime();
        if(imageUri !=null) {
            uploadImage(chatKey, messageKey, imageUri, new OnLoadCompleteListener<String>() {
                @Override
                public void onLoadComplete(String uri) {
                    final RemoteMessage remoteMessage = new RemoteMessage(getCurrentUserKey(), null,uri,timestamp);
                    uploadMessage(chatKey, userKey, messageReference,remoteMessage,listener);
                }
            });
        }else if(!text.isEmpty()){
            final RemoteMessage remoteMessage = new RemoteMessage(getCurrentUserKey(), text,null,timestamp);
            uploadMessage(chatKey,userKey, messageReference,remoteMessage,listener);
        }
    }
    private void uploadMessage(final String chatKey, final String userKey, final DatabaseReference messageReference,
                               final RemoteMessage message,
                               final OnSuccessListener<RemoteMessage> listener){
        messageReference.setValue(message);
        final DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference().child("chats").child(chatKey);
        HashMap<String,Object> chatMap = new HashMap<>();
        chatMap.put("timestamp",message.getTimestamp());
        chatMap.put("last_message",message.getMessageText());
        chatMap.put("sender",message.getSenderKey());
        chatReference.updateChildren(chatMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                FirebaseDatabase.getInstance().getReference().child("users").child(getCurrentUserKey()).child("chats").child(chatKey).setValue("");
                FirebaseDatabase.getInstance().getReference().child("users").child(userKey).child("chats").child(chatKey).setValue("");


                FirebaseDatabase.getInstance().getReference().child("users").child(getCurrentUserKey()).child("chats").child(chatKey).setValue(userKey);
                FirebaseDatabase.getInstance().getReference().child("users").child(userKey).child("chats").child(chatKey).setValue(getCurrentUserKey());

                listener.onSuccess(message);
            }
        });
    }

    private void uploadImage(String chatKey, String messageKey, Uri uri, final OnLoadCompleteListener<String> listener){
        final StorageReference mediaReference = FirebaseStorage.getInstance().getReference().child("chats").child(chatKey).child(messageKey);
        final UploadTask uploadTask = mediaReference.putFile(uri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mediaReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        listener.onLoadComplete(uri.toString());
                    }
                });
            }
        });
    }



    public void getNotificationKeyFromUser(String userKey, final OnLoadCompleteListener<String> listener){
        FirebaseDatabase.getInstance().getReference().child("users").child(userKey).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            if(dataSnapshot.hasChild("notification_key")){
                                String notificationKey = dataSnapshot.child("notification_key").getValue().toString();
                                listener.onLoadComplete(notificationKey);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                }
        );
    }


    public void getChatKeyWithUser(final String userKey, final OnLoadCompleteListener<String> listener){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(getCurrentUserKey()).child("chats");
        Query query = databaseReference.orderByValue().equalTo(userKey);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String chatKey="";
                if(!dataSnapshot.exists()){
                    chatKey = FirebaseDatabase.getInstance().getReference().child("chats").push().getKey();

                    FirebaseDatabase.getInstance().getReference().child("users").child(getCurrentUserKey()).child("chats").child(chatKey).setValue(userKey);
                }else {
                    for(DataSnapshot childSnapshot: dataSnapshot.getChildren()){
                        chatKey = childSnapshot.getKey();
                        break;
                    }
                }
                listener.onLoadComplete(chatKey);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

}
