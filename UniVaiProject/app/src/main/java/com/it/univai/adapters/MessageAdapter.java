package com.it.univai.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.it.univai.R;
import com.it.univai.holders.MessageViewHold;
import com.it.univai.models.ChatModel;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHold> {
    public static final int MSG_LEFT = 0;
    public static final int MSG_RIGHT = 1;
    private Context context;
    private List<ChatModel> chats;
    private String imageUrl;

    public MessageAdapter(Context context, List<ChatModel> chats, String imageUrl) {
        this.context = context;
        this.chats = chats;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MessageViewHold onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == MSG_RIGHT) {
            view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
        }
        return new MessageViewHold(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHold holder, int position) {
        ChatModel chat = chats.get(position);
        holder.showMessage.setText(chat.getMessage());
        if(imageUrl != null && !imageUrl.equals("")) {
            Glide.with(context).load(imageUrl).into(holder.profileImage);
        }
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    @Override
    public int getItemViewType(int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null && chats.get(position).getSender().equals(user.getUid())) {
            return MSG_RIGHT;
        } else {
            return MSG_LEFT;
        }
    }
}
