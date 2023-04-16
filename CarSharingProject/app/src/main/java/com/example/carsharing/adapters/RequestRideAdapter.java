package com.example.carsharing.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carsharing.R;
import com.example.carsharing.holders.RequestRideViewHolder;
import com.example.carsharing.models.RequestRideModel;

import java.util.List;

public class RequestRideAdapter extends RecyclerView.Adapter<RequestRideViewHolder> {
    private final List<RequestRideModel> requestRideList;

    public RequestRideAdapter(List<RequestRideModel> requestRideList) {
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
        //TODO cambiare tipo
    }

    @Override
    public int getItemCount() {
        return requestRideList.size();
    }
}
