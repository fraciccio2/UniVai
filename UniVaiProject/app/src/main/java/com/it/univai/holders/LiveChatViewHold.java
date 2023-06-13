package com.it.univai.holders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.it.univai.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class LiveChatViewHold extends RecyclerView.ViewHolder {

    public CircleImageView profileImage;
    public TextView username;


    public LiveChatViewHold(@NonNull View itemView) {
        super(itemView);
        profileImage = itemView.findViewById(R.id.profile_image);
        username = itemView.findViewById(R.id.username);
    }
}
