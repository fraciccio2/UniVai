package com.example.carsharing.adapters;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carsharing.R;
import com.example.carsharing.holders.RequestRideViewHolder;
import com.example.carsharing.models.RequestWithUserModel;

import java.util.List;

public class RequestRideAdapter extends RecyclerView.Adapter<RequestRideViewHolder> {
    private final List<RequestWithUserModel> requestRideList;
    private final Context context;

    public RequestRideAdapter(Context context, List<RequestWithUserModel> requestRideList) {
        this.context = context;
        this.requestRideList = requestRideList;
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
        if(requestRide.isOut()) {
            holder.labelUser.setText(context.getString(R.string.created_by_text));
            holder.labelPosition.setText(context.getString(R.string.appointment_text));
            holder.position.setText(requestRide.getLocation() + " " +requestRide.getDate());
        }
        holder.user.setText(requestRide.getUserName());
        int dpSize = 2;
        DisplayMetrics dm = context.getResources().getDisplayMetrics() ;
        float strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpSize, dm);
        holder.requestCard.setStrokeWidth(Math.round(strokeWidth));
        switch (requestRide.getStatus()) {
            case ACCEPT:
                holder.requestCard.setStrokeColor(context.getResources().getColor(R.color.success_border));
                holder.status.setText(context.getString(R.string.accept_text));
                holder.status.setBackground(AppCompatResources.getDrawable(context, R.drawable.success_status));
                holder.status.setTextColor(context.getResources().getColor(R.color.white));
                break;
            case PENDING:
                holder.requestCard.setStrokeColor(context.getResources().getColor(R.color.warning_border));
                holder.status.setText(context.getString(R.string.pending_text));
                holder.status.setBackground(AppCompatResources.getDrawable(context, R.drawable.warnig_status));
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
