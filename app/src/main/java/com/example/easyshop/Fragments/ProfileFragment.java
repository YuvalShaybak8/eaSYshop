package com.example.easyshop.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.easyshop.Model.UserModel;
import com.example.easyshop.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private static final int PICK_IMAGE_REQUEST = 71;

    private ImageView profileImage;
    private ImageButton editIcon;
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private String profilePicUrl;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String loggedInUserID;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firestore and Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_edit, container, false);

        // Get logged in user's ID from Firebase Auth
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loggedInUserID = currentUser.getUid();
        } else {
            // Handle the case where the user is not logged in
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Initialize views
        profileImage = view.findViewById(R.id.profile_image);
        editIcon = view.findViewById(R.id.edit_icon);
        nameEditText = view.findViewById(R.id.name);
        emailEditText = view.findViewById(R.id.email);
        passwordEditText = view.findViewById(R.id.password);

        // Initially set profile image to default avatar
        profileImage.setImageResource(R.drawable.avatar1);

        editIcon.setOnClickListener(v -> openGallery());

        view.findViewById(R.id.update_button).setOnClickListener(v -> {
            String newName = nameEditText.getText().toString();
            // Update user information
            updateUserProfile(newName);
        });

        // Load user information from Firebase
        loadUserProfile();

        return view;
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void updateUserProfile(String name) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Update the user's profile in Firestore
            db.collection("users").document(loggedInUserID)
                    .update("name", name, "profilePicUrl", profilePicUrl)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        navigateToHomePage();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show());
        }
    }

    private void navigateToHomePage() {
        Fragment homeFragment = new HomeFragment();
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit();
    }

    private void loadUserProfile() {
        if (loggedInUserID != null) {
            db.collection("users").document(loggedInUserID)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            UserModel user = documentSnapshot.toObject(UserModel.class);
                            if (user != null) {
                                nameEditText.setText(user.getName());
                                emailEditText.setText(user.getEmail());
                                passwordEditText.setText("********");

                                // Load profile image
                                profilePicUrl = user.getProfilePicUrl();
                                if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                                    if (profilePicUrl.startsWith("drawable/")) {
                                        // Load local drawable resource
                                        int resourceId = getResources().getIdentifier(profilePicUrl.replace("drawable/", ""), "drawable", getContext().getPackageName());
                                        if (resourceId != 0) {
                                            profileImage.setImageResource(resourceId);
                                        } else {
                                            profileImage.setImageResource(R.drawable.avatar1);
                                        }
                                    } else {
                                        // Load image from URL
                                        Picasso.get().load(profilePicUrl).into(profileImage);
                                    }
                                } else {
                                    profileImage.setImageResource(R.drawable.avatar1);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to load user profile", e));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                    profileImage.setImageBitmap(bitmap);
                    profilePicUrl = encodeImageToBase64(bitmap);
                    uploadProfileImageToFirestore(profilePicUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void uploadProfileImageToFirestore(String profilePicUrl) {
        if (loggedInUserID != null) {
            db.collection("users").document(loggedInUserID)
                    .update("profilePicUrl", profilePicUrl)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
