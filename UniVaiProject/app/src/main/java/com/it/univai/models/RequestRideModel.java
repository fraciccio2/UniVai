package com.it.univai.models;

import com.it.univai.enums.StatusEnum;

public class RequestRideModel {

    private StatusEnum status;
    private String creatorUser;
    private String requesterUser;
    private String rideId;
    private String location;
    private boolean sameAddress;

    public RequestRideModel(StatusEnum status, String creatorUser, String requesterUser, String rideId, String location, boolean sameAddress) {
        this.status = status;
        this.creatorUser = creatorUser;
        this.requesterUser = requesterUser;
        this.rideId = rideId;
        this.location = location;
        this.sameAddress = sameAddress;
    }

    public RequestRideModel(){}

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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isSameAddress() {
        return sameAddress;
    }

    public void setSameAddress(boolean sameAddress) {
        this.sameAddress = sameAddress;
    }
}
