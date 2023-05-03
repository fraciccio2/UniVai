package com.it.univai.services;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.it.univai.R;
import com.it.univai.activities.MainActivity;
import com.it.univai.activities.NewRideActivity;
import com.it.univai.activities.RidesListActivity;
import com.it.univai.activities.RidesSummaryActivity;
import com.it.univai.activities.SettingsActivity;
import com.it.univai.models.UserModel;
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
                    break;
                case R.id.action_summary:
                    context.startActivity(new Intent(context, RidesSummaryActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    break;
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
