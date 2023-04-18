package com.example.carsharing.models;

import com.example.carsharing.enums.StatusEnum;

public class RequestWithUserModel {
    private StatusEnum status;
    private String userName;
    private String rideId;
    private String tokenRequest;
    private String location;
    private String date;
    private boolean out;

    public RequestWithUserModel(StatusEnum status, String userName, String rideId, String tokenRequest, String location, String date, boolean out) {
        this.status = status;
        this.userName = userName;
        this.rideId = rideId;
        this.tokenRequest = tokenRequest;
        this.location = location;
        this.date = date;
        this.out = out;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getTokenRequest() {
        return tokenRequest;
    }

    public void setTokenRequest(String tokenRequest) {
        this.tokenRequest = tokenRequest;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isOut() {
        return out;
    }

    public void setOut(boolean out) {
        this.out = out;
    }
}
