package com.example.carsharing.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.carsharing.R;
import com.example.carsharing.activities.BookRideActivity;
import com.example.carsharing.activities.RidesListActivity;
import com.example.carsharing.holders.RideViewHolder;
import com.example.carsharing.models.RideWithUserModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RideAdapter extends RecyclerView.Adapter<RideViewHolder> {
    private final List<RideWithUserModel> rideUserList;
    private final Context context;
    private final RidesListActivity activity;

    public RideAdapter(List<RideWithUserModel> rideUserList, Context context, RidesListActivity activity) {
        this.rideUserList = rideUserList;
        this.context = context;
        this.activity = activity;
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
        holder.avatarUser.setOnClickListener(view -> {
            Dialog dialog = new Dialog(activity, R.style.WindowFullScreen);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.alert_image);
            ImageView imageView = dialog.findViewById(R.id.image_full_screen);
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.mipmap.ic_launcher_round);
            Glide.with(activity).load(rideUser.getUserImage()).apply(options).into(imageView);
            dialog.show();
        });
        Glide.with(context).load(rideUser.getUserImage()).into(holder.avatarUser);
        holder.rideNote.setText(rideUser.getNote());
        holder.rideDate.setText(formatter.format(new Date(rideUser.getDate())));
        holder.rideAddress.setText(rideUser.getAddress().getLocation());
        holder.nameUser.setText(rideUser.getName() + " "+rideUser.getSurname());
        holder.card.setOnClickListener(view -> {
            Intent intent = new Intent(context.getApplicationContext(), BookRideActivity.class);
            intent.putExtra(context.getString(R.string.ride_id_text), rideUser.getId());
            intent.putExtra(context.getString(R.string.user_name_text), rideUser.getName() + " "+rideUser.getSurname());
            intent.putExtra(context.getString(R.string.user_id_text), rideUser.getUserId());
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        });
    }

    @Override
    public int getItemCount() {
        return rideUserList.size();
    }
}
