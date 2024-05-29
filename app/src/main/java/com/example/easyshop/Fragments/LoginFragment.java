package com.example.easyshop.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.easyshop.R;
import com.example.easyshop.Model.UserModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginFragment extends Fragment {

    private DatabaseReference mDatabase;
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button submitButton;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        nameEditText = view.findViewById(R.id.nameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        submitButton = view.findViewById(R.id.submitButton);

        submitButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString();
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            writeNewUser(name, email, password);
        });

        return view;
    }

    private void writeNewUser(String name, String email,String password) {
        String userId = mDatabase.push().getKey();
        UserModel user = new UserModel(name, email, password);
        if (userId != null) {
            mDatabase.child("users").child(userId).setValue(user)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "User added to database", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to add user", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
