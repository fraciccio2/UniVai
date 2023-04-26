package com.example.carsharing.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.deeplabstudio.fcmsend.FCMSend;
import com.example.carsharing.R;
import com.example.carsharing.databinding.ActivityBookRideBinding;
import com.example.carsharing.enums.StatusEnum;
import com.example.carsharing.models.AddressModel;
import com.example.carsharing.models.RequestRideModel;
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
import java.util.Date;

public class BookRideActivity extends AppCompatActivity {

    ActivityBookRideBinding binding;
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseRides;
    DatabaseReference mDatabaseUsers;
    DatabaseReference mDatabaseTokens;
    String rideId;
    String userId;
    String location;
    UserModel logUser;

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
        if(user != null) {
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
        if(extras != null) {
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
        mDatabaseRides.orderByKey().equalTo(rideId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot datas: snapshot.getChildren()){
                        AddressModel address = datas.child("address").getValue(AddressModel.class);
                        String date = datas.child("date").getValue(String.class);
                        String note = datas.child("note").getValue(String.class);
                        binding.rideAddress.setText(address.getLocation());
                        SimpleDateFormat formatter = new SimpleDateFormat(getString(R.string.date_pattern));
                        binding.rideDeparture.setText(formatter.format(new Date(date)));
                        binding.rideNote.setText(note);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error", "exception", error.toException());
            }
        });
    }

    private void saveRequestForRide() {
        binding.bookButton.setOnClickListener(view -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if(user != null) {
                if(binding.otherLocationButton.isChecked()) {
                    if(location != null) {
                        if(logUser.getUserImage() == null || logUser.getUserImage().equals("")){
                            AlertDialog.Builder builder = new AlertDialog.Builder(BookRideActivity.this);
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
                            RequestRideModel requestRide = new RequestRideModel(StatusEnum.PENDING, userId, user.getUid(), rideId, location);
                            FirebaseDatabase.getInstance().getReference("requests").push().setValue(requestRide).addOnCompleteListener(task -> {
                                if(task.isSuccessful()) {
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
                                    Intent intent = new Intent(getApplicationContext(), RidesListActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(getApplicationContext(), getString(R.string.error_retry_text), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.insert_address_text), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    RequestRideModel requestRide = new RequestRideModel(StatusEnum.PENDING, userId, user.getUid(), rideId);
                    FirebaseDatabase.getInstance().getReference("requests").push().setValue(requestRide).addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            Intent intent = new Intent(getApplicationContext(), RidesListActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.error_retry_text), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void someLocationButton() {
        binding.someLocationButton.setOnClickListener(view ->{
            if(binding.someLocationButton.isChecked()) {
                binding.autocompleteCard.setVisibility(View.GONE);
            }
        });
    }

    private void otherLocationButton() {
        binding.otherLocationButton.setOnClickListener(view ->{
            if(binding.otherLocationButton.isChecked()) {
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
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}