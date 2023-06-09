package com.it.univai.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.it.univai.R;
import com.it.univai.fragments.YourRidesFragment;
import com.it.univai.holders.YourRideViewHolder;
import com.it.univai.models.RideModel;

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
        if (ride.getActive()) {
            holder.deleteRide.setOnClickListener(view -> fragment.deleteRide(position));
            holder.deleteRide.setTextColor(context.getResources().getColor(R.color.white));
        } else {
            holder.deleteRide.setBackgroundColor(context.getResources().getColor(R.color.light_grey));
            holder.deleteRide.setTextColor(context.getResources().getColor(R.color.white));
            holder.deleteRide.setEnabled(false);
        }
    }

    @Override
    public int getItemCount() {
        return ridesList.size();
    }
}
