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
import com.alexpi.whatsappclone.listeners.OnItemClickListener;
import com.alexpi.whatsappclone.data.local.Chat;
import com.bumptech.glide.Glide;
import com.vanniktech.emoji.EmojiTextView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder>{
    private List<Chat> chatList;
    private OnItemClickListener onItemClickListener;
    private Context context;

    public ChatAdapter(Context context, List<Chat> chatList) {
        this.chatList = chatList;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public List<Chat> getList() {
        return chatList;
    }

    public void add(Chat chat){
        chatList.add(chat);
        notifyItemInserted(chatList.size()-1);
    }
    public void update(String chatKey, String lastMessage, long timestamp, boolean isLastMessageOwn){
        int index = getChatIndexByKey(chatKey);
        if(index != -1){
            Chat outdatedChat = chatList.remove(index);
            outdatedChat.setLastMessageInfo(lastMessage,timestamp,isLastMessageOwn);
            chatList.add(outdatedChat);
            notifyItemMoved(index,chatList.size()-1);
            notifyItemChanged(chatList.size()-1);
        }
    }
    public void remove(String key){
        int index = getChatIndexByKey(key);
        if(index != -1){
            chatList.remove(index);
            notifyItemRemoved(index);
        }
    }
    private int getChatIndexByKey(String key){
        for(Chat chat: chatList)
            if(chat.getKey().equals(key))
                return chatList.indexOf(chat);
        return -1;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat,parent,false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, final int position) {
        holder.bind(chatList.get(position));
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        EmojiTextView subtitleTV;
        TextView titleTV, timestampText;
        ImageView pfpView, doubleCheckView;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTV = itemView.findViewById(R.id.title_tv);
            subtitleTV = itemView.findViewById(R.id.lastMsgTV);
            pfpView = itemView.findViewById(R.id.pfp);
            doubleCheckView = itemView.findViewById(R.id.double_check_iv);
            timestampText = itemView.findViewById(R.id.lastMsgTimestampTV);
        }
        public void bind(Chat chat){
            titleTV.setText(chat.getName());
            if(chat.getLastMessage().isEmpty())
                subtitleTV.setText(context.getString(R.string.image_message));
            else subtitleTV.setText(chat.getLastMessage());
            timestampText.setText(chat.getTimestampText(context));
            if(chat.isLastMessageOwn())
                doubleCheckView.setVisibility(View.VISIBLE);
            else doubleCheckView.setVisibility(View.GONE);
            if(onItemClickListener !=null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(getAdapterPosition() != -1)
                            onItemClickListener.onItemClick(view,getAdapterPosition());
                    }
                });
            }
            if(chat.getPicture().isEmpty())
                Glide.with(context).load(R.drawable.default_profile).fitCenter().into(pfpView);
            else Glide.with(context).load(chat.getPicture()).placeholder(R.drawable.default_profile).fitCenter().into(pfpView);


        }
    }
}
