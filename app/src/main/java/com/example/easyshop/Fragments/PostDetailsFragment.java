package com.example.easyshop.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyshop.Adapters.CommentAdapter;
import com.example.easyshop.Model.CommentModel;
import com.example.easyshop.Model.PostModel;
import com.example.easyshop.Model.UserModel;
import com.example.easyshop.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PostDetailsFragment extends Fragment {

    private PostModel post;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView commentsRecyclerView;
    private CommentAdapter commentAdapter;
    private List<CommentModel> commentList;

    public PostDetailsFragment(PostModel post) {
        this.post = post;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_details, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        ImageView profileImage = view.findViewById(R.id.profileImage);
        TextView userNameTextView = view.findViewById(R.id.userNameTextView);
        TextView postTimestampTextView = view.findViewById(R.id.postTimestampTextView);
        TextView itemTitleTextView = view.findViewById(R.id.itemTitleTextView);
        TextView itemDescriptionTextView = view.findViewById(R.id.itemDescriptionTextView);
        TextView itemPriceTextView = view.findViewById(R.id.itemPriceTextView);
        TextView itemLocationTextView = view.findViewById(R.id.itemLocationTextView);
        ImageView itemImageView = view.findViewById(R.id.itemImageView);
        EditText commentEditText = view.findViewById(R.id.commentEditText);
        ImageView sendCommentButton = view.findViewById(R.id.sendCommentButton);
        commentsRecyclerView = view.findViewById(R.id.commentsRecyclerView);

        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentList = new ArrayList<>(post.getComments());
        commentAdapter = new CommentAdapter(commentList);
        commentsRecyclerView.setAdapter(commentAdapter);

        db.collection("users").document(post.getOwnerID()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserModel user = documentSnapshot.toObject(UserModel.class);
                        if (user != null) {
                            userNameTextView.setText(user.getName());
                            if (user.getProfilePicUrl() != null && !user.getProfilePicUrl().isEmpty()) {
                                Picasso.get().load(user.getProfilePicUrl()).into(profileImage);
                            } else {
                                profileImage.setImageResource(R.drawable.avatar1);
                            }
                        }
                    }
                });

        itemTitleTextView.setText(post.getTitle());
        itemDescriptionTextView.setText(post.getDescription());
        itemPriceTextView.setText("Price: $" + post.getPrice());
        itemLocationTextView.setText("Pickup address: " + post.getLocation());
        Picasso.get().load(post.getImage()).into(itemImageView);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
        String formattedDate = sdf.format(post.getTimestamp().toDate());
        postTimestampTextView.setText(formattedDate);

        sendCommentButton.setOnClickListener(v -> {
            String commentText = commentEditText.getText().toString();
            if (!commentText.isEmpty()) {
                String userID = mAuth.getCurrentUser().getUid();
                String postID = post.getPostID();

                CommentModel comment = new CommentModel(commentText, userID, postID);
                post.getComments().add(comment);

                db.collection("posts").document(postID)
                        .update("comments", post.getComments())
                        .addOnSuccessListener(aVoid -> {
                            commentList.add(comment);
                            commentAdapter.notifyDataSetChanged();
                            commentEditText.setText("");

                            db.collection("users").document(userID)
                                    .update("comments", com.google.firebase.firestore.FieldValue.arrayUnion(comment))
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update user's comments", Toast.LENGTH_SHORT).show());
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add comment", Toast.LENGTH_SHORT).show());
            }
        });

        return view;
    }
}
