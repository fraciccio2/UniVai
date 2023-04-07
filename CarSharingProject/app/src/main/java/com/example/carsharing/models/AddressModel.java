package com.example.carsharing.models;

import com.google.android.gms.maps.model.LatLng;

public class AddressModel {

    private String location;
    private LatLng coordinate;

    public AddressModel(String location, LatLng coordinate) {
        this.location = location;
        this.coordinate = coordinate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LatLng getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(LatLng coordinate) {
        this.coordinate = coordinate;
    }
}
