package com.it.univai.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.it.univai.R;
import com.it.univai.databinding.ActivitySignupBinding;
import com.it.univai.helpers.DataBaseHelper;

public class SignupActivity extends AppCompatActivity {

    ActivitySignupBinding binding;
    DataBaseHelper dataBaseHelper;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        setContentView(binding.getRoot());

        dataBaseHelper = new DataBaseHelper();

        signUpAction();

        binding.loginRedirect.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        });
    }

    private void signUpAction() {
        binding.signupButton.setOnClickListener(view -> {
            String email = binding.signupEmail.getText().toString();
            String password = binding.signupPassword.getText().toString();
            String confirmPassword = binding.signupConfirmPassword.getText().toString();

            if (email.equals("") || password.equals("") || confirmPassword.equals("")) {
                Toast.makeText(SignupActivity.this, getString(R.string.all_field_required_text), Toast.LENGTH_SHORT).show();
            } else {
                if (password.equals(confirmPassword)) {
                    Boolean correctEmail = dataBaseHelper.isCorrectEmail(email);
                    if (correctEmail) {
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SignupActivity.this, getString(R.string.successful_signup_text), Toast.LENGTH_SHORT).show();
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if(user != null) {
                                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                            startActivity(intent);
                                        }
                                    } else {
                                        Toast.makeText(SignupActivity.this, getString(R.string.error_signup_text), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(SignupActivity.this, getString(R.string.email_wrong_text), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SignupActivity.this, getString(R.string.different_password_text), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}