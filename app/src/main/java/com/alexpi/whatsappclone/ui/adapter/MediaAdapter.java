package com.alexpi.whatsappclone.ui.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alexpi.whatsappclone.R;
import com.alexpi.whatsappclone.listeners.OnItemClickListener;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder>{
    private ArrayList<Uri> itemList;
    private OnItemClickListener itemClickListener;

    private OnItemClickListener deleteButtonClickListener;
    private Context context;

    public MediaAdapter(Context context, ArrayList<Uri> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setDeleteButtonOnClickListener(OnItemClickListener deleteButtonClickListener) {
        this.deleteButtonClickListener = deleteButtonClickListener;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_media,parent,false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MediaViewHolder holder, final int position) {
        Glide.with(context).load(itemList.get(position)).centerCrop().into(holder.mediaIV);
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteButtonClickListener.onItemClick(view,holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class MediaViewHolder extends RecyclerView.ViewHolder {
        ImageView mediaIV;
        FloatingActionButton deleteButton;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            mediaIV = itemView.findViewById(R.id.media_iv);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
