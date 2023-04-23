package com.example.carsharing.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.carsharing.R;
import com.example.carsharing.databinding.ActivityMainBinding;
import com.example.carsharing.models.RideModel;
import com.example.carsharing.models.RideWithUserModel;
import com.example.carsharing.models.UserModel;
import com.example.carsharing.services.NavigationHelper;
import com.example.carsharing.services.RidesHelper;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int PERMISSIONS_REQUEST_LOCATION = 100;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    ActivityMainBinding binding;
    GoogleMap gMap;
    FusedLocationProviderClient providerClient;
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseUsers;
    LatLng userLatLng;
    double radius = 1;
    List<RideWithUserModel> rideUserList = new ArrayList<>();
    UserModel logUser;
    NavigationHelper navigationHelper = new NavigationHelper();
    RidesHelper ridesHelper = new RidesHelper();
    TextView autocompleteText;
    String location, tmpLocation;
    int i = 0, l = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference("users");
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
            } else {
                getLoggedUser(user);
            }
        }
        binding.bottomNavigationView.setSelectedItemId(R.id.action_map);
        providerClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.main_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Places.initialize(getApplicationContext(), getString(R.string.api_key));
        openFilterBottomSheets();

        navigationHelper.navigate(binding.bottomNavigationView, getApplicationContext());
        navigationHelper.floatButtonOnClick(binding.floatingButton, getApplicationContext());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.loadMap();
            } else {
                setMapWithDefaultValue(false);
            }
        }
    }

    private void setMapWithDefaultValue(boolean showAlert) {
        if (showAlert) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(getString(R.string.ops_text));
            builder.setMessage(getString(R.string.warning_location_text));
            builder.setNeutralButton(getString(R.string.neutral_button_text), (dialog, which) -> dialog.dismiss());
            builder.show();
        }
        if (logUser != null) {
            userLatLng = new LatLng(logUser.getAddress().getCoordinate().getLatitude(), logUser.getAddress().getCoordinate().getLongitude());
        } else {
            userLatLng = new LatLng(41.90370, 12.49524);
        }
        getRides();
    }

    @SuppressLint("MissingPermission")
    private void loadMap() {
        providerClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                getRides();
            } else {
                setMapWithDefaultValue(true);
            }
        });

        providerClient.getLastLocation().addOnFailureListener(e -> {
            setMapWithDefaultValue(true);
            Log.e("Error", "exception", e);
        });
    }

    private void setMap(double lat, double lon) {
        LatLng mapItaly = new LatLng(lat, lon);
        gMap.addMarker(new MarkerOptions().position(mapItaly).zIndex(2.0f));
        for (RideWithUserModel ride : rideUserList) {
            gMap.addMarker(new MarkerOptions().position(
                            new LatLng(ride.getAddress().getCoordinate().getLatitude(), ride.getAddress().getCoordinate().getLongitude()))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).zIndex(1.0f));
        }
        gMap.moveCamera(CameraUpdateFactory.newLatLng(mapItaly));
        gMap.setMinZoomPreference(5);
        gMap.setLatLngBoundsForCameraTarget(new LatLngBounds(new LatLng(36.6199, 6.7499), new LatLng(47.1153, 18.4802)));
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mapItaly, 14));
        clickOnMarker();
        navigateToBookRideActivity();
    }

    private void getRides() {
        FirebaseDatabase.getInstance().getReference("rides").orderByChild("active").equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ignored : snapshot.getChildren()) {
                        i++;
                    }
                    for (DataSnapshot data : snapshot.getChildren()) {
                        RideModel rideModel = data.getValue(RideModel.class);
                        mDatabaseUsers.orderByKey().equalTo(rideModel.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                l++;
                                if(snapshot.exists()){
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
                                            rideModel.getAddress(),
                                            rideModel.getDate(),
                                            rideModel.getNote(),
                                            rideModel.getActive(),
                                            name,
                                            surname,
                                            userImage,
                                            rideModel.getUserId()
                                    );
                                    if (
                                            ridesHelper.distanceBetweenLatLong(
                                                    rideUser.getAddress().getCoordinate().getLatitude(),
                                                    rideUser.getAddress().getCoordinate().getLongitude(),
                                                    userLatLng.latitude,
                                                    userLatLng.longitude
                                            ) < radius &&
                                                    !rideUser.getUserId().equals(mAuth.getCurrentUser().getUid())
                                    ) {
                                        rideUserList.add(rideUser);
                                    }
                                }
                                if (i == l) {
                                    Log.i("Tag", "enter");
                                    if (rideUserList.size() == 0) {
                                        warningRidesAlert();
                                    }
                                    setMap(userLatLng.latitude, userLatLng.longitude);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("Error", "exception", error.toException());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error", "exception", error.toException());
            }
        });
    }

    private void getLoggedUser(FirebaseUser user) {
        mDatabaseUsers.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    logUser = snapshot.getValue(UserModel.class);
                    navigationHelper.hideButton(binding.floatingButton, binding.bottomNavigationView, logUser);
                    loadMap();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error", "exception", error.toException());
            }
        });
    }

    private void warningRidesAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.ops_text));
        builder.setMessage(getString(R.string.warning_rides_text));
        builder.setNeutralButton(getString(R.string.neutral_button_text), (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void openFilterBottomSheets() {
        binding.filterButton.setOnClickListener(view -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this, R.style.BottomSheetDialogTheme);
            View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bottom_sheet_layout, findViewById(R.id.bottom_sheet_linear_layout));
            MaterialButton filterButton = bottomSheetView.findViewById(R.id.filter_button);
            createGoogleAutocomplete(bottomSheetView);
            filterButton.setOnClickListener(view1 -> {
                location = tmpLocation;
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
        if (location != null) {
            autocompleteText.setText(location);
        } else {
            if (userLatLng != null && userLatLng.latitude == logUser.getAddress().getCoordinate().getLatitude() && userLatLng.longitude == logUser.getAddress().getCoordinate().getLongitude()) {
                autocompleteText.setText(logUser.getAddress().getLocation());
            }
        }
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
                userLatLng = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                tmpLocation = place.getAddress();
                autocompleteText.setText(tmpLocation);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("ERROR", "An error occurred: " + status.getStatusMessage());
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void clickOnMarker() {
        gMap.setOnMarkerClickListener(marker -> {
            RideWithUserModel ride = null;
            for (RideWithUserModel rideTmp : rideUserList) {
                if (rideTmp.getAddress().getCoordinate().getLatitude() == marker.getPosition().latitude && rideTmp.getAddress().getCoordinate().getLongitude() == marker.getPosition().longitude) {
                    ride = rideTmp;
                }
            }
            if (ride != null) {
                gMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Nullable
                    @Override
                    public View getInfoContents(@NonNull Marker marker) {
                        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.ride_popup, null);
                        TextView userNameView = view.findViewById(R.id.user_name);
                        TextView locationView = view.findViewById(R.id.ride_location);
                        TextView timeView = view.findViewById(R.id.ride_time);
                        RideWithUserModel rideUser = null;
                        for (RideWithUserModel rideTmp : rideUserList) {
                            if (rideTmp.getAddress().getCoordinate().getLatitude() == marker.getPosition().latitude && rideTmp.getAddress().getCoordinate().getLongitude() == marker.getPosition().longitude) {
                                rideUser = rideTmp;
                            }
                        }
                        userNameView.setText(rideUser.getName()+ " "+rideUser.getSurname());
                        locationView.setText(rideUser.getAddress().getLocation());
                        SimpleDateFormat formatter = new SimpleDateFormat(getString(R.string.date_pattern));
                        timeView.setText(formatter.format(new Date(rideUser.getDate())));
                        return view;
                    }

                    @Nullable
                    @Override
                    public View getInfoWindow(@NonNull Marker marker) {
                        return null;
                    }
                });
                marker.showInfoWindow();
            }
            return false;
        });
    }

    private void navigateToBookRideActivity() {
        gMap.setOnInfoWindowClickListener(marker1 -> {
            RideWithUserModel rideUser = null;
            for (RideWithUserModel rideTmp : rideUserList) {
                if (rideTmp.getAddress().getCoordinate().getLatitude() == marker1.getPosition().latitude && rideTmp.getAddress().getCoordinate().getLongitude() == marker1.getPosition().longitude) {
                    rideUser = rideTmp;
                }
            }
            if (rideUser != null) {
                Intent intent = new Intent(getApplicationContext(), BookRideActivity.class);
                intent.putExtra(getString(R.string.ride_id_text), rideUser.getId());
                intent.putExtra(getString(R.string.user_name_text), rideUser.getName()+" "+rideUser.getSurname());
                intent.putExtra(getString(R.string.user_id_text), rideUser.getUserId());
                startActivity(intent);
            }
        });
    }
}