package com.it.univai.holders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.it.univai.R;
import com.google.android.material.button.MaterialButton;

public class YourRideViewHolder extends RecyclerView.ViewHolder {
    public TextView address;
    public TextView dateTime;
    public TextView note;
    public MaterialButton deleteRide;

    public YourRideViewHolder(@NonNull View itemView) {
        super(itemView);
        address = itemView.findViewById(R.id.text_view_address);
        dateTime = itemView.findViewById(R.id.text_view_date);
        note = itemView.findViewById(R.id.text_view_note);
        deleteRide = itemView.findViewById(R.id.delete_ride_button);
    }
}
