package com.example.carsharing.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carsharing.R;
import com.example.carsharing.adapters.RideAdapter;
import com.example.carsharing.databinding.ActivityRidesListBinding;
import com.example.carsharing.models.RideModel;
import com.example.carsharing.models.RideWithUserModel;
import com.example.carsharing.models.UserModel;
import com.example.carsharing.services.NavigationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RidesListActivity extends AppCompatActivity {

    ActivityRidesListBinding binding;
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseRides;
    DatabaseReference mDatabaseUsers;
    NavigationHelper navigationHelper = new NavigationHelper();
    UserModel logUser;
    int i = 0, l = 0;

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
        if(user != null) {
            getLoggedUser(user);
        }
        navigationHelper.floatButtonOnClick(binding.floatingButton, getApplicationContext());
        getRides();
    }

    private void getRides() {
        mDatabaseRides.orderByChild("active").equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshotData) {
                if(snapshotData.exists()) {
                    List<RideWithUserModel> rideUserList = new ArrayList<>();
                    for (DataSnapshot ignored : snapshotData.getChildren()) {
                        i++;
                    }
                    for (DataSnapshot data: snapshotData.getChildren()){
                        RideModel ride = data.getValue(RideModel.class);
                        mDatabaseUsers.orderByKey().equalTo(ride.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                l++;
                                if(snapshot.exists()) {
                                    String name = "";
                                    String surname = "";
                                    String userImage = "";
                                    for (DataSnapshot datas: snapshot.getChildren()){
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
                                            userImage
                                    );
                                    if(!ride.getUserId().equals(mAuth.getCurrentUser().getUid())) {
                                        rideUserList.add(rideUser);
                                    }
                                }
                                if(i == l) {
                                    if(rideUserList.size() > 0) {
                                        RecyclerView recyclerView = binding.recyclerView;
                                        LinearLayoutManager layoutManager = new LinearLayoutManager(RidesListActivity.this);
                                        recyclerView.setLayoutManager(layoutManager);
                                        RideAdapter adapter = new RideAdapter(rideUserList, getApplicationContext());
                                        recyclerView.setAdapter(adapter);
                                    } else {
                                        warningRidesAlert();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("Error", "exception", error.toException());
                            }
                        });
                    }
                } else {
                    warningRidesAlert();
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
}