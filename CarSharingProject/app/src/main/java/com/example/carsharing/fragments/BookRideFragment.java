package com.example.carsharing.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.carsharing.R;
import com.example.carsharing.activities.MainActivity;
import com.example.carsharing.activities.RidesListActivity;
import com.example.carsharing.databinding.FragmentBookRideBinding;
import com.example.carsharing.enums.StatusEnum;
import com.example.carsharing.models.AddressModel;
import com.example.carsharing.models.LatLonModel;
import com.example.carsharing.models.RequestRideModel;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class BookRideFragment extends Fragment {

    FragmentBookRideBinding binding;
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseRides;
    String rideId;
    String userId;
    String location;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBookRideBinding.inflate(inflater, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRides = FirebaseDatabase.getInstance().getReference("rides");

        Places.initialize(getActivity(), getString(R.string.api_key));
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.other_location);
        if (autocompleteFragment != null) {
            autocompleteFragment.setCountry("it");
            autocompleteFragment.setHint(getString(R.string.search_address_text));
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS));
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onError(@NonNull Status status) {
                    Log.i("ERROR", "An error occurred: " + status);
                }

                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    location = place.getAddress();
                }
            });
        }

        Bundle bundle = getArguments();
        if(bundle != null) {
            rideId = bundle.getString(getString(R.string.ride_id_text));
            userId = bundle.getString(getString(R.string.user_id_text));
            String userName = bundle.getString(getString(R.string.user_name_text));
            binding.creatorUser.setText(userName);
            getRide();
        }

        saveRequestForRide();
        someLocationButton();
        otherLocationButton();

        return binding.getRoot();
    }

    private void getRide() {
        mDatabaseRides.orderByKey().equalTo(rideId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot datas: snapshot.getChildren()){
                        AddressModel address = datas.child("address").getValue(AddressModel.class);
                        String date = datas.child("date").getValue(String.class);
                        String note = datas.child("note").getValue(String.class);
                        binding.rideAddress.setText(address.getLocation());
                        SimpleDateFormat formatter = new SimpleDateFormat(getString(R.string.date_pattern));
                        binding.rideDeparture.setText(formatter.format(new Date(date)));
                        binding.rideNote.setText(note);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error", "exception", error.toException());
            }
        });
    }

    private void saveRequestForRide() {
        binding.bookButton.setOnClickListener(view -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if(user != null) {
                if(binding.otherLocationButton.isChecked()) {
                    if(location != null) {
                        RequestRideModel requestRide = new RequestRideModel(StatusEnum.PENDING, userId, user.getUid(), rideId, location);
                        FirebaseDatabase.getInstance().getReference("requests").push().setValue(requestRide).addOnCompleteListener(task -> {
                            if(task.isSuccessful()) {
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getActivity(), "C'è stato un problema, riprova.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getActivity(), "Inserisci l'indirizo in cui ti farai trovare", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    RequestRideModel requestRide = new RequestRideModel(StatusEnum.PENDING, userId, user.getUid(), rideId);
                    FirebaseDatabase.getInstance().getReference("requests").push().setValue(requestRide).addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getActivity(), "C'è stato un problema, riprova.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void someLocationButton() {
        binding.someLocationButton.setOnClickListener(view ->{
            if(binding.someLocationButton.isChecked()) {
                binding.autocompleteCard.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void otherLocationButton() {
        binding.otherLocationButton.setOnClickListener(view ->{
            if(binding.otherLocationButton.isChecked()) {
                binding.autocompleteCard.setVisibility(View.VISIBLE);
            }
        });
    }
}