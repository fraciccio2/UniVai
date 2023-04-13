package com.example.carsharing.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carsharing.R;
import com.example.carsharing.holders.RequestViewHolder;
import com.example.carsharing.models.RequestWithUserModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestViewHolder> {
    private final List<RequestWithUserModel> requestUserList;
    private final Context context;

    public RequestAdapter(List<RequestWithUserModel> requestList, Context context) {
        this.requestUserList = requestList;
        this.context = context;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_card, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        RequestWithUserModel requestUser = requestUserList.get(position);
        Glide.with(context).load(requestUser.getUserImage()).into(holder.avatarUser);
        holder.requestNote.setText(requestUser.getNote());
        holder.requestDate.setText(formatter.format(new Date(requestUser.getDate())));
        holder.requestAddress.setText(requestUser.getAddress().getLocation());
        holder.nameUser.setText(requestUser.getName() + " "+requestUser.getSurname());
        holder.card.setOnClickListener(view -> Log.i("Info", requestUser.getId())); //TODO aprire fragment con più informazioni
    }

    @Override
    public int getItemCount() {
        return requestUserList.size();
    }
}
