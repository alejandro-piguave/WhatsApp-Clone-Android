package com.alexpi.whatsappclone.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alexpi.whatsappclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText et1,et2,et3,et4,et5,et6;
    private Button actionButton;
    private CountryCodePicker picker;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    private String mVerificationID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }


        EditText numberET = findViewById(R.id.numberET);
        actionButton = findViewById(R.id.next_button);

        initOtpView();

        picker = findViewById(R.id.ccp);
        picker.registerCarrierNumberEditText(numberET);

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                System.out.println("ERROR");
                e.printStackTrace();
            }

            @Override
            public void onCodeSent(@NonNull String verificationID, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationID, forceResendingToken);
                mVerificationID = verificationID;
                actionButton.setText(R.string.verify_code);
                otpViewSetEnabled(true);
            }

        };
    }

    private void otpViewSetEnabled(boolean enabled) {
        et1.setEnabled(enabled);
        et2.setEnabled(enabled);
        et3.setEnabled(enabled);
        et4.setEnabled(enabled);
        et5.setEnabled(enabled);
        et6.setEnabled(enabled);

        TextView textView = findViewById(R.id.enter_code_tv);
        if(enabled)
            textView.setVisibility(View.VISIBLE);
        else textView.setVisibility(View.INVISIBLE);
    }

    private void initOtpView() {
        et1 = findViewById(R.id.et1);
        et2 = findViewById(R.id.et2);
        et3 = findViewById(R.id.et3);
        et4 = findViewById(R.id.et4);
        et5 = findViewById(R.id.et5);
        et6 = findViewById(R.id.et6);

        et1.addTextChangedListener(new GenericTextWatcher(et1));
        et2.addTextChangedListener(new GenericTextWatcher(et2));
        et3.addTextChangedListener(new GenericTextWatcher(et3));
        et4.addTextChangedListener(new GenericTextWatcher(et4));
        et5.addTextChangedListener(new GenericTextWatcher(et5));
        et6.addTextChangedListener(new GenericTextWatcher(et6));
        otpViewSetEnabled(false);
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    userIsLoggedIn();
                }
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, "Verification failed", Toast.LENGTH_SHORT).show();
                System.out.println("FAIL");
                e.printStackTrace();
            }
        });
    }

    private void userIsLoggedIn() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null) {
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }else{
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("phone",user.getPhoneNumber());
                        userMap.put("about",getString(R.string.default_about));

                        databaseReference.updateChildren(userMap);
                        startActivity(new Intent(LoginActivity.this, ConfigureUserActivity.class));
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
    }

    public void next(View view) {
        if(picker.isValidFullNumber()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.verification_dialog_message,picker.getFormattedFullNumber()))
                    .setNeutralButton(R.string.edit,null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startNumberVerification();
                        }
                    }).setCancelable(false)
                    .show();


        }else Toast.makeText(LoginActivity.this, R.string.invalid_phone_number, Toast.LENGTH_SHORT).show();
    }
    private void verifyPhoneNumberWithCode(){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationID,getFullCode());
        signInWithPhoneAuthCredential(credential);
    }

    private String getFullCode(){
        return et1.getText().toString()+et2.getText().toString()+et3.getText().toString()+
                et4.getText().toString()+et5.getText().toString()+et6.getText().toString();
    }
    private void startNumberVerification() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                picker.getFullNumberWithPlus(),
                120,
                TimeUnit.SECONDS,
                this,
                callbacks
        );
    }
    public class GenericTextWatcher implements TextWatcher
    {
        private View view;
        private GenericTextWatcher(View view)
        {
            this.view = view;
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // TODO Auto-generated method stub
            String text = editable.toString();
            switch(view.getId()) {
                case R.id.et1:
                    if(text.length()==1)
                        et2.requestFocus();
                    break;
                case R.id.et2:
                    if(text.length()==1)
                        et3.requestFocus();
                    else if(text.length()==0)
                        et1.requestFocus();
                    break;
                case R.id.et3:
                    if(text.length()==1)
                        et4.requestFocus();
                    else if(text.length()==0)
                        et2.requestFocus();
                    break;
                case R.id.et4:
                    if(text.length()==1)
                        et5.requestFocus();
                    else if(text.length()==0)
                        et3.requestFocus();
                    break;
                case R.id.et5:
                    if(text.length()==1)
                        et6.requestFocus();
                    else if(text.length()==0)
                        et4.requestFocus();
                    break;
                case R.id.et6:
                    if(text.length()==0)
                        et5.requestFocus();
                    else if(text.length()==1){
                        verifyPhoneNumberWithCode();
                        otpViewSetEnabled(false);
                    }
                    break;

            }
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
        }
    }
}
