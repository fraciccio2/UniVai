package com.example.carsharing.services;

import android.util.Patterns;

public class HelperDataBase {

    public Boolean isCorrectEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.contains("studium");
    }
}
