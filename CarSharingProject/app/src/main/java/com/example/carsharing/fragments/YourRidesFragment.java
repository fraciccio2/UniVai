package com.example.carsharing.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carsharing.R;
import com.example.carsharing.adapters.YourRideAdapter;
import com.example.carsharing.models.RideModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import taimoor.sultani.sweetalert2.Sweetalert;

public class YourRidesFragment extends Fragment {

    SearchView searchView;
    RecyclerView recyclerView;
    View view;
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseRides;
    DatabaseReference mDatabaseRequests;
    List<RideModel> ridesList = new ArrayList<>();
    List<String> rideIds = new ArrayList<>();

    public YourRidesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_your_rides, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRides = FirebaseDatabase.getInstance().getReference("rides");
        mDatabaseRequests = FirebaseDatabase.getInstance().getReference("requests");

        recyclerView = view.findViewById(R.id.recycler_view_your);

        searchView = view.findViewById(R.id.search_view);
        searchView.setActivated(true);
        searchView.setQueryHint(getString(R.string.search_rides_text));
        searchView.onActionViewExpanded();
        searchView.clearFocus();

        getRides();
        filterRides();

        return view;
    }

    private void getRides() {
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            Sweetalert alert = new Sweetalert(getContext(), Sweetalert.PROGRESS_TYPE);
            alert.getProgressHelper().setBarColor(getResources().getColor(R.color.main_color));
            alert.setTitleText(getString(R.string.loading_text));
            alert.setCancelable(false);
            alert.show();
            ridesList = new ArrayList<>();
            rideIds = new ArrayList<>();
            mDatabaseRides.orderByChild("userId").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        for(DataSnapshot data: snapshot.getChildren()) {
                            RideModel ride = data.getValue(RideModel.class);
                            ridesList.add(ride);
                            rideIds.add(data.getKey());
                        }
                        alert.dismiss();
                        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_your);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                        recyclerView.setLayoutManager(layoutManager);
                        Collections.reverse(ridesList);
                        YourRideAdapter adapter = new YourRideAdapter(getContext(), YourRidesFragment.this, ridesList);
                        recyclerView.setAdapter(adapter);

                    } else {
                        alert.dismiss();
                        warningRidesAlert(getString(R.string.warning_your_rides_text));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Error", "exception", error.toException());
                    alert.dismiss();
                }
            });
        }
    }

    private void warningRidesAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.ops_text));
        builder.setMessage(message);
        builder.setNeutralButton(getString(R.string.neutral_button_text), (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public void deleteRide(int position) {
        String rideId = rideIds.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.attention_title_text));
        builder.setMessage(getString(R.string.delete_ride_alert_text));
        builder.setNegativeButton(getString(R.string.not_delete_text), (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(getString(R.string.next_text), (dialog, which) -> {
            mDatabaseRides.child(rideId).removeValue().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    getRides();
                } else {
                    Toast.makeText(getContext(), getString(R.string.error_retry_text), Toast.LENGTH_SHORT).show();
                }
            });
            mDatabaseRequests.orderByChild("rideId").equalTo(rideId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        for (DataSnapshot data: snapshot.getChildren()) {
                            if(data.getKey() != null) {
                                FirebaseDatabase.getInstance().getReference("requests").child(data.getKey()).removeValue();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Error", "exception", error.toException());
                }
            });
        });
        builder.show();
    }

    private void filterRides() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                List<RideModel> filteredRidesList = new ArrayList<>();
                for (RideModel requestRide : ridesList) {
                    if (
                            requestRide.getAddress().getLocation().toLowerCase().contains(text.toLowerCase()) ||
                                    requestRide.getDate().toLowerCase().contains(text.toLowerCase()) ||
                                    requestRide.getNote().toLowerCase().contains(text.toLowerCase())
                    ) {
                        filteredRidesList.add(requestRide);
                    }
                }
                if (filteredRidesList.size() > 0) {
                    recyclerView.setAdapter(new YourRideAdapter(getContext(), YourRidesFragment.this, filteredRidesList));
                } else {
                    warningRidesAlert(getString(R.string.warning_request_filter_text));
                }
                return false;
            }
        });
    }
}