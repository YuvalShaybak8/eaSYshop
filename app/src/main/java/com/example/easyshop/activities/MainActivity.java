package com.example.easyshop.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.easyshop.Fragments.CreatePostFragment;
import com.example.easyshop.Fragments.HomeFragment;
import com.example.easyshop.Fragments.LoginFragment;
import com.example.easyshop.Fragments.MenuBottomSheetFragment;
import com.example.easyshop.Fragments.ProfileFragment;
import com.example.easyshop.Fragments.RegisterFragment;
import com.example.easyshop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;
import com.google.android.libraries.places.api.Places;

public class MainActivity extends AppCompatActivity {

    private FrameLayout menuContainer;
    private BottomNavigationView bottomNavigationView;
    private ImageView profilePic;
    private TextView title;
    private View header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the Places API with your API key
        Places.initialize(getApplicationContext(), "AIzaSyAE412NbG66NdE68Fap8_ncqt_crHnxYTE");

        menuContainer = findViewById(R.id.menu_container);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        profilePic = findViewById(R.id.profilePic);
        title = findViewById(R.id.title);
        header = findViewById(R.id.header);

        if (savedInstanceState == null) {
            replaceFragment(new LoginFragment());
        }

        // Set profile picture
        String profilePicUrl = "@drawable/avatar1"; // Replace with your profile picture URL
        if (!profilePicUrl.isEmpty()) {
            Picasso.get().load(profilePicUrl).into(profilePic);
        } else {
            profilePic.setImageResource(R.drawable.avatar); // default avatar
        }

        profilePic.setOnClickListener(v -> {
            // Handle profile picture click to navigate to profile details
            replaceFragment(new ProfileFragment());
        });

        // Handle bottom navigation item selection
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_home) {
                replaceFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.action_create_post) {
                replaceFragment(new CreatePostFragment());
                return true;
            } else if (itemId == R.id.action_menu) {
                // Show bottom sheet menu
                new MenuBottomSheetFragment().show(getSupportFragmentManager(), "MenuBottomSheetFragment");
                bottomNavigationView.setVisibility(View.GONE);
                return true;
            } else {
                return false;
            }
        });

        // Add a fragment transaction listener to update the UI when the fragment changes
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment != null) {
                updateUIForFragment(currentFragment);
            }
        });
    }

    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

        updateUIForFragment(fragment);
    }

    public void updateUIForFragment(Fragment fragment) {
        if (fragment instanceof LoginFragment || fragment instanceof RegisterFragment) {
            bottomNavigationView.setVisibility(View.GONE);
            header.setVisibility(View.GONE); // Hide header
        } else {
            bottomNavigationView.setVisibility(View.VISIBLE);
            header.setVisibility(View.VISIBLE); // Show header
        }
    }

    @Override
    public void onBackPressed() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView.getVisibility() == View.GONE) {
            bottomNavigationView.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    public void onBottomSheetDismissed() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setVisibility(View.VISIBLE);
    }
}
