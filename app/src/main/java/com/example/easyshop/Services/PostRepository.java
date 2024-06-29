package com.example.easyshop.Services;

import android.content.Context;

import com.example.easyshop.Model.PostModel;

import java.util.List;

public class PostRepository {
    private PostDao postDao;

    public PostRepository(Context context) {
        postDao = new PostDao(context);
    }

    public List<PostModel> getAllPosts() {
        return postDao.getAllPosts();
    }

    public void insertPost(PostModel post) {
        postDao.insertPost(post);
    }

    public void deleteAllPosts() {
        postDao.deleteAllPosts();
    }
}
