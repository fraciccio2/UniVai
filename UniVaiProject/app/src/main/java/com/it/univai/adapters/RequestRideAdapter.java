package com.it.univai.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.it.univai.R;
import com.it.univai.fragments.RidesInFragment;
import com.it.univai.fragments.RidesOutFragment;
import com.it.univai.holders.RequestRideViewHolder;
import com.it.univai.models.RequestWithUserModel;

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
            if (requestRide.isSameAddress()) {
                holder.position.setText(requestRide.getLocation() + " (Stesso tuo luogo di partenza)");
            } else {
                holder.position.setText(requestRide.getLocation() + " (Diverso dal tuo luogo di partenza)");
            }
        }
        holder.user.setText(requestRide.getUserName());
        int dpSize = 2;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpSize, dm);
        holder.requestCard.setStrokeWidth(Math.round(strokeWidth));
        holder.userImage.setOnClickListener(view -> {
            Context context = fragmentOut != null ? fragmentOut.getContext() : fragmentIn.getContext();
            Dialog dialog = new Dialog(context, R.style.WindowFullScreen);
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.alert_image);
            ImageView imageView = dialog.findViewById(R.id.image_full_screen);
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.mipmap.ic_launcher_round);
            Glide.with(context).load(requestRide.getUserAvatar()).apply(options).into(imageView);
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
                holder.phone.setVisibility(View.VISIBLE);
                holder.phone.setText(context.getString(R.string.phone_number_val_text) + " " + requestRide.getPhoneNumber());
                holder.phone.setOnClickListener(view -> {
                    Intent intentDial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "+39" + requestRide.getPhoneNumber()));
                    context.startActivity(intentDial);
                });
                break;
            case PENDING:
                holder.requestCard.setStrokeColor(context.getResources().getColor(R.color.warning_border));
                holder.status.setText(context.getString(R.string.pending_text));
                holder.status.setBackground(AppCompatResources.getDrawable(context, R.drawable.warnig_status));
                if (!isOutRequest) {
                    holder.layoutWithButtons.setVisibility(View.VISIBLE);
                    holder.refuseButton.setOnClickListener(view -> fragmentIn.refuseRequest(requestRide.getTokenRequest(), requestRide.getUserUid()));
                    holder.acceptButton.setOnClickListener(view -> fragmentIn.acceptRequest(requestRide.getTokenRequest(), requestRide.getUserUid()));
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
