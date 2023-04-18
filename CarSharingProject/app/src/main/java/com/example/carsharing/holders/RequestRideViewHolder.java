package com.example.carsharing.holders;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carsharing.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class RequestRideViewHolder extends RecyclerView.ViewHolder {
    public MaterialCardView requestCard;
    public TextView status;
    public TextView user;
    public TextView position;
    public TextView labelUser;
    public TextView labelPosition;
    public TextView token;
    public LinearLayout layoutWithButtons;
    public MaterialButton acceptButton;
    public MaterialButton refuseButton;

    public RequestRideViewHolder(@NonNull View itemView) {
        super(itemView);
        requestCard = itemView.findViewById(R.id.request_ride_card);
        status = itemView.findViewById(R.id.request_status);
        user = itemView.findViewById(R.id.request_user);
        position = itemView.findViewById(R.id.request_position);
        labelUser = itemView.findViewById(R.id.label_request_user);
        labelPosition = itemView.findViewById(R.id.label_request_position);
        layoutWithButtons = itemView.findViewById(R.id.layout_with_button);
        acceptButton = itemView.findViewById(R.id.accept_button);
        refuseButton = itemView.findViewById(R.id.refuse_button);
        token = itemView.findViewById(R.id.token_request);
    }
}
