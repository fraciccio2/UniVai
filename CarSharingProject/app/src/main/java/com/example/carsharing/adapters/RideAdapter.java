package com.example.carsharing.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carsharing.R;
import com.example.carsharing.activities.RidesListActivity;
import com.example.carsharing.fragments.BookRideFragment;
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
        SimpleDateFormat formatter = new SimpleDateFormat(context.getString(R.string.date_pattern));
        RideWithUserModel rideUser = rideUserList.get(position);
        Glide.with(context).load(rideUser.getUserImage()).into(holder.avatarUser);
        holder.rideNote.setText(rideUser.getNote());
        holder.rideDate.setText(formatter.format(new Date(rideUser.getDate())));
        holder.rideAddress.setText(rideUser.getAddress().getLocation());
        holder.nameUser.setText(rideUser.getName() + " "+rideUser.getSurname());
        holder.card.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putString(context.getString(R.string.ride_id_text), rideUser.getId());
            bundle.putString(context.getString(R.string.user_name_text), rideUser.getName() + " "+rideUser.getSurname());
            bundle.putString(context.getString(R.string.user_id_text), rideUser.getUserId());
            BookRideFragment fragment = new BookRideFragment();
            fragment.setArguments(bundle);
            RidesListActivity activity = (RidesListActivity) view.getContext();
            RecyclerView recyclerView = activity.findViewById(R.id.recycler_view);
            recyclerView.setVisibility(View.GONE);
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.book_ride, fragment);
            transaction.commit();
        });
    }

    @Override
    public int getItemCount() {
        return rideUserList.size();
    }
}
