package com.example.carsharing;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.example.carsharing.databinding.ActivityAddInfoBinding;

public class AddInfoActivity extends AppCompatActivity {

    ActivityAddInfoBinding binding;
    String[] items = {"ciao"};
    ArrayAdapter<String> adapterItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapterItems = new ArrayAdapter<String>(this,R.layout.list_item, items);
        binding.autoCompleteText.setAdapter(adapterItems);
    }
}