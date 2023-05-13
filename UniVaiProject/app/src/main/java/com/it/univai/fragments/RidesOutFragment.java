package com.it.univai.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.it.univai.R;
import com.it.univai.adapters.RequestRideAdapter;
import com.it.univai.models.AddressModel;
import com.it.univai.models.RequestRideModel;
import com.it.univai.models.RequestWithUserModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import taimoor.sultani.sweetalert2.Sweetalert;

public class RidesOutFragment extends Fragment {

    View view;
    RecyclerView recyclerView;
    DatabaseReference mDatabaseRequests;
    DatabaseReference mDatabaseUsers;
    DatabaseReference mDatabaseRides;
    FirebaseAuth mAuth;
    SearchView searchView;
    int i = 0, l = 0;
    List<RequestWithUserModel> requestsRideList = new ArrayList<>();

    public RidesOutFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_rides_out, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRequests = FirebaseDatabase.getInstance().getReference("requests");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference("users");
        mDatabaseRides = FirebaseDatabase.getInstance().getReference("rides");

        recyclerView = view.findViewById(R.id.recycler_view_out);

        searchView = view.findViewById(R.id.search_view);
        searchView.setActivated(true);
        searchView.setQueryHint(getString(R.string.search_rides_text));
        searchView.onActionViewExpanded();
        searchView.clearFocus();

        filterRides();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getRequests();
    }

    private void getRequests() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Sweetalert alert = new Sweetalert(getContext(), Sweetalert.PROGRESS_TYPE);
            alert.getProgressHelper().setBarColor(getResources().getColor(R.color.main_color));
            alert.setTitleText(getString(R.string.loading_text));
            alert.setCancelable(false);
            alert.show();
            requestsRideList = new ArrayList<>();
            mDatabaseRequests.orderByChild("requesterUser").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot ignored : snapshot.getChildren()) {
                            i++;
                        }
                        for (DataSnapshot data : snapshot.getChildren()) {
                            RequestRideModel requestRide = data.getValue(RequestRideModel.class);
                            mDatabaseUsers.orderByKey().equalTo(requestRide.getCreatorUser()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()) {
                                        for (DataSnapshot datas: snapshot.getChildren()) {
                                            String userName = datas.child("name").getValue(String.class) + " " + datas.child("surname").getValue(String.class);
                                            String userAvatar = datas.child("userImage").getValue(String.class);
                                            String phoneNumber = datas.child("phoneNumber").getValue(Long.class).toString();
                                            mDatabaseRides.orderByKey().equalTo(requestRide.getRideId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    l++;
                                                    if (snapshot.exists()) {
                                                        AddressModel address;
                                                        String date;
                                                        for (DataSnapshot ride : snapshot.getChildren()) {
                                                            SimpleDateFormat formatter = new SimpleDateFormat(view.getContext().getString(R.string.date_pattern));
                                                            address = ride.child("address").getValue(AddressModel.class);
                                                            date = formatter.format(new Date(ride.child("date").getValue(String.class)));
                                                            requestsRideList.add(new RequestWithUserModel(requestRide.getStatus(), userName, requestRide.getRideId(), data.getKey(), address.getLocation(), date, userAvatar, phoneNumber, true));
                                                        }
                                                        if (requestsRideList.size() > 0 && i == l) {
                                                            alert.dismiss();
                                                            RecyclerView recyclerView = view.findViewById(R.id.recycler_view_out);
                                                            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                                                            recyclerView.setLayoutManager(layoutManager);
                                                            Collections.reverse(requestsRideList);
                                                            RequestRideAdapter adapter = new RequestRideAdapter(getContext(), RidesOutFragment.this, requestsRideList);
                                                            recyclerView.setAdapter(adapter);
                                                        }
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
                        warningRidesAlert(getString(R.string.warning_no_request_out_text));
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

    private void filterRides() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                List<RequestWithUserModel> filteredRequestsRideList = new ArrayList<>();
                for (RequestWithUserModel requestRide : requestsRideList) {
                    if (requestRide.getTokenRequest().contains(text) || requestRide.getUserName().contains(text) || requestRide.getLocation().contains(text)) {
                        filteredRequestsRideList.add(requestRide);
                    }
                }
                if (filteredRequestsRideList.size() > 0) {
                    recyclerView.setAdapter(new RequestRideAdapter(getContext(), RidesOutFragment.this, requestsRideList));
                } else {
                    warningRidesAlert(getString(R.string.warning_ride_filter_text));
                }
                return false;
            }
        });
    }

    private void warningRidesAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.ops_text));
        builder.setMessage(message);
        builder.setNeutralButton(getString(R.string.neutral_button_text), (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}