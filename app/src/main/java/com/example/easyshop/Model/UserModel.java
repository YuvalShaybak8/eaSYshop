package com.example.easyshop.Model;

import java.util.ArrayList;
import java.util.List;

public class UserModel {
        public String name;
        public String email;
        public String password;
        public String image;
        public ArrayList<PostModel> myPosts;
        public ArrayList<PostModel> whishList;
        public ArrayList<PostModel> myOrders;

        public UserModel() {
            //TO DO
        }

        public UserModel(String name, String email,String password) {
            this.name = name;
            this.email = email;
            this.password= password;
        }
}
