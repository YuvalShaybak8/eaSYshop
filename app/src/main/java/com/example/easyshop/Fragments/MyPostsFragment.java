package com.example.easyshop.Fragments;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyshop.Adapters.PostAdapter;
import com.example.easyshop.Model.PostModel;
import com.example.easyshop.Model.UserModel;
import com.example.easyshop.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class MyPostsFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 71;

    private RecyclerView recyclerViewMyPosts;
    private PostAdapter postAdapter;
    private List<PostModel> postList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private Uri selectedImageUri;
    private ProgressDialog progressDialog;
    private PostModel postToEdit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_posts, container, false);

        recyclerViewMyPosts = view.findViewById(R.id.recyclerViewMyPosts);
        recyclerViewMyPosts.setLayoutManager(new LinearLayoutManager(getContext()));

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList, true, false, currentUserID);
        recyclerViewMyPosts.setAdapter(postAdapter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        loadUserPosts();

        return view;
    }

    private void loadUserPosts() {
        db.collection("users").document(currentUserID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserModel user = documentSnapshot.toObject(UserModel.class);
                        if (user != null && user.getMyPosts() != null) {
                            postList.clear();
                            postList.addAll(user.getMyPosts());
                            postList.sort((p1, p2) -> p2.getTimestamp().compareTo(p1.getTimestamp()));
                            postAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load posts", Toast.LENGTH_SHORT).show());
    }

    public void openFileChooser(PostModel post) {
        postToEdit = post;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            if (postToEdit != null) {
                uploadImage(postToEdit);
            }
        }
    }

    private void uploadImage(PostModel post) {
        if (selectedImageUri != null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading File...");
            progressDialog.show();

            StorageReference storageReference = FirebaseStorage.getInstance().getReference("post_images")
                    .child(System.currentTimeMillis() + "." + getFileExtension(selectedImageUri));

            storageReference.putFile(selectedImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    post.setImage(imageUrl);
                                    updatePost(post);
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updatePost(PostModel post) {
        db.collection("posts").document(post.getPostID())
                .set(post)
                .addOnSuccessListener(aVoid -> {
                    db.collection("users").document(post.getOwnerID())
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    UserModel user = documentSnapshot.toObject(UserModel.class);
                                    if (user != null && user.getMyPosts() != null) {
                                        for (PostModel userPost : user.getMyPosts()) {
                                            if (userPost.getPostID().equals(post.getPostID())) {
                                                userPost.setImage(post.getImage());
                                                break;
                                            }
                                        }
                                        db.collection("users").document(post.getOwnerID()).set(user, SetOptions.merge())
                                                .addOnSuccessListener(aVoid1 -> {
                                                    Toast.makeText(getContext(), "Post updated successfully", Toast.LENGTH_SHORT).show();
                                                    loadUserPosts(); // Refresh the list
                                                })
                                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update user posts", Toast.LENGTH_SHORT).show());
                                    }
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to get user data", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update post", Toast.LENGTH_SHORT).show());
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
}
