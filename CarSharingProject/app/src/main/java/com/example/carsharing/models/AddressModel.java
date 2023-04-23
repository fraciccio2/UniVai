package com.example.carsharing.models;

public class AddressModel {

    private String location;
    private LatLonModel coordinate;

    public AddressModel(String location, LatLonModel coordinate) {
        this.location = location;
        this.coordinate = coordinate;
    }

    public AddressModel() {}

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LatLonModel getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(LatLonModel coordinate) {
        this.coordinate = coordinate;
    }
}
