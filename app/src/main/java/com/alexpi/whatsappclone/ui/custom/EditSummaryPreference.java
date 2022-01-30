package com.alexpi.whatsappclone.ui.custom;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.EditTextPreference;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class EditSummaryPreference extends EditTextPreference {

    public EditSummaryPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public EditSummaryPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EditSummaryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditSummaryPreference(Context context) {
        super(context);
    }

    @Override
    public CharSequence getSummary() {
        return getText();
    }


    @Override
    public void setText(String text) {
        super.setText(text);
        updateInDatabase(getKey(), text);
    }

    private void updateInDatabase(String key, String newValue){
        switch (key){
            case "name":
            case "about":
                FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(key).setValue(newValue);
                break;
        }
    }
}