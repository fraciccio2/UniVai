package com.example.carsharing.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.carsharing.R;
import com.example.carsharing.databinding.ActivityAddInfoBinding;
import com.example.carsharing.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddInfoActivity extends AppCompatActivity {

    ActivityAddInfoBinding binding;
    String[] items = {"ciao"};
    ArrayAdapter<String> adapterItems;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        adapterItems = new ArrayAdapter<String>(this, R.layout.list_item, items);
        binding.autoCompleteText.setAdapter(adapterItems);

        binding.startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = binding.startName.getText().toString();
                String surname = binding.startSurname.getText().toString();
                String address = binding.startAddress.getText().toString();
                String university = binding.startSelectUni.getTransitionName();
                Boolean hasCar = binding.startCar.isChecked();
                if(name.equals("") || surname.equals("")){
                    Toast.makeText(AddInfoActivity.this, "Inserisci tutti i campi necessari", Toast.LENGTH_SHORT).show();
                } else {
                    if(mAuth.getCurrentUser() != null) {
                        UserModel user = new UserModel(name, surname, address, university, hasCar);
                        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).setValue(user);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    }
                }
            }
        });
    }
}