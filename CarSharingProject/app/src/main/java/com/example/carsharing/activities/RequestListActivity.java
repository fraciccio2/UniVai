package com.example.carsharing.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.example.carsharing.adapters.RequestAdapter;
import com.example.carsharing.databinding.ActivityRequestListBinding;
import com.example.carsharing.models.RequestModel;
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

public class RequestListActivity extends AppCompatActivity {

    ActivityRequestListBinding binding;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    NavigationHelper navigationHelper = new NavigationHelper();
    UserModel logUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequestListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("requests");
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            getLoggedUser(user);
        }
        getRequests();
    }

    private void getRequests() {
        mDatabase.orderByChild("active").equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<RequestModel> requestList = new ArrayList<>();
                if(snapshot.exists()) {
                    for (DataSnapshot data: snapshot.getChildren()){
                        RequestModel request = data.getValue(RequestModel.class);
                        if(request != null) {
                            requestList.add(request);
                        }
                    }
                    RecyclerView recyclerView = binding.recyclerView;
                    LinearLayoutManager layoutManager = new LinearLayoutManager(RequestListActivity.this);
                    recyclerView.setLayoutManager(layoutManager);
                    RequestAdapter adapter = new RequestAdapter(requestList);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error", "exception", error.toException());
            }
        });
    }

    private void getLoggedUser(FirebaseUser user) {
        FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
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
}