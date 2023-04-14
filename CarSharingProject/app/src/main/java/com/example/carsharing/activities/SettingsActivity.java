package com.example.carsharing.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.carsharing.R;
import com.example.carsharing.databinding.ActivitySettingsBinding;
import com.example.carsharing.models.UserModel;
import com.example.carsharing.services.NavigationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    ActivitySettingsBinding binding;
    FirebaseAuth mAuth;
    NavigationHelper navigationHelper = new NavigationHelper();
    DatabaseReference mDatabaseUsers;
    UserModel logUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.bottomNavigationView.setSelectedItemId(R.id.action_settings);
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(getString(R.string.settings_text));
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference("users");
        navigationHelper.floatButtonOnClick(binding.floatingButton, getApplicationContext());
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            getLoggedUser(user);
        }
        List<CharSequence> languages = new ArrayList<>();
        languages.add(getString(R.string.italian_text));
        languages.add(getString(R.string.english_text));
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerLanguage.setAdapter(adapter);
    }

    private void getLoggedUser(FirebaseUser user) {
        mDatabaseUsers.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    logUser = snapshot.getValue(UserModel.class);
                    navigationHelper.navigate(binding.bottomNavigationView, getApplicationContext());
                    navigationHelper.hideButton(binding.floatingButton, binding.bottomNavigationView, logUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error", "exception", error.toException());
            }
        });
    }
}