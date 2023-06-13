package com.it.univai.models;

public class UserChatModel {

    private String id;
    private String name;
    private String surname;
    private String userImage;

    public UserChatModel(String id, String name, String surname, String userImage) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.userImage = userImage;
    }

    public UserChatModel() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }
}
