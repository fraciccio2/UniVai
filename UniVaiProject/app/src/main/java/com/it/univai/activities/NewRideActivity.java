package com.it.univai.activities;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.it.univai.R;
import com.it.univai.databinding.ActivityNewRideBinding;
import com.it.univai.models.AddressModel;
import com.it.univai.models.LatLonModel;
import com.it.univai.models.RideModel;
import com.it.univai.models.UserModel;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class NewRideActivity extends AppCompatActivity {

    ActivityNewRideBinding binding;
    AddressModel address;
    String text;
    String date;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    UserModel logUser;
    RideModel savedRide;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final int CALENDAR_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewRideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.new_ride_title_text));
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            getLoggedUser(user);
        }
        Places.initialize(getApplicationContext(), getString(R.string.api_key));

        googleAutoComplete();

        binding.newRideAddressCheck.setOnClickListener(view -> {
            if(binding.newRideAddressCheck.isChecked()){
                binding.newRideAddress.setText(null);
                binding.newRideAddress.setHint(logUser.getAddress().getLocation());
                address = logUser.getAddress();
            } else {
                binding.newRideAddress.setHint(getString(R.string.new_ride_address_text));
                address = null;
            }
        });

        setDateAndTime();

        binding.newRideButton.setOnClickListener(view -> saveNewRide(user));
    }

    private void saveNewRide(FirebaseUser user) {
        String note = binding.newRideNote.getText().toString();
        if(address != null && date != null && user != null) {
            if(logUser.getUserImage() == null || logUser.getUserImage().equals("")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewRideActivity.this);
                builder.setTitle(getString(R.string.attention_title_text));
                builder.setMessage(getString(R.string.new_ride_warning_text));
                builder.setNegativeButton(getString(R.string.dont_insert_text), (dialog, which) -> dialog.dismiss());
                builder.setPositiveButton(getString(R.string.insert_text), (dialog, which) -> {
                    Intent intent = new Intent(getApplicationContext(), LoadImageActivity.class);
                    intent.putExtra(getString(R.string.edit_mode_text), true);
                    startActivity(intent);
                });
                builder.show();
            } else {
                RideModel ride = new RideModel(user.getUid(), address, date, note, true);
                mDatabase.child("rides").push().setValue(ride).addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        savedRide = ride;
                        AlertDialog.Builder builder = new AlertDialog.Builder(NewRideActivity.this);
                        builder.setTitle(getString(R.string.attention_title_text));
                        builder.setMessage(getString(R.string.add_on_calendar_text));
                        builder.setNegativeButton(getString(R.string.dont_insert_text), (dialog, which) -> {
                            dialog.dismiss();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        });
                        builder.setPositiveButton(getString(R.string.insert_text), (dialog, which) -> {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            checkPermission(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR);
                        });
                        builder.show();
                    }
                });
            }
        } else {
            Toast.makeText(this, getString(R.string.all_field_required_text), Toast.LENGTH_SHORT).show();
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
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setDateAndTime() {
        binding.newRideTime.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            DatePickerDialog mDatePicker;
            mDatePicker = new DatePickerDialog(NewRideActivity.this, (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(NewRideActivity.this, (timePicker, selectedHour, selectedMinute) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute);
                    date = calendar.getTime().toString();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.date_pattern));
                    text = simpleDateFormat.format(calendar.getTime());
                    binding.newRideTime.setText(text);
                }, hour, minute, true);
                mTimePicker.setTitle(getString(R.string.new_ride_time_text));
                mTimePicker.show();
            }, year, month, day);
            mDatePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            mDatePicker.setTitle(getString(R.string.new_ride_time_text));
            mDatePicker.show();
        });
    }

    private void googleAutoComplete() {
        binding.newRideAddress.setOnClickListener(view -> {
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
                binding.newRideAddress.setText(address.getLocation());
                binding.newRideAddressCheck.setChecked(false);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("ERROR", "An error occurred: " + status.getStatusMessage());
            }
            return;
        }
        if(requestCode == CALENDAR_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                createEventOnCalendar();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void createEventOnCalendar() {
        if(savedRide != null) {
            long startTime = new Date(savedRide.getDate()).getTime();
            long endTime = startTime + 3600000;
            Intent intent = new Intent(Intent.ACTION_EDIT);
            intent.setType("vnd.android.cursor.item/event");
            intent.putExtra("beginTime", startTime);
            intent.putExtra("endTime", endTime);
            intent.putExtra("title", getString(R.string.app_name));
            intent.putExtra("description", savedRide.getNote());
            intent.putExtra("eventLocation", savedRide.getAddress().getLocation());
            intent.putExtra("eventTimezone", TimeZone.getDefault().getID());
            startActivity(intent);
        }
    }

    private void checkPermission(String... permissionsId) {
        boolean permissions = true;
        for (String p : permissionsId) {
            permissions = permissions && ContextCompat.checkSelfPermission(this, p) == PERMISSION_GRANTED;
        }
        if (!permissions) {
            ActivityCompat.requestPermissions(this, permissionsId, CALENDAR_REQUEST_CODE);
        } else {
            createEventOnCalendar();
        }
    }
}