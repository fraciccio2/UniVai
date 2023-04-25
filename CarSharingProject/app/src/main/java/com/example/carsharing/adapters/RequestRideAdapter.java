package com.example.carsharing.adapters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.carsharing.R;
import com.example.carsharing.fragments.RidesInFragment;
import com.example.carsharing.fragments.RidesOutFragment;
import com.example.carsharing.holders.RequestRideViewHolder;
import com.example.carsharing.models.RequestWithUserModel;

import java.util.List;

public class RequestRideAdapter extends RecyclerView.Adapter<RequestRideViewHolder> {
    private final List<RequestWithUserModel> requestRideList;
    private final Context context;
    private RidesInFragment fragmentIn;
    private RidesOutFragment fragmentOut;

    public RequestRideAdapter(Context context, RidesInFragment fragment, List<RequestWithUserModel> requestRideList) {
        this.context = context;
        this.fragmentIn = fragment;
        this.requestRideList = requestRideList;
    }

    public RequestRideAdapter(Context context, RidesOutFragment fragment, List<RequestWithUserModel> requestRideList) {
        this.context = context;
        this.requestRideList = requestRideList;
        this.fragmentOut = fragment;
    }

    @NonNull
    @Override
    public RequestRideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_ride_card, parent, false);
        return new RequestRideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestRideViewHolder holder, int position) {
        RequestWithUserModel requestRide = requestRideList.get(position);
        boolean isOutRequest = requestRide.isOut();
        if(isOutRequest) {
            holder.position.setText(requestRide.getLocation() + " " +requestRide.getDate());
        } else {
            if(requestRide.getLocation() != null) {
                holder.position.setText(requestRide.getLocation());
            } else {
                holder.position.setText(context.getString(R.string.some_address_info_text));
            }
        }
        holder.user.setText(requestRide.getUserName());
        int dpSize = 2;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpSize, dm);
        holder.requestCard.setStrokeWidth(Math.round(strokeWidth));
        holder.userImage.setOnClickListener(view -> {
            Context context = fragmentOut != null ? fragmentOut.getContext() : fragmentIn.getContext();
            Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));
            ImageView imageView = new ImageView(context);
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.mipmap.ic_launcher_round);
            Glide.with(context).load(requestRide.getUserAvatar()).apply(options).into(imageView);
            dialog.addContentView(imageView, new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        });
        Glide.with(context).load(requestRide.getUserAvatar()).into(holder.userImage);
        switch (requestRide.getStatus()) {
            case ACCEPT:
                holder.requestCard.setStrokeColor(context.getResources().getColor(R.color.success_border));
                holder.status.setText(context.getString(R.string.accepted_text));
                holder.status.setBackground(AppCompatResources.getDrawable(context, R.drawable.success_status));
                holder.status.setTextColor(context.getResources().getColor(R.color.white));
                holder.token.setVisibility(View.VISIBLE);
                holder.token.setText(context.getString(R.string.token_text) + " " + requestRide.getTokenRequest());
                break;
            case PENDING:
                holder.requestCard.setStrokeColor(context.getResources().getColor(R.color.warning_border));
                holder.status.setText(context.getString(R.string.pending_text));
                holder.status.setBackground(AppCompatResources.getDrawable(context, R.drawable.warnig_status));
                if(!isOutRequest) {
                    holder.layoutWithButtons.setVisibility(View.VISIBLE);
                    holder.refuseButton.setOnClickListener(view -> fragmentIn.refuseRequest(requestRide.getTokenRequest()));
                    holder.acceptButton.setOnClickListener(view -> fragmentIn.acceptRequest(requestRide.getTokenRequest()));
                }
                break;
            case REFUSED:
                holder.requestCard.setStrokeColor(context.getResources().getColor(R.color.danger_border));
                holder.status.setText(context.getString(R.string.refused_text));
                holder.status.setBackground(AppCompatResources.getDrawable(context, R.drawable.danger_status));
                holder.status.setTextColor(context.getResources().getColor(R.color.white));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return requestRideList.size();
    }
}
