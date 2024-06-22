package com.example.easyshop.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.fragment.app.FragmentTransaction;

import com.example.easyshop.Fragments.PaymentFragment;
import com.example.easyshop.Fragments.HomeFragment;
import com.example.easyshop.Model.PostModel;
import com.example.easyshop.Model.UserModel;
import com.example.easyshop.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HOME = 0;
    private static final int VIEW_TYPE_MY_POSTS = 1;

    private Context context;
    private List<PostModel> postList;
    private FirebaseFirestore db;
    private boolean isMyPostsPage;
    private String currentUserID; // Added to store current user's ID

    public PostAdapter(Context context, List<PostModel> postList, boolean isMyPostsPage, String currentUserID) {
        this.context = context;
        this.postList = postList;
        this.db = FirebaseFirestore.getInstance();
        this.isMyPostsPage = isMyPostsPage;
        this.currentUserID = currentUserID; // Initialize with current user's ID
    }

    @Override
    public int getItemViewType(int position) {
        return isMyPostsPage ? VIEW_TYPE_MY_POSTS : VIEW_TYPE_HOME;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HOME) {
            View view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
            return new HomeViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.my_post_item, parent, false);
            return new MyPostsViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PostModel post = postList.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_HOME) {
            HomeViewHolder homeViewHolder = (HomeViewHolder) holder;
            bindHomeViewHolder(homeViewHolder, post);
        } else {
            MyPostsViewHolder myPostsViewHolder = (MyPostsViewHolder) holder;
            bindMyPostsViewHolder(myPostsViewHolder, post);
        }
    }

    private void bindHomeViewHolder(HomeViewHolder holder, PostModel post) {
        holder.itemTitleTextView.setText(post.getTitle());
        holder.itemDescriptionTextView.setText(post.getDescription());
        holder.itemPriceTextView.setText("Price: $" + post.getPrice());
        holder.itemLocationTextView.setText("Pickup address: " + post.getLocation());

        // Load post image
        Picasso.get().load(post.getImage()).into(holder.itemImageView);

        // Format and set timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
        String formattedDate = sdf.format(post.getTimestamp().toDate());
        holder.postTimestampTextView.setText(formattedDate);

        // Fetch user details based on ownerID
        db.collection("users").document(post.getOwnerID()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserModel user = documentSnapshot.toObject(UserModel.class);
                        if (user != null) {
                            holder.userNameTextView.setText(user.getName());
                            if (user.getProfilePicUrl() != null && !user.getProfilePicUrl().isEmpty()) {
                                Picasso.get().load(user.getProfilePicUrl()).into(holder.profileImage);
                            } else {
                                holder.profileImage.setImageResource(R.drawable.avatar1); // Default avatar
                            }
                        }
                    }
                });

        // Hide "Buy" button if the post belongs to the current user
        if (post.getOwnerID().equals(currentUserID)) {
            holder.buyButton.setVisibility(View.GONE);
        } else {
            holder.buyButton.setVisibility(View.VISIBLE);
        }

        // Set click listener for the buy button
        holder.buyButton.setOnClickListener(v -> {
            // Navigate to the PaymentFragment
            FragmentActivity activity = (FragmentActivity) context;
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new PaymentFragment());
            transaction.addToBackStack(null); // Optional: Adds the transaction to the back stack so the user can navigate back
            transaction.commit();
        });
    }

    private void bindMyPostsViewHolder(MyPostsViewHolder holder, PostModel post) {
        holder.itemTitleTextView.setText(post.getTitle());
        holder.itemDescriptionTextView.setText(post.getDescription());
        holder.itemPriceTextView.setText("Price: $" + post.getPrice());
        holder.itemLocationTextView.setText("Pickup address: " + post.getLocation());

        // Load post image
        Picasso.get().load(post.getImage()).into(holder.itemImageView);

        // Format and set timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("dd.M.yy 'at' HH:mm", Locale.getDefault());
        String formattedDate = sdf.format(post.getTimestamp().toDate());
        holder.postTimestampTextView.setText(formattedDate);

        // Fetch user details based on ownerID
        db.collection("users").document(post.getOwnerID()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserModel user = documentSnapshot.toObject(UserModel.class);
                        if (user != null) {
                            holder.userNameTextView.setText(user.getName());
                            if (user.getProfilePicUrl() != null && !user.getProfilePicUrl().isEmpty()) {
                                Picasso.get().load(user.getProfilePicUrl()).into(holder.profileImage);
                            } else {
                                holder.profileImage.setImageResource(R.drawable.avatar1); // Default avatar
                            }
                        }
                    }
                });

        holder.editButton.setOnClickListener(v -> {
            holder.itemTitleTextView.setVisibility(View.GONE);
            holder.editableTitleTextView.setText(holder.itemTitleTextView.getText());
            holder.editableTitleTextView.setVisibility(View.VISIBLE);

            holder.itemDescriptionTextView.setVisibility(View.GONE);
            holder.editableDescriptionTextView.setText(holder.itemDescriptionTextView.getText());
            holder.editableDescriptionTextView.setVisibility(View.VISIBLE);
        });

        holder.saveButton.setOnClickListener(v -> {
            String newTitle = holder.editableTitleTextView.getText().toString();
            String newDescription = holder.editableDescriptionTextView.getText().toString();

            post.setTitle(newTitle);
            post.setDescription(newDescription);

            db.collection("posts").document(post.getPostID())
                    .set(post)
                    .addOnSuccessListener(aVoid -> {
                        // Step 2: Update the PostModel object in the user's "myPosts" array
                        db.collection("users").document(post.getOwnerID())
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        UserModel user = documentSnapshot.toObject(UserModel.class);
                                        if (user != null && user.myPosts != null) {
                                            // Find the post to update
                                            for (PostModel userPost : user.myPosts) {
                                                if (userPost.getPostID().equals(post.getPostID())) {
                                                    userPost.setTitle(newTitle);
                                                    userPost.setDescription(newDescription);
                                                    break;
                                                }
                                            }
                                            // Update the user's myPosts array in Firestore
                                            db.collection("users").document(post.getOwnerID())
                                                    .set(user)
                                                    .addOnSuccessListener(aVoid1 -> {
                                                        // Notify the user and update UI only after both operations succeed
                                                        Toast.makeText(context, "Post updated successfully", Toast.LENGTH_SHORT).show();
                                                        FragmentActivity activity = (FragmentActivity) context;
                                                        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
                                                        transaction.replace(R.id.fragment_container, new HomeFragment());
                                                        transaction.addToBackStack(null);
                                                        transaction.commit();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        // Handle failure to update the user's myPosts array
                                                        Toast.makeText(context, "Failed to update user posts", Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure to get the user document
                                    Toast.makeText(context, "Failed to get user data", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to update post", Toast.LENGTH_SHORT).show());
        });

        holder.deleteButton.setOnClickListener(v -> {
            String postId = post.getPostID();
            String userId = post.getOwnerID();

            // Step 1: Delete the post from the "posts" collection
            db.collection("posts").document(postId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Step 2: Remove the PostModel object from the user's "myPosts" array
                        db.collection("users").document(userId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        UserModel user = documentSnapshot.toObject(UserModel.class);
                                        if (user != null && user.myPosts != null) {
                                            // Find the post to remove
                                            PostModel postToRemove = null;
                                            for (PostModel userPost : user.myPosts) {
                                                if (userPost.getPostID().equals(postId)) {
                                                    postToRemove = userPost;
                                                    break;
                                                }
                                            }
                                            if (postToRemove != null) {
                                                user.myPosts.remove(postToRemove);
                                                // Update the user's myPosts array in Firestore
                                                db.collection("users").document(userId)
                                                        .set(user)
                                                        .addOnSuccessListener(aVoid1 -> {
                                                            // Notify the user and update UI only after both operations succeed
                                                            Toast.makeText(context, "Post deleted successfully", Toast.LENGTH_SHORT).show();
                                                            postList.remove(holder.getAdapterPosition());
                                                            notifyItemRemoved(holder.getAdapterPosition());
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            // Handle failure to update the user's myPosts array
                                                            Toast.makeText(context, "Failed to update user posts", Toast.LENGTH_SHORT).show();
                                                        });
                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure to get the user document
                                    Toast.makeText(context, "Failed to get user data", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure to delete the post from the main collection
                        Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show();
                    });
        });

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class HomeViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage, itemImageView;
        TextView userNameTextView, postTimestampTextView, itemTitleTextView, itemDescriptionTextView, itemPriceTextView, itemLocationTextView;
        Button buyButton;

        public HomeViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            postTimestampTextView = itemView.findViewById(R.id.postTimestampTextView);
            itemImageView = itemView.findViewById(R.id.itemImageView);
            itemTitleTextView = itemView.findViewById(R.id.itemTitleTextView);
            itemDescriptionTextView = itemView.findViewById(R.id.itemDescriptionTextView);
            itemPriceTextView = itemView.findViewById(R.id.itemPriceTextView);
            itemLocationTextView = itemView.findViewById(R.id.itemLocationTextView);
            buyButton = itemView.findViewById(R.id.buyButton);
        }
    }

    public static class MyPostsViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage, itemImageView;
        TextView userNameTextView, postTimestampTextView, itemTitleTextView, itemDescriptionTextView, itemPriceTextView, itemLocationTextView;
        EditText editableTitleTextView, editableDescriptionTextView;
        Button editButton, deleteButton, saveButton;
        ImageButton editImageButton;

        public MyPostsViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            postTimestampTextView = itemView.findViewById(R.id.postTimestampTextView);
            itemImageView = itemView.findViewById(R.id.itemImageView);
            itemTitleTextView = itemView.findViewById(R.id.itemTitleTextView);
            editableTitleTextView = itemView.findViewById(R.id.editableTitleTextView);
            itemDescriptionTextView = itemView.findViewById(R.id.itemDescriptionTextView);
            editableDescriptionTextView = itemView.findViewById(R.id.editableDescriptionTextView);
            itemPriceTextView = itemView.findViewById(R.id.itemPriceTextView);
            itemLocationTextView = itemView.findViewById(R.id.itemLocationTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            saveButton = itemView.findViewById(R.id.saveButton);
            editImageButton = itemView.findViewById(R.id.editImageButton);
        }
    }
}
