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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri;
    private ImageView profileImageView;
    private ImageButton chooseImageButton;
    private Button updateButton;
    private EditText nameEditText;
    private EditText emailEditText;
    private String userId;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_edit, container, false);

        profileImageView = view.findViewById(R.id.profile_image);
        chooseImageButton = view.findViewById(R.id.edit_icon);
        updateButton = view.findViewById(R.id.update_button);
        nameEditText = view.findViewById(R.id.name);
        emailEditText = view.findViewById(R.id.email);

        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        progressDialog = new ProgressDialog(getContext());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            emailEditText.setText(user.getEmail());
        }

        loadUserProfile();

        chooseImageButton.setOnClickListener(v -> openFileChooser());
        updateButton.setOnClickListener(v -> updateProfile());

        return view;
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
            Glide.with(this).load(mImageUri).into(profileImageView);
        }
    }

    private void updateProfile() {
        String name = nameEditText.getText().toString();
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Updating profile...");
        progressDialog.show();

        if (mImageUri != null) {
            String fileExtension = getFileExtension(mImageUri);
            String fileName = System.currentTimeMillis() + "." + fileExtension;
            StorageReference fileReference = storageReference.child(fileName);

            fileReference.putFile(mImageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        updateUserProfile(name, imageUrl);
                    }))
                    .addOnFailureListener(e -> {
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                        Toast.makeText(getContext(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            updateUserProfile(name, null);
        }
    }

    private void updateUserProfile(String name, String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        if (imageUrl != null) {
            updates.put("profilePicUrl", imageUrl);
        }

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                    loadUserProfile();
                    if (imageUrl != null) {
                        updateHeaderProfilePicture(imageUrl);
                    }
                    // Navigate to HomeFragment
                    navigateToHome();
                })
                .addOnFailureListener(e -> {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    Toast.makeText(getContext(), "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserProfile() {
        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String profilePicUrl = documentSnapshot.getString("profilePicUrl");
                        if (name != null && !name.isEmpty()) {
                            nameEditText.setText(name);
                        }
                        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                            Glide.with(this).load(profilePicUrl).into(profileImageView);
                        }
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateHeaderProfilePicture(String imageUrl) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateHeaderProfilePicture(imageUrl);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void navigateToHome() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra("navigateTo", R.id.action_home); // pass the home action to navigate
        startActivity(intent);
        getActivity().finish();
    }
}
