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
import com.example.carsharing.holders.RideViewHolder;
import com.example.carsharing.models.RideWithUserModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RideAdapter extends RecyclerView.Adapter<RideViewHolder> {
    private final List<RideWithUserModel> rideUserList;
    private final Context context;

    public RideAdapter(List<RideWithUserModel> rideUserList, Context context) {
        this.rideUserList = rideUserList;
        this.context = context;
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ride_card, parent, false);
        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        RideWithUserModel rideUser = rideUserList.get(position);
        Glide.with(context).load(rideUser.getUserImage()).into(holder.avatarUser);
        holder.rideNote.setText(rideUser.getNote());
        holder.rideDate.setText(formatter.format(new Date(rideUser.getDate())));
        holder.rideAddress.setText(rideUser.getAddress().getLocation());
        holder.nameUser.setText(rideUser.getName() + " "+rideUser.getSurname());
        holder.card.setOnClickListener(view -> Log.i("Info", rideUser.getId())); //TODO aprire fragment con più informazioni
    }

    @Override
    public int getItemCount() {
        return rideUserList.size();
    }
}
