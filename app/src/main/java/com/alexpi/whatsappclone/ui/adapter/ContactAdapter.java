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
import com.alexpi.whatsappclone.data.local.Contact;
import com.alexpi.whatsappclone.listeners.OnItemClickListener;
import com.bumptech.glide.Glide;
import com.vanniktech.emoji.EmojiTextView;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder>{
    private List<Contact> contactList;
    private OnItemClickListener onItemClickListener;
    private Context context;

    public ContactAdapter(Context context, List<Contact> contactList) {
        this.contactList = contactList;
        this.context = context;
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setList(List<Contact> contactList) {
        this.contactList = contactList;
    }

    public List<Contact> getList() {
        return contactList;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact,parent,false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, final int position) {
        holder.bind(contactList.get(position));
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        EmojiTextView subtitleTV;
        TextView titleTV;
        ImageView pfpView;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTV = itemView.findViewById(R.id.title_tv);
            subtitleTV = itemView.findViewById(R.id.lastMsgTV);
            pfpView = itemView.findViewById(R.id.pfp);
        }
        public void bind(Contact item){
            titleTV.setText(item.getName());
            subtitleTV.setText(item.getAboutInfo());
            if(onItemClickListener !=null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onItemClickListener.onItemClick(view,getAdapterPosition());
                    }
                });
            }
            if(item.getLogo().isEmpty())
                Glide.with(context).load(R.drawable.default_profile).fitCenter().into(pfpView);
            else Glide.with(context).load(item.getLogo()).placeholder(R.drawable.default_profile).fitCenter().into(pfpView);

        }
    }
}
