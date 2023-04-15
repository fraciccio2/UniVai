package com.example.carsharing.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.carsharing.R;
import com.example.carsharing.databinding.ActivityNewRequestBinding;
import com.example.carsharing.models.AddressModel;
import com.example.carsharing.models.LatLonModel;
import com.example.carsharing.models.RequestModel;
import com.example.carsharing.models.UserModel;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

public class NewRequestActivity extends AppCompatActivity {

    ActivityNewRequestBinding binding;
    AddressModel address;
    String text;
    String date;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    UserModel logUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.new_request_title_text));
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            getLoggedUser(user);
        }
        Places.initialize(getApplicationContext(), getString(R.string.api_key));

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.new_request_address);
        if (autocompleteFragment != null) {
            autocompleteFragment.setCountry("it");
            autocompleteFragment.setHint(getString(R.string.new_request_address_text));
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onError(@NonNull Status status) {
                    Log.i("ERROR", "An error occurred: " + status);
                }

                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    address = new AddressModel(place.getAddress(), new LatLonModel(place.getLatLng().latitude, place.getLatLng().longitude));
                }
            });
        }

        binding.newRequestAddressCheck.setOnClickListener(view -> {
            if(binding.newRequestAddressCheck.isChecked()){
                autocompleteFragment.setHint(logUser.getAddress().getLocation());
                address = logUser.getAddress();
            } else {
                autocompleteFragment.setHint(getString(R.string.new_request_address_text));
                address = null;
            }
        });

        binding.newRequestTime.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            DatePickerDialog mDatePicker;
            mDatePicker = new DatePickerDialog(NewRequestActivity.this, (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(NewRequestActivity.this, (timePicker, selectedHour, selectedMinute) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute);
                    date = calendar.getTime().toString();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.date_pattern));
                    text = simpleDateFormat.format(calendar.getTime());
                    binding.newRequestTime.setText(text);
                }, hour, minute, true);
                mTimePicker.setTitle(getString(R.string.new_request_time_text));
                mTimePicker.show();
            }, year, month, day);
            mDatePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            mDatePicker.setTitle(getString(R.string.new_request_time_text));
            mDatePicker.show();
        });

        binding.newRequestButton.setOnClickListener(view -> saveNewRequest(user));
    }

    private void saveNewRequest(FirebaseUser user) {
        String note = binding.newRequestNote.getText().toString();
        if(address != null && date != null && user != null) {
            RequestModel request = new RequestModel(user.getUid(), address, date, note, true);
            mDatabase.child("requests").push().setValue(request);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Inserisci tutti i campi necessari", Toast.LENGTH_SHORT).show();
        }
    }

    private void getLoggedUser(FirebaseUser user) {
        mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    logUser = snapshot.getValue(UserModel.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error", "exception", error.toException());
            }
        });
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