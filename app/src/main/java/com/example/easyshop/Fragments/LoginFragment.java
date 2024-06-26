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
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.easyshop.R;
import com.example.easyshop.Utils.KeyboardUtils;
import com.example.easyshop.activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginFragment extends Fragment {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button signUpButton;
    private ImageButton passwordToggle;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isPasswordVisible = false;
    private ScrollView scrollView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        loginButton = view.findViewById(R.id.loginButton);
        passwordToggle = view.findViewById(R.id.passwordToggle);
        scrollView = view.findViewById(R.id.scrollView);

        loginButton.setOnClickListener(v -> loginUserAccount());

        signUpButton = view.findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RegisterFragment())
                    .addToBackStack(null)
                    .commit();
        });

        passwordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordToggle.setImageResource(R.drawable.ic_hide_password);
            } else {
                passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passwordToggle.setImageResource(R.drawable.ic_show_password);
            }
            isPasswordVisible = !isPasswordVisible;
            passwordEditText.setSelection(passwordEditText.length());
        });

        KeyboardUtils.setKeyboardVisibilityListener(getActivity(), isVisible -> {
            if (isVisible) {
                scrollView.post(() -> scrollView.smoothScrollTo(0, emailEditText.getBottom()));
            }
        });

        return view;
    }

    private void loginUserAccount() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getContext(), "Please enter email!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Please enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateLoginStatus(true);
                    } else {
                        Toast.makeText(getContext(), "Login failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateLoginStatus(boolean isLoggedIn) {
        if (mAuth.getCurrentUser() != null) {
            String loggedInUserID = mAuth.getCurrentUser().getUid();
            db.collection("users").document(loggedInUserID)
                    .update("isLoggedIn", isLoggedIn)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (isLoggedIn) {
                                navigateToHomeFragment();
                            } else {
                                Toast.makeText(getContext(), "Login status updated", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Failed to update login status", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void navigateToHomeFragment() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateUIForFragment(new HomeFragment());
        }
    }
}
