package com.example.carsharing.services;

import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HelperNavigationBar {

    public void hiddenFloatingButton(FloatingActionButton button) {
        button.setVisibility(View.INVISIBLE);
        button.setEnabled(false);
    }
}
