package com.example.easyshop.Fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.easyshop.Model.UserModel;
import com.example.easyshop.R;
import com.example.easyshop.activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText nameEditText, emailEditText, passwordEditText;
    private ImageButton passwordToggle;
    private boolean isPasswordVisible = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameEditText = view.findViewById(R.id.nameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        Button registerButton = view.findViewById(R.id.registerButton);
        Button signInButton = view.findViewById(R.id.signInButton);
        passwordToggle = view.findViewById(R.id.passwordToggle);

        registerButton.setOnClickListener(v -> registerUser());

        signInButton.setOnClickListener(v -> {
            // Navigate back to LoginFragment
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Set initial state and click listener for passwordToggle ImageButton
        passwordToggle.setOnClickListener(v -> togglePasswordVisibility());

        return view;
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwordToggle.setImageResource(R.drawable.ic_hide_password);
        } else {
            passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            passwordToggle.setImageResource(R.drawable.ic_show_password);
        }
        isPasswordVisible = !isPasswordVisible;
        // Move the cursor to the end of the input text
        passwordEditText.setSelection(passwordEditText.length());
    }

    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getActivity(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserModel user = new UserModel(name, email, password);
                        db.collection("users").document(mAuth.getCurrentUser().getUid())
                                .set(user)
                                .addOnSuccessListener(aVoid -> Toast.makeText(getActivity(), "User registered successfully", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to register user", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(getActivity(), "Registration failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
