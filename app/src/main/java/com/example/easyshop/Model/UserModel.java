package com.example.easyshop.Model;

import com.example.easyshop.Model.PostModel;

import java.util.ArrayList;

public class UserModel {
    public String name;
    public String email;
    public String password;
    public String profilePicUrl; // Use this name consistently
    public ArrayList<PostModel> myPosts;
    public ArrayList<PostModel> wishList;
    public ArrayList<PostModel> myOrders;

    private static final String DEFAULT_IMAGE = "drawable/avatar1.png";

    public UserModel() {
        this.name = "";
        this.email = "";
        this.password = "";
        this.profilePicUrl = DEFAULT_IMAGE;
        this.myPosts = new ArrayList<>();
        this.wishList = new ArrayList<>();
        this.myOrders = new ArrayList<>();
    }

    public UserModel(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.profilePicUrl = DEFAULT_IMAGE;
        this.myPosts = new ArrayList<>();
        this.wishList = new ArrayList<>();
        this.myOrders = new ArrayList<>();
    }

    // Getter method for username
    public String getUsername() {
        return name;
    }

    // Getter method for email
    public String getEmail() {
        return email;
    }

    // Getter method for profilePicUrl
    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    // Getter method for password (Use this with caution)
    public String getPassword() {
        return password;
    }

    // Setter method for password
    public void setPassword(String password) {
        this.password = password;
    }
}
