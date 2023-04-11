package com.example.carsharing.models;

public class RequestWithUserModel {

    private AddressModel address;
    private String date;
    private String note;
    private Boolean active;
    private String name;
    private String surname;

    public RequestWithUserModel(AddressModel address, String date, String note, Boolean active, String name, String surname) {
        this.address = address;
        this.date = date;
        this.note = note;
        this.active = active;
        this.name = name;
        this.surname = surname;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
}
