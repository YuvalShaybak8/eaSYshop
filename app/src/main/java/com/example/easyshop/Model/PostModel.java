package com.example.easyshop.Model;

import com.google.firebase.Timestamp;

public class PostModel {
    private String postID;
    private String title;
    private String description;
    private String image;
    private double price;
    private String location;
    private String ownerID;
    private Timestamp timestamp;

    // No-argument constructor
    public PostModel() {
    }

    public PostModel(String postID, String title, String description, String image, double price, String location, String ownerID, Timestamp timestamp) {
        this.postID = postID;
        this.title = title;
        this.description = description;
        this.image = image;
        this.price = price;
        this.location = location;
        this.ownerID = ownerID;
        this.timestamp = timestamp;
    }

    // Getter and Setter methods
    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
