package com.example.easyshop.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.easyshop.Adapters.PostAdapter;
import com.example.easyshop.Model.PostModel;
import com.example.easyshop.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyPostsFragment extends Fragment {
    private FirebaseFirestore fs;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<PostModel> postList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseAuth mAuth;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_posts, container, false);

        fs = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::loadPosts);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList, true, false, userId); // Pass true for MyPostsFragment and current user ID
        recyclerView.setAdapter(postAdapter);

        loadPosts();

        return view;
    }

    private void loadPosts() {
        swipeRefreshLayout.setRefreshing(true);
        fs.collection("posts").whereEqualTo("ownerID", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        postList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            PostModel post = document.toObject(PostModel.class);
                            post.setPostID(document.getId()); // Set the postID
                            postList.add(post);
                        }
                        postAdapter.notifyDataSetChanged();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                });
    }
}
