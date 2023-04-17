package com.example.carsharing.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carsharing.R;
import com.example.carsharing.adapters.RequestRideAdapter;
import com.example.carsharing.databinding.FragmentRidesInBinding;
import com.example.carsharing.models.RequestRideModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RidesOutFragment extends Fragment {

    FragmentRidesInBinding binding;
    DatabaseReference mDatabaseRequests;
    FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRidesInBinding.inflate(inflater, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRequests = FirebaseDatabase.getInstance().getReference("requests");

        getRequests(inflater.inflate(R.layout.fragment_rides_out, container, false));

        return binding.getRoot();
    }

    private void getRequests(View view) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            List<RequestRideModel> requestsRideList = new ArrayList<>();
            mDatabaseRequests.orderByChild("requesterUser").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            RequestRideModel requestRide = data.getValue(RequestRideModel.class);
                            requestsRideList.add(requestRide);
                        }
                        if (requestsRideList.size() > 0) {
                            RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
                            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                            recyclerView.setLayoutManager(layoutManager);
                            RequestRideAdapter adapter = new RequestRideAdapter(requestsRideList);
                            recyclerView.setAdapter(adapter);
                        }
                        //TODO controllare perché non si vede il componente
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}