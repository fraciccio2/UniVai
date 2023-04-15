package com.example.carsharing.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carsharing.R;

public class RideViewHolder extends RecyclerView.ViewHolder {
    public ImageView avatarUser;
    public TextView nameUser;
    public TextView rideAddress;
    public TextView rideDate;
    public TextView rideNote;
    public LinearLayout card;

    public RideViewHolder(@NonNull View itemView) {
        super(itemView);
        avatarUser = itemView.findViewById(R.id.image_user);
        nameUser = itemView.findViewById(R.id.user_name);
        rideAddress = itemView.findViewById(R.id.ride_address);
        rideDate = itemView.findViewById(R.id.ride_date);
        rideNote = itemView.findViewById(R.id.ride_note);
        card = itemView.findViewById(R.id.card_ride);
    }
}
