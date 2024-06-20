package com.example.easyshop.Fragments;

import android.Manifest;
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
import android.widget.Button;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

        // Initially set profile image to default avatar
        profileImage.setImageResource(R.drawable.avatar1);

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
        String[] options = {"Take Photo", "Choose from Gallery"};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Select Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                    } else if (which == 1) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
                    }
                });
        builder.show();
    }

    private void updateUserProfile(String username, String email, String password) {
        // Update user profile in Firebase or your server
        if (!loggedInUserID.isEmpty()) {
            db.collection("users").document(loggedInUserID)
                    .update("username", username, "email", email, "password", password, "profilePicUrl", profilePicUrl)
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
        if (!loggedInUserID.isEmpty()) {
            db.collection("users").document(loggedInUserID)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            UserModel user = documentSnapshot.toObject(UserModel.class);
                            if (user != null) {
                                usernameEditText.setText(user.getUsername());
                                emailEditText.setText(user.getEmail());
                                passwordEditText.setText(user.getPassword()); // Use this with caution
                                if (user.getProfilePicUrl() != null) {
                                    profilePicUrl = user.getProfilePicUrl();
                                    Picasso.get().load(profilePicUrl).into(profileImage);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to load user profile", e));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == CAMERA_REQUEST_CODE && data != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                profileImage.setImageBitmap(photo);
                profilePicUrl = encodeImageToBase64(photo);
            }
        }
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
