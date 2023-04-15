package com.example.carsharing.services;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.example.carsharing.R;
import com.example.carsharing.activities.MainActivity;
import com.example.carsharing.activities.NewRideActivity;
import com.example.carsharing.activities.RidesListActivity;
import com.example.carsharing.activities.SettingsActivity;
import com.example.carsharing.models.UserModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NavigationHelper {

    public void navigate(BottomNavigationView navigation, Context context) {
        navigation.setBackground(null);
        navigation.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_map:
                    context.startActivity(new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    break;
                case R.id.action_search:
                    context.startActivity(new Intent(context, RidesListActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    break;
                case R.id.action_settings:
                    context.startActivity(new Intent(context, SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
            return false;
        });
    }

    public void hideButton(FloatingActionButton floatingButton, BottomNavigationView navigation, UserModel logUser) {
        if(!logUser.getHasCar()) {
            floatingButton.setVisibility(View.INVISIBLE);
            floatingButton.setEnabled(false);
            navigation.getMenu().removeItem(R.id.action_disabled);
        }
    }

    public void floatButtonOnClick(FloatingActionButton floatingButton, Context context) {
        floatingButton.setOnClickListener(view -> {
            Intent intent = new Intent(context, NewRideActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }
}
