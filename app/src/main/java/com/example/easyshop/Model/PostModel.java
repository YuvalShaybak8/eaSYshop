package com.example.easyshop.Model;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PostModel {
    private String postID;
    private String title;
    private String description;
    private String image;
    private double price;
    private String location;
    private String ownerID;
    private Timestamp timestamp;
    private boolean isPurchased;
    private String buyerID;
    private List<CommentModel> comments;

    public PostModel() {
        comments = new ArrayList<>();
    }

    public PostModel(String postID, String title, String description, String image, double price, String location, String ownerID, Timestamp timestamp, boolean isPurchased, String buyerID, List<CommentModel> comments) {
        this.postID = postID;
        this.title = title;
        this.description = description;
        this.image = image;
        this.price = price;
        this.location = location;
        this.ownerID = ownerID;
        this.timestamp = timestamp;
        this.isPurchased = isPurchased;
        this.buyerID = buyerID;
        this.comments = comments;
    }

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

    public boolean isPurchased() {
        return isPurchased;
    }

    public void setPurchased(boolean purchased) {
        isPurchased = purchased;
    }

    public String getBuyerID() {
        return buyerID;
    }

    public void setBuyerID(String buyerID) {
        this.buyerID = buyerID;
    }

    public List<CommentModel> getComments() {
        return comments;
    }

    public void setComments(List<CommentModel> comments) {
        this.comments = comments;
    }
}
