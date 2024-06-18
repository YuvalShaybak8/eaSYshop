package com.example.easyshop.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.easyshop.R;
import com.example.easyshop.activities.MainActivity;

public class MenuBottomSheetFragment extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.logout).setOnClickListener(v -> {
            // Handle Logout
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).replaceFragment(new LoginFragment());
                dismiss();
            }
        });
        view.findViewById(R.id.my_profile).setOnClickListener(v -> {
            // Handle My Profile
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).replaceFragment(new ProfileFragment());
                dismiss();
            }
        });
        view.findViewById(R.id.my_wish_list).setOnClickListener(v -> {
            // Handle My Wish List
            // Add the fragment navigation here
        });
        view.findViewById(R.id.my_orders).setOnClickListener(v -> {
            // Handle My Orders
            // Add the fragment navigation here
        });
        view.findViewById(R.id.my_posts).setOnClickListener(v -> {
            // Handle My Posts
            // Add the fragment navigation here
        });
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onBottomSheetDismissed();
        }
    }
}