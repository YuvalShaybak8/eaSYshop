package com.example.easyshop.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import com.bumptech.glide.Glide;
import com.example.easyshop.Fragments.PaymentFragment;
import com.example.easyshop.Fragments.HomeFragment;
import com.example.easyshop.Fragments.MyPostsFragment;
import com.example.easyshop.Fragments.PostDetailsFragment;
import com.example.easyshop.Model.PostModel;
import com.example.easyshop.Model.UserModel;
import com.example.easyshop.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HOME = 0;
    private static final int VIEW_TYPE_MY_POSTS = 1;
    private static final int VIEW_TYPE_ORDERS = 2;

    private static final int PICK_IMAGE_REQUEST = 71;

    private Context context;
    private List<PostModel> postList;
    private FirebaseFirestore db;
    private boolean isMyPostsPage;
    private boolean isOrdersPage; // New boolean for orders page
    private String currentUserID;
    private Uri selectedImageUri;

    public PostAdapter(Context context, List<PostModel> postList, boolean isMyPostsPage, boolean isOrdersPage, String currentUserID) {
        this.context = context;
        this.postList = postList;
        this.db = FirebaseFirestore.getInstance();
        this.isMyPostsPage = isMyPostsPage;
        this.isOrdersPage = isOrdersPage;
        this.currentUserID = currentUserID;
    }

    @Override
    public int getItemViewType(int position) {
        if (isOrdersPage) {
            return VIEW_TYPE_ORDERS;
        } else if (isMyPostsPage) {
            return VIEW_TYPE_MY_POSTS;
        } else {
            return VIEW_TYPE_HOME;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HOME) {
            View view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
            return new HomeViewHolder(view);
        } else if (viewType == VIEW_TYPE_MY_POSTS) {
            View view = LayoutInflater.from(context).inflate(R.layout.my_post_item, parent, false);
            return new MyPostsViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.my_order_item, parent, false);
            return new OrdersViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PostModel post = postList.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_HOME) {
            HomeViewHolder homeViewHolder = (HomeViewHolder) holder;
            bindHomeViewHolder(homeViewHolder, post);
        } else if (holder.getItemViewType() == VIEW_TYPE_MY_POSTS) {
            MyPostsViewHolder myPostsViewHolder = (MyPostsViewHolder) holder;
            bindMyPostsViewHolder(myPostsViewHolder, post, position);
        } else {
            OrdersViewHolder ordersViewHolder = (OrdersViewHolder) holder;
            bindOrdersViewHolder(ordersViewHolder, post);
        }
    }

    private void bindHomeViewHolder(HomeViewHolder holder, PostModel post) {
        holder.itemTitleTextView.setText(post.getTitle());
        holder.itemDescriptionTextView.setText(post.getDescription());
        holder.itemPriceTextView.setText("Price: $" + post.getPrice());
        holder.itemLocationTextView.setText("Pickup address: " + post.getLocation());

        Glide.with(context).load(post.getImage()).into(holder.itemImageView);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
        String formattedDate = sdf.format(post.getTimestamp().toDate());
        holder.postTimestampTextView.setText(formattedDate);

        db.collection("users").document(post.getOwnerID()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserModel user = documentSnapshot.toObject(UserModel.class);
                        if (user != null) {
                            holder.userNameTextView.setText(user.getName());
                            if (user.getProfilePicUrl() != null && !user.getProfilePicUrl().isEmpty()) {
                                Glide.with(context).load(user.getProfilePicUrl()).into(holder.profileImage);
                            } else {
                                holder.profileImage.setImageResource(R.drawable.avatar1); // Default avatar
                            }
                        }
                    }
                });

        if (post.getOwnerID().equals(currentUserID)) {
            holder.buyButton.setVisibility(View.GONE);
        } else {
            holder.buyButton.setVisibility(View.VISIBLE);
        }

        holder.buyButton.setOnClickListener(v -> {
            FragmentActivity activity = (FragmentActivity) context;
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new PaymentFragment(post));
            transaction.addToBackStack(null);
            transaction.commit();
        });

        holder.commentIcon.setOnClickListener(v -> {
            FragmentActivity activity = (FragmentActivity) context;
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new PostDetailsFragment(post));
            transaction.addToBackStack(null); // Adds the transaction to the back stack so the user can navigate back
            transaction.commit();
        });

        if (post.getComments().size() > 0) {
            holder.commentCount.setText(String.valueOf(post.getComments().size()));
            holder.commentCount.setVisibility(View.VISIBLE);
        } else {
            holder.commentCount.setVisibility(View.GONE);
        }

        db.collection("users").document(currentUserID).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                UserModel user = documentSnapshot.toObject(UserModel.class);
                if (user != null && user.getWishList() != null) {
                    boolean isInWishlist = false;
                    for (PostModel wishlistPost : user.getWishList()) {
                        if (wishlistPost.getPostID().equals(post.getPostID())) {
                            isInWishlist = true;
                            break;
                        }
                    }
                    holder.wishlistIcon.setImageResource(isInWishlist ? R.drawable.ic_in_wishlist : R.drawable.ic_no_wishlist);
                }
            }
        });

        holder.wishlistIcon.setOnClickListener(v -> {
            db.collection("users").document(currentUserID).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    UserModel user = documentSnapshot.toObject(UserModel.class);
                    if (user != null) {
                        boolean isInWishlist = user.getWishList().stream().anyMatch(wishlistPost -> wishlistPost.getPostID().equals(post.getPostID()));

                        if (isInWishlist) {
                            user.getWishList().removeIf(wishlistPost -> wishlistPost.getPostID().equals(post.getPostID()));
                        } else {
                            user.getWishList().add(post);
                        }

                        db.collection("users").document(currentUserID).set(user, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    holder.wishlistIcon.setImageResource(isInWishlist ? R.drawable.ic_no_wishlist : R.drawable.ic_in_wishlist);
                                });
                    }
                }
            });
        });
    }

    private void bindMyPostsViewHolder(MyPostsViewHolder holder, PostModel post, int position) {
        holder.itemTitleTextView.setText(post.getTitle());
        holder.itemDescriptionTextView.setText(post.getDescription());
        holder.itemPriceTextView.setText("Price: $" + post.getPrice());
        holder.itemLocationTextView.setText("Pickup address: " + post.getLocation());

        Glide.with(context).load(post.getImage()).into(holder.itemImageView);

        SimpleDateFormat sdf = new SimpleDateFormat("dd.M.yy 'at' HH:mm", Locale.getDefault());
        String formattedDate = sdf.format(post.getTimestamp().toDate());
        holder.postTimestampTextView.setText(formattedDate);

        db.collection("users").document(post.getOwnerID()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserModel user = documentSnapshot.toObject(UserModel.class);
                        if (user != null) {
                            holder.userNameTextView.setText(user.getName());
                            if (user.getProfilePicUrl() != null && !user.getProfilePicUrl().isEmpty()) {
                                Glide.with(context).load(user.getProfilePicUrl()).into(holder.profileImage);
                            } else {
                                holder.profileImage.setImageResource(R.drawable.avatar1);
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

        holder.editImageButton.setOnClickListener(v -> {
            if (context instanceof FragmentActivity) {
                FragmentActivity activity = (FragmentActivity) context;
                MyPostsFragment fragment = (MyPostsFragment) activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (fragment != null) {
                    fragment.openFileChooser(post);
                }
            }
        });

        holder.saveButton.setOnClickListener(v -> {
            String newTitle = holder.editableTitleTextView.getText().toString();
            String newDescription = holder.editableDescriptionTextView.getText().toString();
            Uri selectedImageUri = holder.selectedImageUri;

            post.setTitle(newTitle);
            post.setDescription(newDescription);
            if (selectedImageUri != null) {
                post.setImage(selectedImageUri.toString());
            }

            db.collection("posts").document(post.getPostID())
                    .set(post)
                    .addOnSuccessListener(aVoid -> {
                        db.collection("users").document(post.getOwnerID())
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        UserModel user = documentSnapshot.toObject(UserModel.class);
                                        if (user != null && user.myPosts != null) {
                                            for (PostModel userPost : user.myPosts) {
                                                if (userPost.getPostID().equals(post.getPostID())) {
                                                    userPost.setTitle(newTitle);
                                                    userPost.setDescription(newDescription);
                                                    if (selectedImageUri != null) {
                                                        userPost.setImage(selectedImageUri.toString());
                                                    }
                                                    break;
                                                }
                                            }
                                            db.collection("users").document(post.getOwnerID()).set(user)
                                                    .addOnSuccessListener(aVoid1 -> {
                                                        Toast.makeText(context, "Post updated successfully", Toast.LENGTH_SHORT).show();
                                                        FragmentActivity activity = (FragmentActivity) context;
                                                        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
                                                        transaction.replace(R.id.fragment_container, new MyPostsFragment());
                                                        transaction.addToBackStack(null);
                                                        transaction.commit();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(context, "Failed to update user posts", Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to get user data", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to update post", Toast.LENGTH_SHORT).show());
        });

        holder.deleteButton.setOnClickListener(v -> {
            String postId = post.getPostID();
            String userId = post.getOwnerID();

            db.collection("posts").document(postId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        db.collection("users").document(userId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        UserModel user = documentSnapshot.toObject(UserModel.class);
                                        if (user != null && user.myPosts != null) {
                                            PostModel postToRemove = null;
                                            for (PostModel userPost : user.myPosts) {
                                                if (userPost.getPostID().equals(postId)) {
                                                    postToRemove = userPost;
                                                    break;
                                                }
                                            }
                                            if (postToRemove != null) {
                                                user.myPosts.remove(postToRemove);
                                                db.collection("users").document(userId).set(user)
                                                        .addOnSuccessListener(aVoid1 -> {
                                                            Toast.makeText(context, "Post deleted successfully", Toast.LENGTH_SHORT).show();
                                                            postList.remove(holder.getAdapterPosition());
                                                            notifyItemRemoved(holder.getAdapterPosition());
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(context, "Failed to update user posts", Toast.LENGTH_SHORT).show();
                                                        });
                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to get user data", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void bindOrdersViewHolder(OrdersViewHolder holder, PostModel post) {
        holder.itemTitleTextView.setText(post.getTitle());
        holder.itemDescriptionTextView.setText(post.getDescription());
        holder.itemPriceTextView.setText("Price: $" + post.getPrice());
        holder.itemLocationTextView.setText("Pickup address: " + post.getLocation());


        Glide.with(context).load(post.getImage()).into(holder.itemImageView);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
        String formattedDate = sdf.format(post.getTimestamp().toDate());
        holder.postTimestampTextView.setText(formattedDate);

        db.collection("users").document(post.getOwnerID()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserModel user = documentSnapshot.toObject(UserModel.class);
                        if (user != null) {
                            holder.userNameTextView.setText(user.getName());
                            if (user.getProfilePicUrl() != null && !user.getProfilePicUrl().isEmpty()) {
                                Glide.with(context).load(user.getProfilePicUrl()).into(holder.profileImage);
                            } else {
                                holder.profileImage.setImageResource(R.drawable.avatar1);
                            }
                        }
                    }
                });

        holder.purchasedButton.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void updateSelectedImageUri(Uri selectedImageUri) {
        this.selectedImageUri = selectedImageUri;
        notifyDataSetChanged();
    }

    public static class HomeViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage, itemImageView;
        TextView userNameTextView, postTimestampTextView, itemTitleTextView, itemDescriptionTextView, itemPriceTextView, itemLocationTextView, commentCount;
        Button buyButton;
        ImageView commentIcon, wishlistIcon;

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
            commentIcon = itemView.findViewById(R.id.commentIcon);
            commentCount = itemView.findViewById(R.id.commentCount);
            wishlistIcon = itemView.findViewById(R.id.wishlistIcon);
            buyButton = itemView.findViewById(R.id.buyButton);
        }
    }

    public static class MyPostsViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage, itemImageView;
        TextView userNameTextView, postTimestampTextView, itemTitleTextView, itemDescriptionTextView, itemPriceTextView, itemLocationTextView;
        EditText editableTitleTextView, editableDescriptionTextView;
        Button editButton, deleteButton, saveButton;
        ImageButton editImageButton;
        Uri selectedImageUri;

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

    public static class OrdersViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage, itemImageView;
        TextView userNameTextView, postTimestampTextView, itemTitleTextView, itemDescriptionTextView, itemPriceTextView, itemLocationTextView;
        Button purchasedButton;

        public OrdersViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            postTimestampTextView = itemView.findViewById(R.id.postTimestampTextView);
            itemImageView = itemView.findViewById(R.id.itemImageView);
            itemTitleTextView = itemView.findViewById(R.id.itemTitleTextView);
            itemDescriptionTextView = itemView.findViewById(R.id.itemDescriptionTextView);
            itemPriceTextView = itemView.findViewById(R.id.itemPriceTextView);
            itemLocationTextView = itemView.findViewById(R.id.itemLocationTextView);
            purchasedButton = itemView.findViewById(R.id.buyButton);
        }
    }
}
