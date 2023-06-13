package com.it.univai.holders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.it.univai.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageViewHold extends RecyclerView.ViewHolder {

    public TextView showMessage;
    public CircleImageView profileImage;

    public MessageViewHold(@NonNull View itemView) {
        super(itemView);
        showMessage = itemView.findViewById(R.id.show_message_chat);
        profileImage = itemView.findViewById(R.id.profile_image_message);
    }
}
