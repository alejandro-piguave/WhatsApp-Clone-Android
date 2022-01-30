package com.alexpi.whatsappclone.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alexpi.whatsappclone.R;
import com.alexpi.whatsappclone.data.local.MediaInfo;
import com.alexpi.whatsappclone.data.local.Message;
import com.alexpi.whatsappclone.listeners.OnItemClickListener;
import com.alexpi.whatsappclone.utils.Utils;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter{
    private List<Message> messages;
    private OnItemClickListener onItemClickListener;
    private Context context;

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    public MessageAdapter(Context context, List<Message> itemList) {
        this.messages = itemList;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public List<Message> getList() {
        return messages;
    }

    @Override
    public int getItemViewType(int position) {
        if(messages.get(position).getSenderUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
            return VIEW_TYPE_MESSAGE_SENT;
        else return VIEW_TYPE_MESSAGE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_outgoing_message, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_incoming_message, parent, false);
            return new ReceivedMessageHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()){
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder)holder).bind(messages.get(position));
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder)holder).bind(messages.get(position));
                break;
            default:
                return;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }


    private class SentMessageHolder extends RecyclerView.ViewHolder {
        EmojiTextView messageText;
        TextView timeText;
        ImageView mediaView;
        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.msg_body_tv);
            timeText =  itemView.findViewById(R.id.timestamp_tv);
            mediaView = itemView.findViewById(R.id.media_view);
        }
        void bind(final Message message) {
            timeText.setText(Utils.format1(message.getTimestamp()));

            if (!message.getMessage().isEmpty()) {
                messageText.setVisibility(View.VISIBLE);
                messageText.setText(message.getMessage());
            } else messageText.setVisibility(View.GONE);

            if (!message.getMedia().isEmpty()) {
                mediaView.setVisibility(View.VISIBLE);
                if(onItemClickListener != null){
                    mediaView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onItemClickListener.onItemClick(mediaView,getAdapterPosition());
                        }
                    });
                }
                Glide.with(context).load(message.getMedia()).placeholder(R.drawable.no_picture).centerCrop().into(mediaView);

            } else
                mediaView.setVisibility(View.GONE);
        }

    }
    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        EmojiTextView messageText;
        TextView timeText;
        ImageView mediaView;
        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.msg_body_tv);
            timeText = itemView.findViewById(R.id.timestamp_tv);
            mediaView = itemView.findViewById(R.id.media_view);
        }
        void bind(final Message message) {
            timeText.setText(Utils.format1(message.getTimestamp()));

            if(!message.getMessage().isEmpty()){
                messageText.setVisibility(View.VISIBLE);
                messageText.setText(message.getMessage());
            }else messageText.setVisibility(View.GONE);

            if(!message.getMedia().isEmpty()){
                mediaView.setVisibility(View.VISIBLE);
                if(onItemClickListener != null){
                    mediaView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onItemClickListener.onItemClick(mediaView,getAdapterPosition());
                        }
                    });
                }
                Glide.with(context).load(message.getMedia()).placeholder(R.drawable.no_picture).centerCrop().into(mediaView);

            }else
                mediaView.setVisibility(View.GONE);

        }
    }

    public ArrayList<MediaInfo> getMediaInfo(){
        ArrayList<MediaInfo> mediaUris = new ArrayList<>();

        for (Message message: messages){
            if(message.getMedia() != null && !message.getMedia().isEmpty()){
                MediaInfo mediaInfo = new MediaInfo(message.getMedia(),message.getSenderName(),message.getTimestamp());
                mediaUris.add(mediaInfo);

            }
        }
        return mediaUris;
    }
}

