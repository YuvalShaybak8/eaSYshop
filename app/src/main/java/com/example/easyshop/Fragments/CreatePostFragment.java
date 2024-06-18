package com.example.easyshop.Fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.example.easyshop.R;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

public class CreatePostFragment extends Fragment {

    private static final int REQUEST_IMAGE_PICK = 1;

    private EditText editTextTitle;
    private EditText editTextDescription;
    private EditText editTextPrice;
    private EditText editTextAddress;
    private ImageView imageView1, imageView2, imageView3;
    private Button buttonCreatePost;
    private Uri imageUri1, imageUri2, imageUri3;
    private FirebaseFirestore fs;

    public CreatePostFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_post, container, false);
        fs = FirebaseFirestore.getInstance();

        editTextTitle = view.findViewById(R.id.createPostTitle);
        editTextDescription = view.findViewById(R.id.createPostDescription);
        editTextPrice = view.findViewById(R.id.createPostPrice);
        editTextAddress = view.findViewById(R.id.createPostAddress);
        imageView1 = view.findViewById(R.id.createPostImage1);
        imageView2 = view.findViewById(R.id.createPostImage2);
        imageView3 = view.findViewById(R.id.createPostImage3);
        buttonCreatePost = view.findViewById(R.id.createPostSubmitButton);

        imageView1.setOnClickListener(v -> openImageSelector(1));
        imageView2.setOnClickListener(v -> openImageSelector(2));
        imageView3.setOnClickListener(v -> openImageSelector(3));

        buttonCreatePost.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString();
            String description = editTextDescription.getText().toString();
            double price = editTextPrice.getText().toString().isEmpty() ? 0.0 : Double.parseDouble(editTextPrice.getText().toString());
            String address = editTextAddress.getText().toString();

            if (title.isEmpty() || description.isEmpty() || price <= 0 || address.isEmpty() || imageUri1 == null || imageUri2 == null || imageUri3 == null) {
                Toast.makeText(getContext(), "Please fill all the fields and upload images", Toast.LENGTH_SHORT).show();
            } else {
                // Create a new post
                PostModel post = new PostModel(title, description, address, price, "stamp", "222"); // Dummy values for image and ownerID
                fs.collection("posts").add(post);

                // Clear the fields
                editTextTitle.setText("");
                editTextDescription.setText("");
                editTextPrice.setText("");
                editTextAddress.setText("");
                imageView1.setImageResource(R.drawable.add_image);
                imageView2.setImageResource(R.drawable.add_image);
                imageView3.setImageResource(R.drawable.add_image);
                imageUri1 = null;
                imageUri2 = null;
                imageUri3 = null;
            }
        });

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS));
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    // Get the place address and set it to the address field
                    editTextAddress.setText(place.getAddress());
                }

                @Override
                public void onError(@NonNull Status status) {
                    // Handle error
                }
            });
        }

        return view;
    }

    private void openImageSelector(int imageViewNumber) {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhoto.setType("image/*");
        startActivityForResult(pickPhoto, REQUEST_IMAGE_PICK + imageViewNumber);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            switch (requestCode) {
                case REQUEST_IMAGE_PICK + 1:
                    imageUri1 = selectedImageUri;
                    imageView1.setImageURI(imageUri1);
                    break;
                case REQUEST_IMAGE_PICK + 2:
                    imageUri2 = selectedImageUri;
                    imageView2.setImageURI(imageUri2);
                    break;
                case REQUEST_IMAGE_PICK + 3:
                    imageUri3 = selectedImageUri;
                    imageView3.setImageURI(imageUri3);
                    break;
            }
        }
    }
}
