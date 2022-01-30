package com.alexpi.whatsappclone.ui.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.alexpi.whatsappclone.R;
import com.alexpi.whatsappclone.ui.fragment.ProfilePictureFragment;
import com.alexpi.whatsappclone.ui.fragment.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.pfp_layout,new ProfilePictureFragment(),"FRAGMENT_PFP").commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.preference_container,new SettingsFragment(),"FRAGMENT_SETTINGS").commit();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
