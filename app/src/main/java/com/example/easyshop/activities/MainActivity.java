package com.example.easyshop.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.easyshop.Fragments.CreatePostFragment;
import com.example.easyshop.Fragments.HomeFragment;
import com.example.easyshop.Fragments.LoginFragment;
import com.example.easyshop.Fragments.ProfileFragment;
import com.example.easyshop.Fragments.RegisterFragment;
import com.example.easyshop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private FrameLayout menuContainer;
    private BottomNavigationView bottomNavigationView;
    private ImageView profilePic;
    private TextView title;
    private View header;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        menuContainer = findViewById(R.id.menu_container);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        profilePic = findViewById(R.id.profilePic);
        title = findViewById(R.id.title);
        header = findViewById(R.id.header);

        if (savedInstanceState == null) {
            replaceFragment(new LoginFragment());
        }

        // Set profile picture
        String profilePicUrl = "url_to_profile_picture"; // Replace with actual URL
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
                // Replace with the appropriate fragment for the menu
                return true;
            } else {
                return false;
            }
        });

        // Add this line to show header and bottom navigation when the app launches with HomeFragment
        updateUIForFragment(new HomeFragment());

        // Add a fragment transaction listener to update the UI when the fragment changes
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment != null) {
                updateUIForFragment(currentFragment);
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

        updateUIForFragment(fragment);
    }

    private void updateUIForFragment(Fragment fragment) {
        if (fragment instanceof LoginFragment || fragment instanceof RegisterFragment) {
            bottomNavigationView.setVisibility(View.GONE);
            header.setVisibility(View.GONE); // Hide header
        } else {
            bottomNavigationView.setVisibility(View.VISIBLE);
            header.setVisibility(View.VISIBLE); // Show header
        }
    }
}
