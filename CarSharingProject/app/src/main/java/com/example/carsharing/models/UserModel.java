package com.example.carsharing.models;

import java.util.HashMap;

public class UserModel {

    private String name;
    private String surname;
    private AddressModel address;
    private String university;
    private Boolean hasCar;

    public UserModel(String name, String surname, AddressModel address, String university, Boolean hasCar) {
        this.name = name;
        this.surname = surname;
        this.address = address;
        this.university = university;
        this.hasCar = hasCar;
    }

    public UserModel(){}

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

    public AddressModel getAddress() {
        return address;
    }

    public void setAddress(AddressModel address) {
        this.address = address;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public Boolean getHasCar() {
        return hasCar;
    }

    public void setHasCar(Boolean hasCar) {
        this.hasCar = hasCar;
    }
}
