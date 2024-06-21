package com.example.easyshop.Fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easyshop.Adapters.AddressSuggestionsAdapter;
import com.example.easyshop.Model.PostModel;
import com.example.easyshop.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class CreatePostFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 71;
    private static final int CAMERA_REQUEST_CODE = 101;

    private EditText editTextTitle;
    private EditText editTextDescription;
    private EditText editTextPrice;
    private EditText editTextAddress;
    private ImageView imageView1;
    private Button buttonCreatePost;
    private RecyclerView addressSuggestionsRecyclerView;
    private AddressSuggestionsAdapter addressSuggestionsAdapter;
    private Uri imageUri1;
    private FirebaseFirestore fs;
    private FirebaseAuth mAuth;
    private PlacesClient placesClient;

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
        editTextAddress = view.findViewById(R.id.createPostAddress);
        imageView1 = view.findViewById(R.id.createPostImage1);

        buttonCreatePost = view.findViewById(R.id.createPostSubmitButton);
        addressSuggestionsRecyclerView = view.findViewById(R.id.addressSuggestionsRecyclerView);

        // Initialize the Places API
        Places.initialize(requireContext(), "AIzaSyAE412NbG66NdE68Fap8_ncqt_crHnxYTE");
        placesClient = Places.createClient(requireContext());

        imageView1.setOnClickListener(v -> openImageSelector());

        addressSuggestionsAdapter = new AddressSuggestionsAdapter(new ArrayList<>(), suggestion -> {
            editTextAddress.setText(suggestion);
            addressSuggestionsRecyclerView.setVisibility(View.GONE);
        });

        addressSuggestionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        addressSuggestionsRecyclerView.setAdapter(addressSuggestionsAdapter);

        buttonCreatePost.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString();
            String description = editTextDescription.getText().toString();
            double price = editTextPrice.getText().toString().isEmpty() ? 0.0 : Double.parseDouble(editTextPrice.getText().toString());
            String address = editTextAddress.getText().toString();

            if (title.isEmpty() || description.isEmpty() || price <= 0 || address.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all the fields and upload at least one image", Toast.LENGTH_SHORT).show();
            } else {
                String ownerID = mAuth.getCurrentUser().getUid();
                Timestamp timestamp = Timestamp.now();

                // Create a new post
                PostModel post = new PostModel(title, description, imageUri1.toString(), price, address, ownerID, timestamp);
                fs.collection("posts").add(post)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(getContext(), "Post created successfully", Toast.LENGTH_SHORT).show();
                            // Clear the fields
                            editTextTitle.setText("");
                            editTextDescription.setText("");
                            editTextPrice.setText("");
                            editTextAddress.setText("");
                            imageView1.setImageResource(R.drawable.add_image);

                            imageUri1 = null;
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to create post", Toast.LENGTH_SHORT).show());
            }
        });

        // Add a TextWatcher to the address field to trigger autocomplete suggestions
        editTextAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    getAutocompleteSuggestions(s.toString());
                } else {
                    addressSuggestionsRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return view;
    }

    private void openImageSelector() {
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

    private void getAutocompleteSuggestions(String query) {
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
            @Override
            public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    List<AutocompletePrediction> predictions = task.getResult().getAutocompletePredictions();
                    List<String> suggestionList = new ArrayList<>();
                    for (AutocompletePrediction prediction : predictions) {
                        suggestionList.add(prediction.getFullText(null).toString());
                    }
                    if (!suggestionList.isEmpty()) {
                        addressSuggestionsRecyclerView.setVisibility(View.VISIBLE);
                        addressSuggestionsAdapter.updateSuggestions(suggestionList);
                    } else {
                        addressSuggestionsRecyclerView.setVisibility(View.GONE);
                    }
                } else {
                    // Handle the error
                    Status status = task.getException() instanceof com.google.android.gms.common.api.ApiException ?
                            ((com.google.android.gms.common.api.ApiException) task.getException()).getStatus() : null;
                    if (status != null) {
                        Toast.makeText(getContext(), "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Unknown error occurred", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
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
                case CAMERA_REQUEST_CODE:
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    imageView1.setImageBitmap(photo);
                    // Convert bitmap to Uri
                    imageUri1 = getImageUri(getContext(), photo);
                    break;
            }
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
}
