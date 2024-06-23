package com.example.easyshop.Model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserModel {
    public String name;
    public String email;
    public String password;
    public String profilePicUrl;
    public ArrayList<PostModel> myPosts;
    public ArrayList<PostModel> wishList;
    public ArrayList<PostModel> myOrders;
    public List<CommentModel> comments; // Add this line

    private static final String DEFAULT_IMAGE = "drawable/avatar1.png";

    public UserModel() {
        this.name = "";
        this.email = "";
        this.password = "";
        this.profilePicUrl = DEFAULT_IMAGE;
        this.myPosts = new ArrayList<>();
        this.wishList = new ArrayList<>();
        this.myOrders = new ArrayList<>();
        this.comments = new ArrayList<>(); // Initialize comments
    }

    public UserModel(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.profilePicUrl = DEFAULT_IMAGE;
        this.myPosts = new ArrayList<>();
        this.wishList = new ArrayList<>();
        this.myOrders = new ArrayList<>();
        this.comments = new ArrayList<>(); // Initialize comments
    }

    // Getter method for username
    public String getName() {
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

    public List<CommentModel> getComments() {
        return comments;
    }

    public void setComments(List<CommentModel> comments) {
        this.comments = comments;
    }

    // Getter method for wishList
    public List<PostModel> getWishList() {
        return wishList;
    }

    // Setter method for wishList
    public void setWishList(List<PostModel> wishList) {
        this.wishList = (ArrayList<PostModel>) wishList;
    }
}
