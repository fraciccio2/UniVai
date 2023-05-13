package com.it.univai.holders;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.it.univai.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestRideViewHolder extends RecyclerView.ViewHolder {
    public MaterialCardView requestCard;
    public TextView status;
    public TextView user;
    public TextView position;
    public TextView token;
    public TextView phone;
    public LinearLayout layoutWithButtons;
    public MaterialButton acceptButton;
    public MaterialButton refuseButton;
    public CircleImageView userImage;

    public RequestRideViewHolder(@NonNull View itemView) {
        super(itemView);
        requestCard = itemView.findViewById(R.id.request_ride_card);
        status = itemView.findViewById(R.id.request_status);
        user = itemView.findViewById(R.id.request_user);
        position = itemView.findViewById(R.id.request_position);
        layoutWithButtons = itemView.findViewById(R.id.layout_with_button);
        acceptButton = itemView.findViewById(R.id.accept_button);
        refuseButton = itemView.findViewById(R.id.refuse_button);
        userImage = itemView.findViewById(R.id.user_avatar);
        token = itemView.findViewById(R.id.token_request);
        phone = itemView.findViewById(R.id.phone_number);
    }
}
