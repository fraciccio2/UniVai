package com.it.univai.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.it.univai.R;
import com.it.univai.adapters.ViewPagerAdapter;
import com.it.univai.databinding.ActivityRidesSummaryBinding;
import com.it.univai.helpers.NavigationHelper;
import com.it.univai.models.UserModel;

public class RidesSummaryActivity extends AppCompatActivity {

    ActivityRidesSummaryBinding binding;
    ViewPagerAdapter viewPagerAdapter;
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseUser;
    UserModel logUser;
    NavigationHelper navigationHelper = new NavigationHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRidesSummaryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewPagerAdapter = new ViewPagerAdapter(this);
        binding.viewPager.setAdapter(viewPagerAdapter);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUser = FirebaseDatabase.getInstance().getReference("users");
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            getLoggedUser(user);
        }

        binding.bottomNavigationView.setSelectedItemId(R.id.action_summary);
        navigationHelper.navigate(binding.bottomNavigationView, getApplicationContext());
        navigationHelper.floatButtonOnClick(binding.floatingButton, getApplicationContext());

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.tabLayout.getTabAt(position).select();
            }
        });
    }

    private void getLoggedUser(FirebaseUser user) {
        mDatabaseUser.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    logUser = snapshot.getValue(UserModel.class);
                    navigationHelper.hideButton(binding.floatingButton, binding.bottomNavigationView, logUser);
                    if(!logUser.getHasCar()) {
                        binding.tabLayout.removeTabAt(2);
                        binding.tabLayout.removeTabAt(1);
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