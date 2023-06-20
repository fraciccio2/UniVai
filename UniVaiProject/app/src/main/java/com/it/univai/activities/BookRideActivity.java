package com.it.univai.activities;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.deeplabstudio.fcmsend.FCMSend;
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
import com.it.univai.R;
import com.it.univai.databinding.ActivityBookRideBinding;
import com.it.univai.enums.StatusEnum;
import com.it.univai.models.AddressModel;
import com.it.univai.models.RequestRideModel;
import com.it.univai.models.RideModel;
import com.it.univai.models.UserModel;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import taimoor.sultani.sweetalert2.Sweetalert;

public class BookRideActivity extends AppCompatActivity {

    ActivityBookRideBinding binding;
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseRides;
    DatabaseReference mDatabaseUsers;
    DatabaseReference mDatabaseTokens;
    String rideId;
    String userId;
    String location;
    String formattedDate;
    AddressModel address;
    UserModel logUser;
    RideModel savedRide;
    private static final int CALENDAR_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookRideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FCMSend.SetServerKey(getString(R.string.server_key));

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRides = FirebaseDatabase.getInstance().getReference("rides");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference("users");
        mDatabaseTokens = FirebaseDatabase.getInstance().getReference("registrationToken");

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(getString(R.string.book_ride_text));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            getLoggedUser(user);
        }

        Places.initialize(getApplicationContext(), getString(R.string.api_key));
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.other_location);
        if (autocompleteFragment != null) {
            autocompleteFragment.setCountry("it");
            autocompleteFragment.setHint(getString(R.string.search_address_text));
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS));
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onError(@NonNull Status status) {
                    Log.i("ERROR", "An error occurred: " + status);
                }

                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    location = place.getAddress();
                }
            });
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            rideId = extras.getString(getString(R.string.ride_id_text));
            userId = extras.getString(getString(R.string.user_id_text));
            String userName = extras.getString(getString(R.string.user_name_text));
            binding.creatorUser.setText(userName);
            getRide();
        }

        saveRequestForRide();
        someLocationButton();
        otherLocationButton();
    }

    private void getRide() {
        Sweetalert alert = new Sweetalert(this, Sweetalert.PROGRESS_TYPE);
        alert.getProgressHelper().setBarColor(getResources().getColor(R.color.main_color));
        alert.setTitleText(getString(R.string.loading_text));
        alert.setCancelable(false);
        alert.show();
        mDatabaseRides.orderByKey().equalTo(rideId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot datas : snapshot.getChildren()) {
                        address = datas.child("address").getValue(AddressModel.class);
                        String date = datas.child("date").getValue(String.class);
                        String note = datas.child("note").getValue(String.class);
                        binding.rideAddress.setText(address.getLocation());
                        SimpleDateFormat formatter = new SimpleDateFormat(getString(R.string.date_pattern));
                        formattedDate = formatter.format(new Date(date));
                        binding.rideDeparture.setText(formattedDate);
                        binding.rideNote.setText(note);
                        savedRide = new RideModel(address, date, note);
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

    private void saveRequestForRide() {
        binding.bookButton.setOnClickListener(view -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                if (binding.otherLocationButton.isChecked()) {
                    if (location != null) {
                        if (logUser.getUserImage() == null || logUser.getUserImage().equals("")) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(BookRideActivity.this);
                            builder.setTitle(getString(R.string.attention_title_text));
                            builder.setMessage(getString(R.string.book_ride_warning_text));
                            builder.setNegativeButton(getString(R.string.dont_insert_text), (dialog, which) -> dialog.dismiss());
                            builder.setPositiveButton(getString(R.string.insert_text), (dialog, which) -> {
                                Intent intent = new Intent(getApplicationContext(), LoadImageActivity.class);
                                intent.putExtra(getString(R.string.edit_mode_text), true);
                                startActivity(intent);
                            });
                            builder.show();
                        } else {
                            RequestRideModel requestRide = new RequestRideModel(StatusEnum.PENDING, userId, user.getUid(), rideId, location, false);
                            FirebaseDatabase.getInstance().getReference("requests").push().setValue(requestRide).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    saveEventOnCalendarAndSendNotification();
                                } else {
                                    Toast.makeText(getApplicationContext(), getString(R.string.error_retry_text), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.insert_address_text), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (logUser.getUserImage() == null || logUser.getUserImage().equals("")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(BookRideActivity.this);
                        builder.setTitle(getString(R.string.attention_title_text));
                        builder.setMessage(getString(R.string.book_ride_warning_text));
                        builder.setNegativeButton(getString(R.string.dont_insert_text), (dialog, which) -> dialog.dismiss());
                        builder.setPositiveButton(getString(R.string.insert_text), (dialog, which) -> {
                            Intent intent = new Intent(getApplicationContext(), LoadImageActivity.class);
                            intent.putExtra(getString(R.string.edit_mode_text), true);
                            startActivity(intent);
                        });
                        builder.show();
                    } else {
                        String location = address.getLocation() + " " + formattedDate;
                        RequestRideModel requestRide = new RequestRideModel(StatusEnum.PENDING, userId, user.getUid(), rideId, location, true);
                        FirebaseDatabase.getInstance().getReference("requests").push().setValue(requestRide).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                saveEventOnCalendarAndSendNotification();
                            } else {
                                Toast.makeText(getApplicationContext(), getString(R.string.error_retry_text), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });
    }

    private void someLocationButton() {
        binding.someLocationButton.setOnClickListener(view -> {
            if (binding.someLocationButton.isChecked()) {
                binding.autocompleteCard.setVisibility(View.GONE);
            }
        });
    }

    private void otherLocationButton() {
        binding.otherLocationButton.setOnClickListener(view -> {
            if (binding.otherLocationButton.isChecked()) {
                binding.autocompleteCard.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getLoggedUser(FirebaseUser user) {
        mDatabaseUsers.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
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

    private void createEventOnCalendar() {
        if (savedRide != null) {
            long startTime = new Date(savedRide.getDate()).getTime();
            long endTime = startTime + 3600000;
            Intent intent = new Intent(Intent.ACTION_EDIT);
            intent.setType("vnd.android.cursor.item/event");
            intent.putExtra("beginTime", startTime);
            intent.putExtra("endTime", endTime);
            intent.putExtra("title", getString(R.string.app_name));
            intent.putExtra("description", savedRide.getNote());
            intent.putExtra("eventLocation", address.getLocation());
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

    private void saveEventOnCalendarAndSendNotification() {
        mDatabaseTokens.orderByValue().equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        FCMSend.Builder builder = new FCMSend.Builder(data.getKey())
                                .setTitle(getString(R.string.app_name))
                                .setBody(getString(R.string.interested_message_text));
                        builder.send();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error", "exception", error.toException());
            }
        });
        Toast.makeText(this, getString(R.string.request_success_text), Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(BookRideActivity.this);
        builder.setTitle(getString(R.string.attention_title_text));
        builder.setMessage(getString(R.string.add_on_calendar_text));
        builder.setNegativeButton(getString(R.string.dont_insert_text), (dialog, which) -> {
            dialog.dismiss();
            Intent intent = new Intent(getApplicationContext(), RidesListActivity.class);
            startActivity(intent);
        });
        builder.setPositiveButton(getString(R.string.insert_text), (dialog, which) -> {
            Intent intent = new Intent(getApplicationContext(), RidesListActivity.class);
            startActivity(intent);
            checkPermission(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR);
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CALENDAR_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                createEventOnCalendar();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}