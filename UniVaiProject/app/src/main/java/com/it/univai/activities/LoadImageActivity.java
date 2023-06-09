package com.it.univai.activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.it.univai.R;
import com.it.univai.databinding.ActivityLoadImageBinding;
import com.it.univai.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import taimoor.sultani.sweetalert2.Sweetalert;

public class LoadImageActivity extends AppCompatActivity {

    ActivityLoadImageBinding binding;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    private Uri imageUri;
    Sweetalert alert;
    final private StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoadImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(getString(R.string.load_image_text));

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        alert = new Sweetalert(this, Sweetalert.PROGRESS_TYPE);
        alert.getProgressHelper().setBarColor(getResources().getColor(R.color.main_color));
        alert.setTitleText(getString(R.string.loading_text));
        alert.setCancelable(false);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            editMode(extras);
        }

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        imageUri = data.getData();
                        binding.uploadImage.setImageURI(imageUri);
                        binding.continueButton.setVisibility(View.VISIBLE);
                        binding.continueButton.setEnabled(true);
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
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });

        binding.continueButton.setOnClickListener(view -> {
            if (imageUri != null) {
                uploadToFirebase(imageUri);
            }
        });
    }

    private void uploadToFirebase(Uri uri) {
        alert.show();
        final StorageReference imageReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(uri));
        imageReference.putFile(uri).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                imageReference.getDownloadUrl().addOnCompleteListener(taskUri -> {
                    if(taskUri.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Map<String, Object> updateUser = new HashMap<>();
                            updateUser.put("userImage", taskUri.getResult().toString());
                            mDatabase.child(user.getUid()).updateChildren(updateUser);
                            alert.dismiss();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        alert.dismiss();
                    }
                });
            } else {
                alert.dismiss();
            }
        });
    }

    private String getFileExtension(Uri fileUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(fileUri));
    }

    private void editMode(Bundle extras) {
        boolean editMode = extras.getBoolean(getString(R.string.edit_mode_text));
        if (editMode) {
            alert.show();
            binding.skipButton.setVisibility(View.INVISIBLE);
            binding.skipButton.setEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mDatabase.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        UserModel user = snapshot.getValue(UserModel.class);
                        if(user != null && user.getUserImage() != null) {
                            Glide.with(getApplicationContext()).load(user.getUserImage()).into(binding.uploadImage);
                        } else {
                            binding.uploadImage.setImageResource(R.drawable.upload_image);
                        }
                        alert.dismiss();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Error", "exception", error.toException());
                    alert.dismiss();
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}