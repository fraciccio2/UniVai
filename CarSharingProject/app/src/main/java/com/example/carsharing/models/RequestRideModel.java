package com.example.carsharing.models;

import com.example.carsharing.enums.StatusEnum;

public class RequestRideModel {

    private StatusEnum status;
    private String creatorUser;
    private String requesterUser;
    private String rideId;

    public RequestRideModel(StatusEnum status, String creatorUser, String requesterUser, String rideId) {
        this.status = status;
        this.creatorUser = creatorUser;
        this.requesterUser = requesterUser;
        this.rideId = rideId;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public String getCreatorUser() {
        return creatorUser;
    }

    public void setCreatorUser(String creatorUser) {
        this.creatorUser = creatorUser;
    }

    public String getRequesterUser() {
        return requesterUser;
    }

    public void setRequesterUser(String requesterUser) {
        this.requesterUser = requesterUser;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }
}
