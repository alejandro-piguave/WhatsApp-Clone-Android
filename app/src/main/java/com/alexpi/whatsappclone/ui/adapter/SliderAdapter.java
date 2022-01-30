package com.alexpi.whatsappclone.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.alexpi.whatsappclone.R;
import com.alexpi.whatsappclone.data.local.MediaInfo;
import com.bumptech.glide.Glide;

import java.util.List;

public class SliderAdapter extends PagerAdapter {

    private List<MediaInfo> mediaInfoList;

    public SliderAdapter( List<MediaInfo> mediaInfoList) {
        this.mediaInfoList = mediaInfoList;
    }

    @Override
    public int getCount() {
        return mediaInfoList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.image_slider_layout_item, container, false);
        ImageView imageView = layout.findViewById(R.id.iv_auto_image_slider);
        Glide.with(container.getContext()).load(mediaInfoList.get(position).getMediaUri()).into(imageView);
        container.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mediaInfoList.get(position).getSenderName();
    }
}