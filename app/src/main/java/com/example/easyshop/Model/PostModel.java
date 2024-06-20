package com.example.easyshop.Model;

public class PostModel {
    public String title;
    public String description;
    public String image;
    public double price;
    public String location;
    public String ownerID;

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


}
