package com.example.easyshop.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
//import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import com.example.easyshop.Fragments.LoginFragment;
import com.example.easyshop.Fragments.ProfileFragment;
import com.example.easyshop.Fragments.RegisterFragment;
import com.example.easyshop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

//    private NavController navController;
    private FrameLayout menuContainer;
    private BottomNavigationView bottomNavigationView;
    private ImageView profilePic;
    private TextView title;
    private View header;

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

//        // Set up NavController for bottom navigation
//        NavController navController = Navigation.findNavController(this, R.id.fragment_container);
//        NavigationUI.setupWithNavController(bottomNavigationView, navController);
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
