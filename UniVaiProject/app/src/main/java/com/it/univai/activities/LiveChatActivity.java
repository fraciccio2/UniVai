package com.it.univai.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.it.univai.adapters.LiveChatAdapter;
import com.it.univai.databinding.ActivityLiveChatBinding;
import com.it.univai.enums.StatusEnum;
import com.it.univai.models.UserChatModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import taimoor.sultani.sweetalert2.Sweetalert;

public class LiveChatActivity extends AppCompatActivity {

    ActivityLiveChatBinding binding;
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseRequests;
    DatabaseReference mDatabaseUsers;
    List<UserChatModel> mUsers;
    Sweetalert alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLiveChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRequests = FirebaseDatabase.getInstance().getReference("requests");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference("users");

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(getString(R.string.chat_text));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        alert = new Sweetalert(this, Sweetalert.PROGRESS_TYPE);
        alert.getProgressHelper().setBarColor(getResources().getColor(R.color.main_color));
        alert.setTitleText(getString(R.string.loading_text));
        alert.setCancelable(false);
        alert.show();

        readUsers();
    }

    private void readUsers() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mUsers = new ArrayList<>();
            mDatabaseRequests.orderByChild("requesterUser").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Set<String> idUsers = new HashSet<>();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            StatusEnum status = data.child("status").getValue(StatusEnum.class);
                            if (status == StatusEnum.ACCEPT) {
                                idUsers.add(data.child("creatorUser").getValue(String.class));
                            }
                        }
                        if (idUsers.size() > 0) {
                            for (String idUser : idUsers) {
                                mDatabaseUsers.orderByKey().equalTo(idUser).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            for (DataSnapshot data : snapshot.getChildren()) {
                                                String name = data.child("name").getValue(String.class);
                                                String surname = data.child("surname").getValue(String.class);
                                                String userImage = data.child("userImage").getValue(String.class);
                                                mUsers.add(
                                                        new UserChatModel(
                                                                data.getKey(),
                                                                name,
                                                                surname,
                                                                userImage
                                                        )
                                                );
                                            }
                                            if (idUsers.size() == mUsers.size()) {
                                                RecyclerView recyclerView = binding.liveChatRecycler;
                                                LinearLayoutManager layoutManager = new LinearLayoutManager(LiveChatActivity.this);
                                                recyclerView.setLayoutManager(layoutManager);
                                                Collections.reverse(mUsers);
                                                LiveChatAdapter adapter = new LiveChatAdapter(getApplicationContext(), mUsers);
                                                recyclerView.setAdapter(adapter);
                                                alert.dismiss();
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}