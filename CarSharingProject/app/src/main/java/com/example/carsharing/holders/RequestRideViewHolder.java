package com.example.carsharing.holders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carsharing.R;
import com.google.android.material.card.MaterialCardView;

public class RequestRideViewHolder extends RecyclerView.ViewHolder {
    public MaterialCardView requestCard;
    public TextView status;
    public TextView user;
    public TextView position;
    public TextView labelUser;
    public TextView labelPosition;

    public RequestRideViewHolder(@NonNull View itemView) {
        super(itemView);
        requestCard = itemView.findViewById(R.id.request_ride_card);
        status = itemView.findViewById(R.id.request_status);
        user = itemView.findViewById(R.id.request_user);
        position = itemView.findViewById(R.id.request_position);
        labelUser = itemView.findViewById(R.id.label_request_user);
        labelPosition = itemView.findViewById(R.id.label_request_position);
    }
}
