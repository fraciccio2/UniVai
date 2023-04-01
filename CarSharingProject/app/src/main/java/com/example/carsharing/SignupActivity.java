package com.example.carsharing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.carsharing.databinding.ActivitySignupBinding;

public class SignupActivity extends AppCompatActivity {

    ActivitySignupBinding binding;
    HelperDataBase helperDataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        helperDataBase = new HelperDataBase(this);

        binding.signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.signupEmail.getText().toString();
                String password = binding.signupPassword.getText().toString();
                String confirmPassword = binding.signupConfirmPassword.getText().toString();

                if(email.equals("") || password.equals("") || confirmPassword.equals("")){
                    Toast.makeText(SignupActivity.this, "Tutti i campi sono obbligatori", Toast.LENGTH_SHORT).show();
                } else {
                    if(password.equals(confirmPassword)) {
                        Boolean checkUserEmail = helperDataBase.checkEmail(email);
                        if(!checkUserEmail) {
                            Boolean insert = helperDataBase.insertData(email, password);
                            if(insert) {
                                Toast.makeText(SignupActivity.this, "Registrazione avvenuta con successo", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(SignupActivity.this, "Errore nella registrazione", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(SignupActivity.this, "Email già presente", Toast.LENGTH_SHORT).show();
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