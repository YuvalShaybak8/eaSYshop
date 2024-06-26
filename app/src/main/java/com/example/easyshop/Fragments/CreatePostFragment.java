package com.example.easyshop.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.easyshop.Model.PostModel;
import com.example.easyshop.Model.UserModel;
import com.example.easyshop.R;
import com.example.easyshop.activities.MainActivity;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class CreatePostFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 71;

    private EditText editTextTitle;
    private EditText editTextDescription;
    private EditText editTextPrice;
    private ImageView imageView1;
    private Button buttonCreatePost;
    private Uri imageUri1;
    private FirebaseFirestore fs;
    private FirebaseAuth mAuth;
    private String selectedAddress;
    private ProgressDialog progressDialog;

    public CreatePostFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_post, container, false);
        fs = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        editTextTitle = view.findViewById(R.id.createPostTitle);
        editTextDescription = view.findViewById(R.id.createPostDescription);
        editTextPrice = view.findViewById(R.id.createPostPrice);
        imageView1 = view.findViewById(R.id.createPostImage1);
        buttonCreatePost = view.findViewById(R.id.createPostSubmitButton);

        // Initialize the Places API
        Places.initialize(requireContext(), "AIzaSyAE412NbG66NdE68Fap8_ncqt_crHnxYTE");

        // Setup AutocompleteSupportFragment
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // Get the address
                selectedAddress = place.getAddress();
            }

            @Override
            public void onError(@NonNull Status status) {
                // Handle error
                Toast.makeText(getContext(), "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        imageView1.setOnClickListener(v -> openImageSelector());

        buttonCreatePost.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString();
            String description = editTextDescription.getText().toString();
            double price = editTextPrice.getText().toString().isEmpty() ? 0.0 : Double.parseDouble(editTextPrice.getText().toString());

            if (title.isEmpty() || description.isEmpty() || price <= 0 || selectedAddress == null || selectedAddress.isEmpty() || imageUri1 == null) {
                Toast.makeText(getContext(), "Please fill all the fields and upload at least one image", Toast.LENGTH_SHORT).show();
            } else {
                uploadImageAndCreatePost(title, description, price, selectedAddress);
            }
        });

        return view;
    }

    private void openImageSelector() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            switch (requestCode) {
                case PICK_IMAGE_REQUEST:
                    imageUri1 = selectedImageUri;
                    imageView1.setImageURI(imageUri1);
                    break;
            }
        }
    }

    private void uploadImageAndCreatePost(String title, String description, double price, String location) {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Uploading File...");
        progressDialog.show();

        String fileName = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CANADA).format(new Date());
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("post_images/" + fileName);

        storageReference.putFile(imageUri1)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                createPost(title, description, price, location, imageUrl);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
    }

    private void createPost(String title, String description, double price, String location, String imageUrl) {
        String ownerID = mAuth.getCurrentUser().getUid();
        Timestamp timestamp = Timestamp.now();

        PostModel post = new PostModel("", title, description, imageUrl, price, location, ownerID, timestamp);

        fs.collection("posts").add(post)
                .addOnSuccessListener(documentReference -> {
                    post.setPostID(documentReference.getId());
                    fs.collection("posts").document(post.getPostID()).set(post)
                            .addOnSuccessListener(aVoid -> {
                                fs.collection("users").document(ownerID)
                                        .get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.exists()) {
                                                UserModel user = documentSnapshot.toObject(UserModel.class);
                                                if (user != null) {
                                                    if (user.getMyPosts() == null) {
                                                        user.setMyPosts(new ArrayList<>());
                                                    }
                                                    user.getMyPosts().add(post);
                                                    fs.collection("users").document(ownerID).set(user, SetOptions.merge())
                                                            .addOnSuccessListener(aVoid1 -> {
                                                                progressDialog.dismiss();
                                                                Toast.makeText(getContext(), "Post created successfully", Toast.LENGTH_SHORT).show();
                                                                resetFields();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                progressDialog.dismiss();
                                                                Toast.makeText(getContext(), "Failed to add post to user's posts", Toast.LENGTH_SHORT).show();
                                                            });
                                                }
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            progressDialog.dismiss();
                                            Toast.makeText(getContext(), "Failed to get user data", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(getContext(), "Failed to create post", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Failed to create post", Toast.LENGTH_SHORT).show();
                });
    }

    private void resetFields() {
        editTextTitle.setText("");
        editTextDescription.setText("");
        editTextPrice.setText("");
        imageView1.setImageResource(R.drawable.add_image);
        imageUri1 = null;
    }
}
