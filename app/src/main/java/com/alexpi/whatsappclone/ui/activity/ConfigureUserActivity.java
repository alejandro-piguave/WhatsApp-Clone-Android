package com.alexpi.whatsappclone.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alexpi.whatsappclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class ConfigureUserActivity extends AppCompatActivity {

    private EditText userNameET;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_user);

        userNameET = findViewById(R.id.etUserName);
    }

    public void createUser(View view) {
        if(userNameET.getText().toString().isEmpty()){
            Toast.makeText(this, R.string.enter_valid_name, Toast.LENGTH_SHORT).show();
        }else {
            FirebaseDatabase.getInstance().getReference().child("users").
                    child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                    child("name").setValue(userNameET.getText().toString());

            startActivity(new Intent(ConfigureUserActivity.this, MainActivity.class));
            finish();
        }
    }
}
