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
import com.example.easyshop.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<PostModel> postList;

    public PostAdapter(Context context, List<PostModel> postList) {
        this.context = context;
        this.postList = postList;
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
        holder.itemTitleTextView.setText(post.title);
        holder.itemDescriptionTextView.setText(post.description);
        holder.itemPriceTextView.setText("Price: $" + post.price);
        holder.itemLocationTextView.setText("Pickup address: " + post.location);
        Picasso.get().load(post.image).into(holder.itemImageView);
        // Here you can set the profile image, name, and other user details
        // based on the ownerID if you have user details in your database
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImageView;
        TextView itemTitleTextView, itemDescriptionTextView, itemPriceTextView, itemLocationTextView;
        Button buyButton;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImageView = itemView.findViewById(R.id.itemImageView);
            itemTitleTextView = itemView.findViewById(R.id.itemTitleTextView);
            itemDescriptionTextView = itemView.findViewById(R.id.itemDescriptionTextView);
            itemPriceTextView = itemView.findViewById(R.id.itemPriceTextView);
            itemLocationTextView = itemView.findViewById(R.id.itemLocationTextView);
            buyButton = itemView.findViewById(R.id.buyButton);
        }
    }
}
