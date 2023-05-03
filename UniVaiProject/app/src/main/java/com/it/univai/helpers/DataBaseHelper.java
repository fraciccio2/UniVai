package com.it.univai.helpers;

import android.util.Patterns;

public class DataBaseHelper {

    public Boolean isCorrectEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.contains("studium");
    }
}
