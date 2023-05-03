package com.example.carsharing.fragments;

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

import com.deeplabstudio.fcmsend.FCMSend;
import com.example.carsharing.R;
import com.example.carsharing.adapters.RequestRideAdapter;
import com.example.carsharing.enums.StatusEnum;
import com.example.carsharing.models.RequestRideModel;
import com.example.carsharing.models.RequestWithUserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import taimoor.sultani.sweetalert2.Sweetalert;

public class RidesInFragment extends Fragment {

    SearchView searchView;
    RecyclerView recyclerView;
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseRequests;
    DatabaseReference mDatabaseUsers;
    DatabaseReference mDatabaseTokens;
    View view;
    List<RequestWithUserModel> requestsRideList = new ArrayList<>();
    int i = 0, l = 0;

    public RidesInFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_rides_in, container, false);

        FCMSend.SetServerKey(getString(R.string.server_key));

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRequests = FirebaseDatabase.getInstance().getReference("requests");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference("users");
        mDatabaseTokens = FirebaseDatabase.getInstance().getReference("registrationToken");

        recyclerView = view.findViewById(R.id.recycler_view_in);

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
            mDatabaseRequests.orderByChild("creatorUser").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot ignored : snapshot.getChildren()) {
                            i++;
                        }
                        for (DataSnapshot data : snapshot.getChildren()) {
                            RequestRideModel requestRide = data.getValue(RequestRideModel.class);
                            mDatabaseUsers.orderByKey().equalTo(requestRide.getRequesterUser()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    l++;
                                    if (snapshot.exists()) {
                                        String userName = "";
                                        String userAvatar = "";
                                        String userUid = "";
                                        for (DataSnapshot datas : snapshot.getChildren()) {
                                            userName = datas.child("name").getValue(String.class) + " " + datas.child("surname").getValue(String.class);
                                            userAvatar = datas.child("userImage").getValue(String.class);
                                            userUid = datas.getKey();
                                        }
                                        requestsRideList.add(new RequestWithUserModel(requestRide.getStatus(), userName, userUid, requestRide.getRideId(), data.getKey(), requestRide.getLocation(), userAvatar, requestRide.isSameAddress(), false));
                                        if (i == l) {
                                            alert.dismiss();
                                            if (requestsRideList.size() > 0) {
                                                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                                                recyclerView.setLayoutManager(layoutManager);
                                                Collections.reverse(requestsRideList);
                                                RequestRideAdapter adapter = new RequestRideAdapter(getContext(), RidesInFragment.this, requestsRideList);
                                                recyclerView.setAdapter(adapter);
                                            } else {
                                                warningRidesAlert(getString(R.string.warning_no_request_in_text));
                                            }
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
                        warningRidesAlert(getString(R.string.warning_no_request_in_text));
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
                    if (
                            requestRide.getTokenRequest().toLowerCase().contains(text.toLowerCase()) ||
                                    requestRide.getUserName().toLowerCase().contains(text.toLowerCase()) ||
                                    (requestRide.getLocation() != null && requestRide.getLocation().toLowerCase().contains(text.toLowerCase()))
                    ) {
                        filteredRequestsRideList.add(requestRide);
                    }
                }
                if (filteredRequestsRideList.size() > 0) {
                    recyclerView.setAdapter(new RequestRideAdapter(getContext(), RidesInFragment.this, requestsRideList));
                } else {
                    warningRidesAlert(getString(R.string.warning_request_filter_text));
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

    public void refuseRequest(String requestId, String userUid) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", StatusEnum.REFUSED);
        mDatabaseRequests.child(requestId).updateChildren(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mDatabaseTokens.orderByValue().equalTo(userUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot data : snapshot.getChildren()) {
                                FCMSend.Builder builder = new FCMSend.Builder(data.getKey())
                                        .setTitle(getString(R.string.app_name))
                                        .setBody(getString(R.string.refused_message_text));
                                builder.send();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Error", "exception", error.toException());
                    }
                });
            }
            getRequests();
        });
    }

    public void acceptRequest(String requestId, String userUid) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", StatusEnum.ACCEPT);
        mDatabaseRequests.child(requestId).updateChildren(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mDatabaseTokens.orderByValue().equalTo(userUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot data : snapshot.getChildren()) {
                                FCMSend.Builder builder = new FCMSend.Builder(data.getKey())
                                        .setTitle(getString(R.string.app_name))
                                        .setBody(getString(R.string.accepted_message_text));
                                builder.send();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Error", "exception", error.toException());
                    }
                });
            }
            getRequests();
        });
    }
}