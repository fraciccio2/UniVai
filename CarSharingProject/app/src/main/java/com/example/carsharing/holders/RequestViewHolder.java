package com.example.carsharing.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carsharing.R;

public class RequestViewHolder extends RecyclerView.ViewHolder {
    public ImageView avatarUser;
    public TextView nameUser;
    public TextView requestAddress;
    public TextView requestDate;
    public TextView requestNote;
    public LinearLayout card;

    public RequestViewHolder(@NonNull View itemView) {
        super(itemView);
        avatarUser = itemView.findViewById(R.id.image_user);
        nameUser = itemView.findViewById(R.id.user_name);
        requestAddress = itemView.findViewById(R.id.request_address);
        requestDate = itemView.findViewById(R.id.request_date);
        requestNote = itemView.findViewById(R.id.request_note);
        card = itemView.findViewById(R.id.card_request);
    }
}
