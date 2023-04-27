package com.example.carsharing.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carsharing.R;
import com.example.carsharing.fragments.YourRidesFragment;
import com.example.carsharing.holders.YourRideViewHolder;
import com.example.carsharing.models.RideModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class YourRideAdapter extends RecyclerView.Adapter<YourRideViewHolder> {

    private final Context context;
    private final YourRidesFragment fragment;
    private final List<RideModel> ridesList;

    public YourRideAdapter(Context context, YourRidesFragment fragment, List<RideModel> ridesList) {
        this.context = context;
        this.fragment = fragment;
        this.ridesList = ridesList;
    }

    @NonNull
    @Override
    public YourRideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.your_ride_card, parent, false);
        return new YourRideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull YourRideViewHolder holder, int position) {
        RideModel ride = ridesList.get(position);
        holder.address.setText(ride.getAddress().getLocation());
        SimpleDateFormat formatter = new SimpleDateFormat(context.getString(R.string.date_pattern));
        holder.dateTime.setText(formatter.format(new Date(ride.getDate())));
        holder.note.setText(ride.getNote());
        holder.deleteRide.setOnClickListener(view -> fragment.deleteRide(position));
    }

    @Override
    public int getItemCount() {
        return ridesList.size();
    }
}
