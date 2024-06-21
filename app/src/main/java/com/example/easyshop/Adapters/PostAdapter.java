package com.example.easyshop.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyshop.Model.PostModel;
import com.example.easyshop.Model.UserModel;
import com.example.easyshop.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<PostModel> postList;
    private FirebaseFirestore db;

    public PostAdapter(Context context, List<PostModel> postList) {
        this.context = context;
        this.postList = postList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        PostModel post = postList.get(position);
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
                            holder.userNameTextView.setText(user.getUsername());
                            if (user.getProfilePicUrl() != null && !user.getProfilePicUrl().isEmpty()) {
                                Picasso.get().load(user.getProfilePicUrl()).into(holder.profileImage);
                            } else {
                                holder.profileImage.setImageResource(R.drawable.avatar1); // Default avatar
                            }
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage, itemImageView;
        TextView userNameTextView, postTimestampTextView, itemTitleTextView, itemDescriptionTextView, itemPriceTextView, itemLocationTextView;
        Button buyButton;

        public PostViewHolder(@NonNull View itemView) {
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
}
