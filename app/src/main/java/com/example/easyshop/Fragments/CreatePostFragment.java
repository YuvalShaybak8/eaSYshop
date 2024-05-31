package com.example.easyshop.Fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.easyshop.Model.PostModel;
import com.example.easyshop.R;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.FirebaseFirestore;

public class CreatePostFragment extends Fragment {
    private EditText editTextTitle;
    private EditText editTextDescription;
    private EditText editTextPrice;
    private EditText editTextAddress;
    private EditText editTextImage;
    private Button buttonCreatePost;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FirebaseFirestore fs;

    public CreatePostFragment() {
        // Required empty public constructor
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_post, container, false);
        fs = FirebaseFirestore.getInstance();
        editTextTitle = view.findViewById(R.id.createPostTitle);
        editTextDescription = view.findViewById(R.id.createPostDescription);
        editTextPrice = view.findViewById(R.id.createPostPrice);
        editTextAddress = view.findViewById(R.id.createPostAddress);
        //editTextImage = view.findViewById(R.id.createPostImage);
        buttonCreatePost = view.findViewById(R.id.createPostSubmitButton);
        buttonCreatePost.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString();
            String description = editTextDescription.getText().toString();
            Double price = editTextPrice.getText().toString() == "" ? 0.0 : Double.parseDouble(editTextPrice.getText().toString());
            String address = editTextAddress.getText().toString();
            if (title.isEmpty() || description.isEmpty() || price <= 0 || address.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show();
            }
            else {
                // Create a new post, and added some dummy values for the image and ownerID
                PostModel post = new PostModel(title, description, address, price,"stamp", "222");;
                fs.collection("posts").add(post);
                // Clear the fields
                editTextTitle.setText("");
                editTextDescription.setText("");
                editTextPrice.setText("");
                editTextAddress.setText("");
            }

        });
        return view;
    }
}
