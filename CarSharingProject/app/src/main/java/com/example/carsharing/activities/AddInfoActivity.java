package com.example.carsharing.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.carsharing.R;
import com.example.carsharing.databinding.ActivityAddInfoBinding;
import com.example.carsharing.models.AddressModel;
import com.example.carsharing.models.LatLonModel;
import com.example.carsharing.models.UserModel;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import taimoor.sultani.sweetalert2.Sweetalert;

public class AddInfoActivity extends AppCompatActivity {

    ActivityAddInfoBinding binding;
    String[] items = {
            "Giurisprudenza", "Economia e Impresa", "Scienze Politiche e Sociali", "Scienze Umanitarie",
            "Scienze della Formazione", "Scuola di Medicina", "Scienze del Farmaco e della Formazione", "Agricoltura, Alimentazione e Ambiente, Igneria Civile e Architettura",
            "Igneria Elettrica, Elettronica e Informatica", "Fisica e Astronomia", "Matematica e Informatica",
            "Scienza Biologiche, Geologiche e Ambientali", "Scienze Chimiche", "Scuola Superiore di Catania", "Scuola di Lingua e Cultura Italiana per Stranieri"
    };
    ArrayAdapter<String> adapterItems;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    AddressModel address;
    boolean editMode = false;
    UserModel logUser;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Places.initialize(getApplicationContext(), getString(R.string.api_key));

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(getString(R.string.start_text));

        adapterItems = new ArrayAdapter<>(this, R.layout.list_item, items);
        binding.autoCompleteText.setAdapter(adapterItems);

        googleAutoComplete();

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            editMode(extras);
        }

        addDataToUserAction();
    }

    private void addDataToUserAction() {
        binding.startButton.setOnClickListener(view -> {
            if(!editMode) {
                String name = binding.startName.getText().toString();
                String surname = binding.startSurname.getText().toString();
                String university = binding.autoCompleteText.getText().toString();
                Boolean hasCar = binding.startCar.isChecked();
                if (name.equals("") || surname.equals("") || university.equals("") || address.equals("")) {
                    Toast.makeText(AddInfoActivity.this, "Inserisci tutti i campi necessari", Toast.LENGTH_SHORT).show();
                } else {
                    if (mAuth.getCurrentUser() != null) {
                        UserModel user = new UserModel(name, surname, address, university, hasCar);
                        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).setValue(user);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    }
                }
            } else {
                Map<String, Object> updateUser = new HashMap<>();
                if (logUser.getHasCar() != binding.startCar.isChecked()) {
                    updateUser.put("hasCar", binding.startCar.isChecked());
                }
                if(address != null && !(logUser.getAddress().getLocation().equals(address.getLocation()))) {
                    updateUser.put("address", address);
                }
                if(!(logUser.getName().equals(binding.startName.getText().toString()))) {
                    updateUser.put("name", binding.startName.getText().toString());
                }
                if(!(logUser.getSurname().equals(binding.startSurname.getText().toString()))) {
                    updateUser.put("surname", binding.startSurname.getText().toString());
                }
                if(!(logUser.getUniversity().equals(binding.autoCompleteText.getText().toString()))) {
                    updateUser.put("university", binding.autoCompleteText.getText().toString());
                }
                mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).updateChildren(updateUser);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void editMode(Bundle extras) {
        boolean editMode = extras.getBoolean(getString(R.string.edit_mode_text));
        if (editMode) {
            Sweetalert alert = new Sweetalert(this, Sweetalert.PROGRESS_TYPE);
            alert.getProgressHelper().setBarColor(getResources().getColor(R.color.main_color));
            alert.setTitleText(getString(R.string.loading_text));
            alert.setCancelable(false);
            alert.show();
            this.editMode = true;
            binding.startButton.setText(getString(R.string.edit_button_text));
            getSupportActionBar().setTitle(getString(R.string.edit_button_text));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        logUser = snapshot.getValue(UserModel.class);
                        binding.startName.setText(logUser.getName());
                        binding.startSurname.setText(logUser.getSurname());
                        binding.startAddress.setText(logUser.getAddress().getLocation());
                        binding.autoCompleteText.setText(logUser.getUniversity(), false);
                        binding.startCar.setChecked(logUser.getHasCar());
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

    private void googleAutoComplete() {
        binding.startAddress.setOnClickListener(view -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .build(getApplicationContext());
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                address = new AddressModel(place.getAddress(), new LatLonModel(place.getLatLng().latitude, place.getLatLng().longitude));
                binding.startAddress.setText(address.getLocation());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("ERROR", "An error occurred: " + status.getStatusMessage());
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}