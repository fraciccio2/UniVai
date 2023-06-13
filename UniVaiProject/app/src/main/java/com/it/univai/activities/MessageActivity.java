package com.it.univai.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.it.univai.R;
import com.it.univai.databinding.ActivityMessageBinding;
import com.it.univai.holders.MessageViewHold;
import com.it.univai.models.ChatModel;

import java.util.ArrayList;
import java.util.List;

import taimoor.sultani.sweetalert2.Sweetalert;

public class MessageActivity extends AppCompatActivity {

    ActivityMessageBinding binding;
    String userId;
    String userName;
    String userImage;
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseChats;
    FirebaseRecyclerAdapter<ChatModel, MessageViewHold> adapter;
    List<ChatModel> chats;
    Sweetalert alert;
    public static final int MSG_LEFT = 0;
    public static final int MSG_RIGHT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabaseChats = FirebaseDatabase.getInstance().getReference("chats");
        chats = new ArrayList<>();

        binding.arrowBack.setOnClickListener(view -> finish());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            alert = new Sweetalert(this, Sweetalert.PROGRESS_TYPE);
            alert.getProgressHelper().setBarColor(getResources().getColor(R.color.main_color));
            alert.setTitleText(getString(R.string.loading_text));
            alert.setCancelable(false);
            alert.show();

            userId = extras.getString(getString(R.string.user_id_text));
            userName = extras.getString(getString(R.string.user_name_text));
            userImage = extras.getString(getString(R.string.image_id_text));
            Glide.with(MessageActivity.this).load(userImage).into(binding.profileImage);
            binding.username.setText(userName);
            sendMessage();
            readMessage();

            binding.messageChatRecycler.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setStackFromEnd(true);
            binding.messageChatRecycler.setLayoutManager(linearLayoutManager);
            adapter = setFirebaseAdapter();
            binding.messageChatRecycler.setAdapter(adapter);
        }
    }

    private void sendMessage() {
        binding.btnSend.setOnClickListener(view -> {
            String messageTxt = binding.textSend.getText().toString();
            if (!messageTxt.equals("")) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    ChatModel message = new ChatModel(user.getUid(), userId, messageTxt);
                    chats.add(message);
                    mDatabaseChats.push().setValue(message);
                    binding.textSend.setText("");
                }
            } else {
                Toast.makeText(this, getString(R.string.empty_message_text), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void readMessage() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mDatabaseChats.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            ChatModel chat = data.getValue(ChatModel.class);
                            if (chat != null && (chat.getReceiver().equals(user.getUid()) && chat.getSender().equals(userId) || chat.getReceiver().equals(userId) && chat.getSender().equals(user.getUid()))) {
                                chats.add(chat);
                            }
                        }
                    }
                    alert.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Error", "exception", error.toException());
                    alert.dismiss();
                }
            });
        }
    }

    private FirebaseRecyclerAdapter<ChatModel, MessageViewHold> setFirebaseAdapter() {
        FirebaseRecyclerOptions<ChatModel> options = new FirebaseRecyclerOptions.Builder<ChatModel>()
                .setQuery(mDatabaseChats, ChatModel.class).build();
        return new FirebaseRecyclerAdapter<ChatModel, MessageViewHold>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MessageViewHold holder, int position, @NonNull ChatModel model) {
                ChatModel chat = chats.get(position);
                holder.showMessage.setText(chat.getMessage());
                if(userImage != null && !userImage.equals("") && holder.profileImage != null) {
                    Glide.with(getApplicationContext()).load(userImage).into(holder.profileImage);
                }
            }

            @NonNull
            @Override
            public MessageViewHold onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view;
                if(viewType == MSG_RIGHT) {
                    view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.chat_item_right, parent, false);
                } else {
                    view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.chat_item_left, parent, false);
                }
                return new MessageViewHold(view);
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
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}