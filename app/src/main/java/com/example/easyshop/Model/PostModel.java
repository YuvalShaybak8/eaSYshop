package com.example.easyshop.Model;

public class PostModel {
    public String title;
    public String description;
    public String image;
    public double price;
    public String location;
    public String ownerID;

    // No-argument constructor
    public PostModel() {
    }

    public PostModel(String title, String description, String image, double price, String location, String ownerID) {
        this.title = title;
        this.description = description;
        this.image = image;
        this.price = price;
        this.location = location;
        this.ownerID = ownerID;
    }

    public PostModel(String title, String description, double price, String location, String ownerID) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.location = location;
        this.ownerID = ownerID;
    }

    // Getter and Setter methods (optional but recommended)
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
}