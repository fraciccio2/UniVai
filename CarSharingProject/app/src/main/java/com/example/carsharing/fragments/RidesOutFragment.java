package com.example.carsharing.fragments;

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

import com.example.carsharing.R;
import com.example.carsharing.adapters.RequestRideAdapter;
import com.example.carsharing.models.AddressModel;
import com.example.carsharing.models.RequestRideModel;
import com.example.carsharing.models.RequestWithUserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RidesOutFragment extends Fragment {

    DatabaseReference mDatabaseRequests;
    DatabaseReference mDatabaseUsers;
    DatabaseReference mDatabaseRides;
    FirebaseAuth mAuth;
    SearchView searchView;
    int i = 0, l = 0;
    String userName = "";

    public RidesOutFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rides_out, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRequests = FirebaseDatabase.getInstance().getReference("requests");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference("users");
        mDatabaseRides = FirebaseDatabase.getInstance().getReference("rides");

        searchView = (SearchView) view.findViewById(R.id.search_view);
        searchView.setActivated(true);
        searchView.setQueryHint(getString(R.string.search_rides_text));
        searchView.onActionViewExpanded();
        searchView.clearFocus();

        getRequests(view);

        return view;
    }

    private void getRequests(View view) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            List<RequestWithUserModel> requestsRideList = new ArrayList<>();
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
                                            userName = datas.child("name").getValue(String.class) + " " +datas.child("surname").getValue(String.class);
                                            mDatabaseRides.orderByKey().equalTo(requestRide.getRideId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if(snapshot.exists()){
                                                        l++;
                                                        AddressModel address;
                                                        String date;
                                                        for(DataSnapshot ride: snapshot.getChildren()) {
                                                            SimpleDateFormat formatter = new SimpleDateFormat(view.getContext().getString(R.string.date_pattern));
                                                            address = ride.child("address").getValue(AddressModel.class);
                                                            date = formatter.format(new Date(ride.child("date").getValue(String.class)));
                                                            requestsRideList.add(new RequestWithUserModel(requestRide.getStatus(), userName, requestRide.getRideId(), data.getKey(), address.getLocation(), date,true));
                                                        }
                                                        if (requestsRideList.size() > 0 && i == l) {
                                                            RecyclerView recyclerView = view.findViewById(R.id.recycler_view_out);
                                                            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                                                            recyclerView.setLayoutManager(layoutManager);
                                                            RequestRideAdapter adapter = new RequestRideAdapter(getContext(), requestsRideList);
                                                            recyclerView.setAdapter(adapter);
                                                        }
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