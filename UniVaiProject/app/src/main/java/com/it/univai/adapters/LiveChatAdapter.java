package com.it.univai.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.it.univai.R;
import com.it.univai.activities.MessageActivity;
import com.it.univai.holders.LiveChatViewHold;
import com.it.univai.models.UserChatModel;

import java.util.List;

public class LiveChatAdapter extends RecyclerView.Adapter<LiveChatViewHold> {

    private Context context;
    private List<UserChatModel> users;

    public LiveChatAdapter(Context context, List<UserChatModel> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public LiveChatViewHold onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_chat, parent, false);
        return new LiveChatViewHold(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LiveChatViewHold holder, int position) {
        UserChatModel user = users.get(position);
        holder.username.setText(user.getName() + " " +user.getSurname());
        if(user.getUserImage() != null || !user.getUserImage().equals("")) {
            Glide.with(context).load(user.getUserImage()).into(holder.profileImage);
        }

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context.getApplicationContext(), MessageActivity.class);
            intent.putExtra(context.getString(R.string.user_id_text), user.getId());
            intent.putExtra(context.getString(R.string.image_id_text), user.getUserImage());
            intent.putExtra(context.getString(R.string.user_name_text), user.getName() + " " +user.getSurname());
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}
