package com.example.mycontact;
public class Contact {
    private String name;
    private String phoneNumber;
    private String photoUri;

    public Contact(String name, String phoneNumber,String photoUri) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.photoUri = photoUri;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPhotoUri() {
        return photoUri;
    }
}