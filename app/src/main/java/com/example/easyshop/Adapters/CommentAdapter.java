package com.example.easyshop.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyshop.Model.CommentModel;
import com.example.easyshop.Model.UserModel;
import com.example.easyshop.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<CommentModel> commentList;
    private FirebaseFirestore db;

    public CommentAdapter(List<CommentModel> commentList) {
        this.commentList = commentList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentModel comment = commentList.get(position);

        db.collection("users").document(comment.getUserID()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserModel user = documentSnapshot.toObject(UserModel.class);
                        if (user != null) {
                            holder.userNameTextView.setText(user.getName());
                            if (user.getProfilePicUrl() != null && !user.getProfilePicUrl().isEmpty()) {
                                Picasso.get().load(user.getProfilePicUrl()).into(holder.commentProfileImage);
                            } else {
                                holder.commentProfileImage.setImageResource(R.drawable.avatar1); // Default avatar
                            }
                        }
                    }
                });

        holder.commentTextView.setText(comment.getCommentText());
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView, commentTextView;
        ImageView commentProfileImage;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            commentProfileImage = itemView.findViewById(R.id.commentProfileImage);
        }
    }
}
