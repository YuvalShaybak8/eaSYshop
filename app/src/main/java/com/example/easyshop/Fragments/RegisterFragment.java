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
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.easyshop.R;
import com.example.easyshop.ViewModel.RegisterViewModel;
import com.example.easyshop.activities.MainActivity;

public class RegisterFragment extends Fragment {

    private RegisterViewModel registerViewModel;
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private ImageButton passwordToggle;
    private boolean isPasswordVisible = false;
    private Button registerButton;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        nameEditText = view.findViewById(R.id.nameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        registerButton = view.findViewById(R.id.registerButton);
        Button signInButton = view.findViewById(R.id.signInButton);
        progressBar = view.findViewById(R.id.progressBar);
        passwordToggle = view.findViewById(R.id.passwordToggle);

        registerButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String username = nameEditText.getText().toString();
            progressBar.setVisibility(View.VISIBLE);

            registerViewModel.registerUser(email, password, username, getContext());
        });

        signInButton.setOnClickListener(v -> {
            // Navigate back to LoginFragment
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .addToBackStack(null)
                    .commit();
        });

        passwordToggle.setOnClickListener(v -> togglePasswordVisibility());

        registerViewModel.registerResult.observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess) {
                Toast.makeText(getActivity(), "Registration Successful", Toast.LENGTH_SHORT).show();
                navigateToHomeFragment();
            } else {
                Toast.makeText(getActivity(), "Registration failed", Toast.LENGTH_SHORT).show();
            }
        });

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
        passwordEditText.setSelection(passwordEditText.getText().length());
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
