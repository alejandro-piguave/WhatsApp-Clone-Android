package com.alexpi.whatsappclone.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;


import android.os.Bundle;
import android.view.MenuItem;

import com.alexpi.whatsappclone.R;
import com.alexpi.whatsappclone.data.local.MediaInfo;
import com.alexpi.whatsappclone.ui.adapter.SliderAdapter;
import com.alexpi.whatsappclone.utils.Utils;

import java.util.ArrayList;

public class MediaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        ViewPager sliderView = findViewById(R.id.viewPager);
        if(getIntent().getExtras() != null){
            int index = getIntent().getExtras().getInt("index",0);
            final ArrayList<MediaInfo> mediaInfoList = getIntent().getExtras().getParcelableArrayList("mediaList");
            sliderView.setAdapter(new SliderAdapter(mediaInfoList));
            sliderView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
               @Override
               public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

               @Override
               public void onPageSelected(int position) {
                   if(getSupportActionBar() != null){
                       getSupportActionBar().setTitle(mediaInfoList.get(position).getSenderName());
                       getSupportActionBar().setSubtitle(Utils.format3(mediaInfoList.get(position).getTimestamp()));
                   }
               }

               @Override
               public void onPageScrollStateChanged(int state) { }
            });
            sliderView.setCurrentItem(index);
        }

        ViewPager viewPager = new ViewPager(this);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if(getSupportActionBar() != null){
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }

}
