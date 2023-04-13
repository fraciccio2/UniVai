package com.example.carsharing.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;

import com.example.carsharing.R;
import com.example.carsharing.adapters.RequestAdapter;
import com.example.carsharing.databinding.ActivityRequestListBinding;
import com.example.carsharing.models.RequestModel;
import com.example.carsharing.models.RequestWithUserModel;
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
    DatabaseReference mDatabaseRequests;
    DatabaseReference mDatabaseUsers;
    NavigationHelper navigationHelper = new NavigationHelper();
    UserModel logUser;
    int i = 0, l = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequestListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        mDatabaseRequests = FirebaseDatabase.getInstance().getReference("requests");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference("users");
        FirebaseUser user = mAuth.getCurrentUser();
        binding.bottomNavigationView.setSelectedItemId(R.id.action_todo);
        if(user != null) {
            getLoggedUser(user);
        }
        navigationHelper.floatButtonOnClick(binding.floatingButton, getApplicationContext());
        getRequests();
    }

    private void getRequests() {
        mDatabaseRequests.orderByChild("active").equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshotData) {
                if(snapshotData.exists()) {
                    List<RequestWithUserModel> requestUserList = new ArrayList<>();
                    for (DataSnapshot ignored : snapshotData.getChildren()) {
                        i++;
                    }
                    for (DataSnapshot data: snapshotData.getChildren()){
                        l++;
                        RequestModel request = data.getValue(RequestModel.class);
                        mDatabaseUsers.orderByKey().equalTo(request.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()) {
                                    String name = "";
                                    String surname = "";
                                    String userImage = "";
                                    for (DataSnapshot datas: snapshot.getChildren()){
                                        name = datas.child("name").getValue(String.class);
                                        surname = datas.child("surname").getValue(String.class);
                                        userImage = datas.child("userImage").getValue(String.class);
                                    }
                                    RequestWithUserModel requestUser = new RequestWithUserModel(
                                            data.getKey(),
                                            request.getAddress(),
                                            request.getDate(),
                                            request.getNote(),
                                            request.getActive(),
                                            name,
                                            surname,
                                            userImage
                                    );
                                    if(!request.getUserId().equals(mAuth.getCurrentUser().getUid())) {
                                        requestUserList.add(requestUser);
                                    }
                                }
                                if(i == l) {
                                    if(requestUserList.size() > 0) {
                                        RecyclerView recyclerView = binding.recyclerView;
                                        LinearLayoutManager layoutManager = new LinearLayoutManager(RequestListActivity.this);
                                        recyclerView.setLayoutManager(layoutManager);
                                        RequestAdapter adapter = new RequestAdapter(requestUserList, getApplicationContext());
                                        recyclerView.setAdapter(adapter);
                                    } else {
                                        warningRequestsAlert(); //TODO rivedere perché si apre 2 volte
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
                    warningRequestsAlert();
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

    private void warningRequestsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RequestListActivity.this);
        builder.setTitle(getString(R.string.ops_text));
        builder.setMessage(getString(R.string.warning_requests_text));
        builder.setNeutralButton(getString(R.string.neutral_button_text), (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}