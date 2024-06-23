package com.example.easyshop.Model;

public class CommentModel {
    private String commentText;
    private String userID;
    private String postID;

    public CommentModel() {
    }

    public CommentModel(String commentText, String userID, String postID) {
        this.commentText = commentText;
        this.userID = userID;
        this.postID = postID;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }
}
