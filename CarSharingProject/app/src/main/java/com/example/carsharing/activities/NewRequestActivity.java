package com.example.carsharing.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.carsharing.R;
import com.example.carsharing.databinding.ActivityNewRequestBinding;
import com.example.carsharing.models.AddressModel;
import com.example.carsharing.models.RequestModel;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class NewRequestActivity extends AppCompatActivity {

    ActivityNewRequestBinding binding;
    AddressModel address;
    String text;
    String date;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
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
                    address = new AddressModel(place.getAddress(), place.getLatLng());
                }
            });
        }

        binding.newRequestTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                DatePickerDialog mDatePicker;
                mDatePicker = new DatePickerDialog(NewRequestActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
                        TimePickerDialog mTimePicker;
                        mTimePicker = new TimePickerDialog(NewRequestActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                calendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute);
                                date = calendar.getTime().toString();
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.date_pattern));
                                text = simpleDateFormat.format(calendar.getTime());
                                binding.newRequestTime.setText(text);
                            }
                        }, hour, minute, true);
                        mTimePicker.setTitle(getString(R.string.new_request_time_text));
                        mTimePicker.show();
                    }
                }, year, month, day);
                mDatePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                mDatePicker.setTitle(getString(R.string.new_request_time_text));
                mDatePicker.show();
            }
        });

        binding.newRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNewRequest();
            }
        });
    }

    private void saveNewRequest() {
        String note = binding.newRequestNote.getText().toString();
        if(address.getLocation() != null && date != null) {
            RequestModel request = new RequestModel(address, date, note, true);
            FirebaseUser user = mAuth.getCurrentUser();
            if(user != null) {
                mDatabase.child("requests").child(user.getUid()).setValue(request);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        } else {
            Toast.makeText(this, "Inserisci tutti i campi necessari", Toast.LENGTH_SHORT).show();
        }
    }
}