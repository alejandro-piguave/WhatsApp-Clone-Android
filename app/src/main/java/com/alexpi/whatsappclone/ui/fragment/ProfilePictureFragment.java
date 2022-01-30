package com.alexpi.whatsappclone.ui.fragment;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import com.alexpi.whatsappclone.R;
import com.alexpi.whatsappclone.utils.Utils;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ProfilePictureFragment extends Fragment {

    private AppCompatImageView profileImage;
    private FloatingActionButton profileButton;

    private final static int GALLERY_IMAGE_REQUEST_CODE = 3;
    private final int CAMERA_CAPTURE_REQUEST_CODE = 1;

    private DatabaseReference pfpDBReference;
    private StorageReference pfpStorageReference;

    private Uri mCropImageUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_picture,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileImage = view.findViewById(R.id.pfp_img);
        profileButton = view.findViewById(R.id.pfp_button);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setProfilePicture();
            }
        });
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        loadProfilePicture();
    }

    private void loadProfilePicture() {
        Glide.with(getContext()).load(R.drawable.default_profile).into(profileImage);
        //Picasso.get().load(R.drawable.default_profile).noPlaceholder().centerCrop().fit().into(profileImage);

        pfpStorageReference = FirebaseStorage.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        pfpDBReference = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("pfp");
        pfpDBReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String pfpUrl = dataSnapshot.getValue().toString();
                    Glide.with(getContext()).load(pfpUrl).placeholder(R.drawable.default_profile).fitCenter().into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void setProfilePicture() {
        new AlertDialog.Builder(getContext()).setItems(R.array.pfp_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch (which){
                    case 0:
                        openGallery();
                        break;
                    case 1:
                        if (CropImage.isExplicitCameraPermissionRequired(getContext()))
                            requestPermissions(new String[]{Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
                         else openCamera();
                        break;
                    case 2:
                        deletePicture();
                        break;
                }
            }
        }).show();
    }
    private void deletePicture(){
        pfpStorageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Glide.with(getContext()).load(R.drawable.default_profile).fitCenter().into(profileImage);
                pfpDBReference.removeValue();

            }
        });
    }
    private void performCropping(Uri uri){
        CropImage.activity(uri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(getContext(),ProfilePictureFragment.this);
    }

    private void openGallery(){
        List<Intent> intents = CropImage.getGalleryIntents(getContext().getPackageManager(),Intent.ACTION_GET_CONTENT,false);
        startActivityForResult(intents.get(0),GALLERY_IMAGE_REQUEST_CODE);

    }
    private void openCamera(){
        Intent captureIntent = CropImage.getCameraIntent(getContext(),null);
        startActivityForResult(captureIntent, CAMERA_CAPTURE_REQUEST_CODE);
    }

    private void uploadImage(final Uri imageUri){
        if(Utils.isNetworkAvailable(getContext())) {
            final AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(R.layout.loading_dialog_layout).create();
            dialog.show();
            UploadTask uploadTask = pfpStorageReference.putFile(imageUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    pfpStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            pfpDBReference.setValue(uri.toString());

                            Glide.with(getContext()).load(imageUri)
                                    .placeholder(R.drawable.default_profile).into(profileImage);
                            //Picasso.get().load(imageUri).noPlaceholder().centerCrop().fit().into(profileImage);
                            dialog.dismiss();
                            Toast.makeText(getContext(), R.string.image_uploaded, Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            });
        }else Toast.makeText(getContext(), R.string.no_network, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if(mCropImageUri != null && requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE)
                performCropping(mCropImageUri);
            else if(requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE)
                openCamera();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == CAMERA_CAPTURE_REQUEST_CODE){
                Uri uri = CropImage.getCaptureImageOutputUri(getContext());
                performCropping(uri);
            }else if(requestCode == GALLERY_IMAGE_REQUEST_CODE){
                Uri uri = CropImage.getPickImageResultUri(getContext(),data);
                if (CropImage.isReadExternalStoragePermissionsRequired(getContext(), uri)) {
                    mCropImageUri = uri;
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
                } else performCropping(uri);
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Uri imageUri = result.getUri();
                uploadImage(imageUri);

            }
        }
    }


}
