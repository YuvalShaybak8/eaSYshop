package com.example.easyshop.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.fragment.app.Fragment;

import com.example.easyshop.Model.UserModel;
import com.example.easyshop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private static final int PICK_IMAGE_REQUEST = 71;
    private static final int CAMERA_REQUEST_CODE = 101;

    private ImageView profileImage;
    private ImageButton editIcon;
    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private String profilePicUrl;

    private FirebaseFirestore db;
    private String loggedInUserID;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_edit, container, false);

        // Get logged in user's ID from shared preferences or wherever you store it
        loggedInUserID = getActivity().getSharedPreferences("MyApp", Context.MODE_PRIVATE).getString("userID", "");

        // Initialize views
        profileImage = view.findViewById(R.id.profile_image);
        editIcon = view.findViewById(R.id.edit_icon);
        usernameEditText = view.findViewById(R.id.username);
        emailEditText = view.findViewById(R.id.email);
        passwordEditText = view.findViewById(R.id.password);

        editIcon.setOnClickListener(v -> showImageSelectionDialog());

        view.findViewById(R.id.update_button).setOnClickListener(v -> {
            String newUsername = usernameEditText.getText().toString();
            String newEmail = emailEditText.getText().toString();
            String newPassword = passwordEditText.getText().toString();

            // Update user information
            updateUserProfile(newUsername, newEmail, newPassword);
        });

        // Load user information from Firebase
        loadUserProfile();

        return view;
    }

    private void showImageSelectionDialog() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    private void updateUserProfile(String username, String email, String password) {
        // Update user profile in Firebase or your server
        if (!loggedInUserID.isEmpty()) {
            db.collection("users").document(loggedInUserID)
                    .update("username", username, "email", email, "password", password, "profilePicUrl", profilePicUrl)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadUserProfile() {
        if (!loggedInUserID.isEmpty()) {
            db.collection("users").document(loggedInUserID)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            UserModel user = documentSnapshot.toObject(UserModel.class);
                            if (user != null) {
                                usernameEditText.setText(user.getUsername());
                                emailEditText.setText(user.getEmail());
                                passwordEditText.setText(user.getPassword());
                                if (user.getProfilePicUrl() != null) {
                                    profilePicUrl = user.getProfilePicUrl();
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
            if (requestCode == PICK_IMAGE_REQUEST) {
                Uri selectedImageUri = data.getData();
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
            }
        }
    }
}
