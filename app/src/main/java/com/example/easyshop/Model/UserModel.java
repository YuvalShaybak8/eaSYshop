package com.example.easyshop.Model;

import java.util.ArrayList;
import java.util.List;

public class UserModel {
        public String name;
        public String email;
        public String password;
        public String image;
        public ArrayList<PostModel> myPosts;
        public ArrayList<PostModel> wishList;
        public ArrayList<PostModel> myOrders;

        private static final String DEFAULT_IMAGE = "drawable/avatar1.png";

        public UserModel() {
            this.name = "";
            this.email = "";
            this.password = "";
            this.image = DEFAULT_IMAGE;
            this.myPosts = new ArrayList<>();
            this.wishList = new ArrayList<>();
            this.myOrders = new ArrayList<>();
        }

        public UserModel(String name, String email, String password) {
            this.name = name;
            this.email = email;
            this.password = password;
            this.image = DEFAULT_IMAGE;
            this.myPosts = new ArrayList<>();
            this.wishList = new ArrayList<>();
            this.myOrders = new ArrayList<>();
        }
}
