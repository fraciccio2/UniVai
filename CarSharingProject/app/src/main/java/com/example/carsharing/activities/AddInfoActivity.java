package com.example.carsharing.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.carsharing.R;
import com.example.carsharing.databinding.ActivityAddInfoBinding;
import com.example.carsharing.models.UserModel;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

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
    String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Places.initialize(getApplicationContext(), getString(R.string.api_key));

        adapterItems = new ArrayAdapter<String>(this, R.layout.list_item, items);
        binding.autoCompleteText.setAdapter(adapterItems);
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.start_address);
        if (autocompleteFragment != null) {
            autocompleteFragment.setCountry("it");
            autocompleteFragment.setHint(getString(R.string.search_text));
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onError(@NonNull Status status) {
                    Log.i("ERROR", "An error occurred: " + status);
                }

                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    address = place.getAddress();
                }
            });
        }

        binding.startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        });
    }
}