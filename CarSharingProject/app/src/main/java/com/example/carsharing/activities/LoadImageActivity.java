package com.example.carsharing.activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.carsharing.R;
import com.example.carsharing.databinding.ActivityLoadImageBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class LoadImageActivity extends AppCompatActivity {

    ActivityLoadImageBinding binding;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    private Uri imageUri;
    final private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    final static String anonymousUser = "https://firebasestorage.googleapis.com/v0/b/car-sharing-b39c3.appspot.com/o/user-spy.png?alt=media&token=dece854f-d20a-4aed-98f9-4976fbdcb2fc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoadImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        imageUri = data.getData();
                        binding.uploadImage.setImageURI(imageUri);
                        binding.continueButton.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(LoadImageActivity.this, getString(R.string.no_image_selected_text), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        binding.uploadImage.setOnClickListener(view -> {
            Intent photoPicker = new Intent();
            photoPicker.setAction(Intent.ACTION_GET_CONTENT);
            photoPicker.setType("image/*");
            activityResultLauncher.launch(photoPicker);
        });

        binding.skipButton.setOnClickListener(view -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if(user != null) {
                Map<String, Object> updateUser = new HashMap<>();
                updateUser.put("userImage", anonymousUser);
                mDatabase.child(user.getUid()).updateChildren(updateUser);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        binding.continueButton.setOnClickListener(view -> {
            if (imageUri != null) {
                uploadToFirebase(imageUri);
            }
        });
    }

    private void uploadToFirebase(Uri uri) {
        final StorageReference imageReference = storageReference.child(System.currentTimeMillis() + "."+ getFileExtension(uri));
        imageReference.putFile(uri).addOnSuccessListener(taskSnapshot -> imageReference.getDownloadUrl().addOnSuccessListener(uriDownloaded -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if(user != null) {
                Map<String, Object> updateUser = new HashMap<>();
                updateUser.put("userImage", uriDownloaded.toString());
                mDatabase.child(user.getUid()).updateChildren(updateUser);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        }));
    }

    private String getFileExtension(Uri fileUri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(fileUri));
    }
}