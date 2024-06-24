package com.example.easyshop.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyshop.Adapters.AddressSuggestionsAdapter;
import com.example.easyshop.Model.PostModel;
import com.example.easyshop.R;
import com.example.easyshop.activities.MainActivity;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

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

            if (title.isEmpty() || description.isEmpty() || price <= 0 || selectedAddress == null || selectedAddress.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all the fields and upload at least one image", Toast.LENGTH_SHORT).show();
            } else {
                String ownerID = mAuth.getCurrentUser().getUid();
                Timestamp timestamp = Timestamp.now();

                // Ensure imageUri1 is not null
                String imageUriString = (imageUri1 != null) ? imageUri1.toString() : "";

                // Create a new post
                PostModel post = new PostModel("", title, description, imageUriString, price, selectedAddress, ownerID, timestamp);

                // Add post to Firestore
                fs.collection("posts").add(post)
                        .addOnSuccessListener(documentReference -> {
                            // Set the generated postID
                            post.setPostID(documentReference.getId());
                            fs.collection("posts").document(post.getPostID()).set(post)
                                    .addOnSuccessListener(aVoid -> {
                                        // Add post to user's myPosts array
                                        Map<String, Object> update = new HashMap<>();
                                        update.put("myPosts", com.google.firebase.firestore.FieldValue.arrayUnion(post));
                                        fs.collection("users").document(ownerID).set(update, SetOptions.merge())
                                                .addOnSuccessListener(aVoid1 -> {
                                                    Toast.makeText(getContext(), "Post created successfully", Toast.LENGTH_SHORT).show();
                                                    // Clear the fields
                                                    editTextTitle.setText("");
                                                    editTextDescription.setText("");
                                                    editTextPrice.setText("");
                                                    selectedAddress = null;
                                                    autocompleteFragment.setText("");

                                                    imageView1.setImageResource(R.drawable.add_image);

                                                    imageUri1 = null;

                                                    // Navigate back to HomeFragment and refresh posts
                                                    if (getActivity() instanceof MainActivity) {
                                                        MainActivity mainActivity = (MainActivity) getActivity();
                                                        mainActivity.replaceFragment(new HomeFragment(), true); // Passing true to indicate refresh
                                                    }
                                                })
                                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add post to user's posts", Toast.LENGTH_SHORT).show());
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to create post", Toast.LENGTH_SHORT).show());
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to create post", Toast.LENGTH_SHORT).show());
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

    private Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
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
}
