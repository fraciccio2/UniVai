package com.example.carsharing.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.carsharing.R;
import com.example.carsharing.adapters.LanguageSpinnerAdapter;
import com.example.carsharing.databinding.ActivitySettingsBinding;
import com.example.carsharing.models.UserModel;
import com.example.carsharing.services.DataBaseHelper;
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
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    ActivitySettingsBinding binding;
    FirebaseAuth mAuth;
    NavigationHelper navigationHelper = new NavigationHelper();
    DatabaseReference mDatabaseUsers;
    UserModel logUser;
    DataBaseHelper dataBaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.bottomNavigationView.setSelectedItemId(R.id.action_settings);

        dataBaseHelper = new DataBaseHelper(getApplicationContext());

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

        populateLanguageSpinner();
        logOutAction();
        changeUserImageAction();
        editUserDataAction();
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

    private void logOutAction () {
        binding.logoutAction.setOnClickListener(view -> {
            mAuth.signOut();
            dataBaseHelper.dropTable();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void populateLanguageSpinner () {
        List<String> languages = new ArrayList<>();
        languages.add(getString(R.string.italian_text));
        languages.add(getString(R.string.english_text));
        LanguageSpinnerAdapter adapter = new LanguageSpinnerAdapter(getApplicationContext(), android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(R.layout.language_dropdown_item);
        binding.spinnerLanguage.setAdapter(adapter);
        String language = Locale.getDefault().getLanguage();
        if(language.equals("en")) {
            binding.spinnerLanguage.setSelection(1);
        }
        changeLanguage();
    }

    private void changeLanguage() {
        binding.spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Locale locale;
                if(i == 0) {
                    locale = new Locale("it");
                } else {
                    locale = new Locale("en");
                }
                if(!(Locale.getDefault().getLanguage().equals(locale.getLanguage()))){
                    Locale.setDefault(locale);
                    Configuration config = new Configuration();
                    getResources().updateConfiguration(config, getResources().getDisplayMetrics());
                    recreate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.i("Info", "Nothing");
            }
        });
    }

    private void changeUserImageAction() {
        binding.changeImageAction.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), LoadImageActivity.class);
            intent.putExtra(getString(R.string.edit_mode_text), true);
            startActivity(intent);
        });
    }

    private void editUserDataAction() {
        binding.editDataAction.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), AddInfoActivity.class);
            intent.putExtra(getString(R.string.edit_mode_text), true);
            startActivity(intent);
        });
    }
}