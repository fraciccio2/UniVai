package com.example.carsharing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.carsharing.databinding.ActivitySignupBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {

    ActivitySignupBinding binding;
    HelperDataBase helperDataBase;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        mAuth = FirebaseAuth.getInstance();
        setContentView(binding.getRoot());

        helperDataBase = new HelperDataBase();

        binding.signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.signupEmail.getText().toString();
                String password = binding.signupPassword.getText().toString();
                String confirmPassword = binding.signupConfirmPassword.getText().toString();

                if (email.equals("") || password.equals("") || confirmPassword.equals("")) {
                    Toast.makeText(SignupActivity.this, "Tutti i campi sono obbligatori", Toast.LENGTH_SHORT).show();
                } else {
                    if (password.equals(confirmPassword)) {
                        Boolean correctEmail = helperDataBase.isCorrectEmail(email);
                        if (correctEmail) {
                                mAuth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(SignupActivity.this, "Registrazione avvenuta con successo", Toast.LENGTH_SHORT).show();
                                                    FirebaseUser user = mAuth.getCurrentUser();
                                                } else {
                                                    Toast.makeText(SignupActivity.this, "Errore nella registrazione", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                        } else {
                            Toast.makeText(SignupActivity.this, "L'email fornita non è valida", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignupActivity.this, "Le password inserite sono diverse", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        binding.loginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}