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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.easyshop.R;
import com.example.easyshop.activities.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri;
    private ImageView profileImageView;
    private ImageButton chooseImageButton;
    private Button uploadImageButton;
    private TextView nameTextView;
    private TextView emailTextView;
    private String userId;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_edit, container, false);

        profileImageView = view.findViewById(R.id.profile_image);
        chooseImageButton = view.findViewById(R.id.edit_icon);
        uploadImageButton = view.findViewById(R.id.update_button);
        nameTextView = view.findViewById(R.id.name);
        emailTextView = view.findViewById(R.id.email);

        // Get current user ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            loadUserProfile();
        } else {
            // Handle the case where there is no logged-in user
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return view;
        }

        chooseImageButton.setOnClickListener(v -> openFileChooser());

        uploadImageButton.setOnClickListener(v -> uploadImage());

        return view;
    }

    private void loadUserProfile() {
        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String profilePicUrl = documentSnapshot.getString("profilePicUrl");
                            String email = documentSnapshot.getString("email");

                            nameTextView.setText(name);
                            emailTextView.setText(email);
                            if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                                Glide.with(getContext()).load(profilePicUrl).into(profileImageView);
                                // Update the header image
                                ((MainActivity) getActivity()).updateHeaderProfilePicture(profilePicUrl);
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            profileImageView.setImageURI(mImageUri);
        }
    }

    private void uploadImage() {
        if (mImageUri != null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading File...");
            progressDialog.show();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CANADA);
            Date now = new Date();
            String fileName = formatter.format(now);
            storageReference = FirebaseStorage.getInstance().getReference("profile_pics/" + fileName);

            storageReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();
                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("profilePicUrl", downloadUrl);

                                    FirebaseFirestore.getInstance().collection("users").document(userId)
                                            .update(updates)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                                                if (progressDialog.isShowing())
                                                    progressDialog.dismiss();
                                                // Load the updated profile picture
                                                loadUserProfilePicture();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(getContext(), "Failed to update profile picture URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                if (progressDialog.isShowing())
                                                    progressDialog.dismiss();
                                            });
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                            Toast.makeText(getContext(), "Failed to upload: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getContext(), "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserProfilePicture() {
        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String profilePicUrl = documentSnapshot.getString("profilePicUrl");
                        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                            // Update the header image
                            ((MainActivity) getActivity()).updateHeaderProfilePicture(profilePicUrl);
                        }
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load profile picture", Toast.LENGTH_SHORT).show();
                });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
}
