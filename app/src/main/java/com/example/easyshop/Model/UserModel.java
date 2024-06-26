package com.example.easyshop.Model;

import java.util.ArrayList;
import java.util.List;

public class UserModel {
    public String name;
    public String email;
    public String password;
    public String profilePicUrl;
    public ArrayList<PostModel> myPosts;
    public ArrayList<PostModel> wishList;
    public ArrayList<PostModel> myOrders;
    public List<CommentModel> comments;
    public boolean isLoggedIn;

    private static final String DEFAULT_IMAGE = "drawable/avatar1.png";

    public UserModel() {
        this.name = "";
        this.email = "";
        this.password = "";
        this.profilePicUrl = DEFAULT_IMAGE;
        this.myPosts = new ArrayList<>();
        this.wishList = new ArrayList<>();
        this.myOrders = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.isLoggedIn = false;
    }

    public UserModel(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.profilePicUrl = DEFAULT_IMAGE;
        this.myPosts = new ArrayList<>();
        this.wishList = new ArrayList<>();
        this.myOrders = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.isLoggedIn = false;
    }

    // Getters and setters for isLoggedIn
    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<CommentModel> getComments() {
        return comments;
    }

    public void setComments(List<CommentModel> comments) {
        this.comments = comments;
    }

    public List<PostModel> getWishList() {
        return wishList;
    }

    public void setWishList(List<PostModel> wishList) {
        this.wishList = (ArrayList<PostModel>) wishList;
    }

    public List<PostModel> getMyOrders() {
        return myOrders;
    }

    public void setMyOrders(List<PostModel> myOrders) {
        this.myOrders = (ArrayList<PostModel>) myOrders;
    }

    public List<PostModel> getMyPosts() {
        return myPosts;
    }

    public void setMyPosts(List<PostModel> myPosts) {
        this.myPosts = (ArrayList<PostModel>) myPosts;
    }
}
