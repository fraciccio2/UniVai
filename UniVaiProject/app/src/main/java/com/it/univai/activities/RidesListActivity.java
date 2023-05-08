package com.it.univai.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.it.univai.R;
import com.it.univai.adapters.RideAdapter;
import com.it.univai.databinding.ActivityRidesListBinding;
import com.it.univai.helpers.NavigationHelper;
import com.it.univai.helpers.RidesHelper;
import com.it.univai.models.AddressModel;
import com.it.univai.models.LatLonModel;
import com.it.univai.models.RideModel;
import com.it.univai.models.RideWithUserModel;
import com.it.univai.models.UserModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import taimoor.sultani.sweetalert2.Sweetalert;

public class RidesListActivity extends AppCompatActivity {

    ActivityRidesListBinding binding;
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseRides;
    DatabaseReference mDatabaseUsers;
    NavigationHelper navigationHelper = new NavigationHelper();
    RidesHelper ridesHelper = new RidesHelper();
    UserModel logUser;
    AddressModel address;
    TextView autocompleteText;
    double radius = 10;
    int i = 0, l = 0;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRidesListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        mDatabaseRides = FirebaseDatabase.getInstance().getReference("rides");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference("users");
        binding.bottomNavigationView.setSelectedItemId(R.id.action_search);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            getLoggedUser(user);
        }

        Places.initialize(getApplicationContext(), getString(R.string.api_key));

        navigationHelper.floatButtonOnClick(binding.floatingButton, getApplicationContext());
        getRides();
        openFilterBottomSheets();
    }

    private void getRides() {
        Sweetalert alert = new Sweetalert(this, Sweetalert.PROGRESS_TYPE);
        alert.getProgressHelper().setBarColor(getResources().getColor(R.color.main_color));
        alert.setTitleText(getString(R.string.loading_text));
        alert.setCancelable(false);
        alert.show();
        mDatabaseRides.orderByChild("active").equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshotData) {
                if (snapshotData.exists()) {
                    List<RideWithUserModel> rideUserList = new ArrayList<>();
                    for (DataSnapshot ignored : snapshotData.getChildren()) {
                        i++;
                    }
                    for (DataSnapshot data : snapshotData.getChildren()) {
                        RideModel ride = data.getValue(RideModel.class);
                        mDatabaseUsers.orderByKey().equalTo(ride.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                l++;
                                if (snapshot.exists()) {
                                    String name = "";
                                    String surname = "";
                                    String userImage = "";
                                    for (DataSnapshot datas : snapshot.getChildren()) {
                                        name = datas.child("name").getValue(String.class);
                                        surname = datas.child("surname").getValue(String.class);
                                        userImage = datas.child("userImage").getValue(String.class);
                                    }
                                    RideWithUserModel rideUser = new RideWithUserModel(
                                            data.getKey(),
                                            ride.getAddress(),
                                            ride.getDate(),
                                            ride.getNote(),
                                            ride.getActive(),
                                            name,
                                            surname,
                                            userImage,
                                            ride.getUserId()
                                    );
                                    if (!ride.getUserId().equals(mAuth.getCurrentUser().getUid())) {
                                        if (ridesHelper.distanceBetweenLatLong(
                                                rideUser.getAddress().getCoordinate().getLatitude(),
                                                rideUser.getAddress().getCoordinate().getLongitude(),
                                                address.getCoordinate().getLatitude(),
                                                address.getCoordinate().getLongitude())
                                                < radius
                                        ) {
                                            rideUserList.add(rideUser);
                                        }
                                    }
                                }
                                if (i == l) {
                                    alert.dismiss();
                                    if (rideUserList.size() > 0) {
                                        binding.noRidesCard.setVisibility(View.GONE);
                                        binding.filterButton.setVisibility(View.VISIBLE);
                                    } else {
                                        binding.noRidesCard.setVisibility(View.VISIBLE);
                                        binding.filterButton.setVisibility(View.GONE);
                                        warningRidesAlert();
                                    }
                                    RecyclerView recyclerView = binding.recyclerView;
                                    LinearLayoutManager layoutManager = new LinearLayoutManager(RidesListActivity.this);
                                    recyclerView.setLayoutManager(layoutManager);
                                    Collections.reverse(rideUserList);
                                    RideAdapter adapter = new RideAdapter(rideUserList, getApplicationContext(), RidesListActivity.this);
                                    recyclerView.setAdapter(adapter);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("Error", "exception", error.toException());
                                alert.dismiss();
                            }
                        });
                    }
                } else {
                    alert.dismiss();
                    binding.noRidesCard.setVisibility(View.VISIBLE);
                    binding.filterButton.setVisibility(View.GONE);
                    warningRidesAlert();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error", "exception", error.toException());
                alert.dismiss();
            }
        });
    }

    private void getLoggedUser(FirebaseUser user) {
        mDatabaseUsers.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    logUser = snapshot.getValue(UserModel.class);
                    address = logUser.getAddress();
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

    private void warningRidesAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RidesListActivity.this);
        builder.setTitle(getString(R.string.ops_text));
        builder.setMessage(getString(R.string.warning_rides_text));
        builder.setNeutralButton(getString(R.string.neutral_button_text), (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void openFilterBottomSheets() {
        binding.filterButton.setOnClickListener(view -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(RidesListActivity.this, R.style.BottomSheetDialogTheme);
            View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bottom_sheet_layout, findViewById(R.id.bottom_sheet_linear_layout));
            MaterialButton filterButton = bottomSheetView.findViewById(R.id.filter_button);
            createGoogleAutocomplete(bottomSheetView);
            filterButton.setOnClickListener(view1 -> {
                getRides();
                bottomSheetDialog.dismiss();
            });
            initializeSeekBar(bottomSheetView);
            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
        });
    }

    private void createGoogleAutocomplete(View bottomSheetView) {
        autocompleteText = bottomSheetView.findViewById(R.id.autocomplete_text);
        autocompleteText.setText(address.getLocation());
        autocompleteText.setOnClickListener(view -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .build(getApplicationContext());
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        });
    }

    private void initializeSeekBar(View bottomSheetView) {
        SeekBar seekBar = bottomSheetView.findViewById(R.id.seekbar_distance);
        TextView distanceText = bottomSheetView.findViewById(R.id.text_view_radius);
        distanceText.setText(Math.round(radius) + "Km");
        seekBar.setProgress(((int) radius) - 1);
        int dpSize = 16;
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        float marginSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpSize, dm);
        seekBar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                float value = seekBar.getProgress();
                float currentPosition = (seekBar.getWidth() - marginSize) / seekBar.getMax();
                currentPosition = currentPosition * value;
                distanceText.setX(currentPosition);
                seekBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float currentPosition = (seekBar.getWidth() - marginSize) / seekBar.getMax();
                currentPosition = currentPosition * i;
                distanceText.setX(currentPosition);
                distanceText.setText("" + (i + 1) + "Km");
                radius = i + 1;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                address = new AddressModel(place.getAddress(), new LatLonModel(place.getLatLng().latitude, place.getLatLng().longitude));
                autocompleteText.setText(address.getLocation());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("ERROR", "An error occurred: " + status.getStatusMessage());
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}