package com.example.carsharing.models;

public class RequestModel {

    private AddressModel address;
    private String date;
    private String note;
    private Boolean active;

    public RequestModel(AddressModel address, String date, String note, Boolean active) {
        this.address = address;
        this.date = date;
        this.note = note;
        this.active = active;
    }

    public AddressModel getAddress() {
        return address;
    }

    public void setAddress(AddressModel address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
