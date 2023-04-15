package com.example.carsharing.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.carsharing.R;
import com.example.carsharing.databinding.ActivityMainBinding;
import com.example.carsharing.models.RideModel;
import com.example.carsharing.models.UserModel;
import com.example.carsharing.services.NavigationHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int PERMISSIONS_REQUEST_LOCATION = 100;
    ActivityMainBinding binding;
    GoogleMap gMap;
    FusedLocationProviderClient providerClient;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    LatLng userLatLng;
    double radius = 1000;
    List<RideModel> ridesList = new ArrayList<>();
    UserModel logUser;
    NavigationHelper navigationHelper = new NavigationHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            getLoggedUser(user);
        }
        binding.bottomNavigationView.setSelectedItemId(R.id.action_map);
        providerClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.main_map);
        mapFragment.getMapAsync(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
        }

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
            }
        }
    }

    private void loadMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            providerClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    getRides();
                }
            });

            providerClient.getLastLocation().addOnFailureListener(e -> Log.e("Error", "exception", e));
        } else {
            if (logUser != null) {
                userLatLng = new LatLng(logUser.getAddress().getCoordinate().getLatitude(), logUser.getAddress().getCoordinate().getLongitude());
            } else {
                userLatLng = new LatLng(41.90370, 12.49524);
            }
            getRides();
        }
    }

    private void setMap(double lat, double lon) {
        LatLng mapItaly = new LatLng(lat, lon);
        gMap.addMarker(new MarkerOptions().position(mapItaly));
        for (RideModel ride : ridesList) {
            gMap.addMarker(new MarkerOptions().position(new LatLng(ride.getAddress().getCoordinate().getLatitude(), ride.getAddress().getCoordinate().getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        }
        gMap.moveCamera(CameraUpdateFactory.newLatLng(mapItaly));
        gMap.setMinZoomPreference(5);
        gMap.setLatLngBoundsForCameraTarget(new LatLngBounds(new LatLng(36.6199, 6.7499), new LatLng(47.1153, 18.4802)));
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mapItaly, 14));
    }

    private static double distanceBetweenLatLong(double lat1, double lon1, double lat2, double lon2) {
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);

        double earthRadius = 6371.01; //Kilometers
        return earthRadius * Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));
    }

    private void getRides() {
        FirebaseDatabase.getInstance().getReference("rides").orderByChild("active").equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        RideModel rideModel = data.getValue(RideModel.class);
                        if (distanceBetweenLatLong(rideModel.getAddress().getCoordinate().getLatitude(), rideModel.getAddress().getCoordinate().getLongitude(), userLatLng.latitude, userLatLng.longitude) < radius) {
                            ridesList.add(rideModel);
                        }
                    }
                    setMap(userLatLng.latitude, userLatLng.longitude);
                } else {
                    setMap(userLatLng.latitude, userLatLng.longitude);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error", "exception", error.toException());
            }
        });
    }

    private void getLoggedUser(FirebaseUser user) {
        mDatabase.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
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
}