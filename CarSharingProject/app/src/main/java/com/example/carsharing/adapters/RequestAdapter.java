package com.example.carsharing.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carsharing.R;
import com.example.carsharing.holders.RequestViewHolder;
import com.example.carsharing.models.RequestModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestViewHolder> {
    private List<RequestModel> requestList;

    public RequestAdapter(List<RequestModel> requestList) {
        this.requestList = requestList;
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
        RequestModel request = requestList.get(position);
        //holder.avatarUser.setImageURI(); //TODO settare immagine
        holder.requestNote.setText(request.getNote());
        holder.requestDate.setText(formatter.format(new Date(request.getDate())));
        holder.requestAddress.setText(request.getAddress().getLocation());
        holder.nameUser.setText("Ture Polpetta"); //TODO settare nome
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }
}
