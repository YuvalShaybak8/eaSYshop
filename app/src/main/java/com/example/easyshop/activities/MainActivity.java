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
import com.example.easyshop.Fragments.WishlistFragment;
import com.example.easyshop.Model.UserModel;
import com.example.easyshop.R;
import com.example.easyshop.Utils.KeyboardUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private FrameLayout menuContainer;
    private BottomNavigationView bottomNavigationView;
    private CircleImageView profilePic;
    private TextView title;
    private View header;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String loggedInUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firestore and Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        menuContainer = findViewById(R.id.menu_container);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        profilePic = findViewById(R.id.profilePic);
        title = findViewById(R.id.title);
        header = findViewById(R.id.header);

        if (savedInstanceState == null) {
            checkLoginStatus();
        }

        // Set profile picture
        loadUserProfile();

        profilePic.setOnClickListener(v -> {
            // Handle profile picture click to navigate to profile details
            replaceFragment(new ProfileFragment(), false);
        });

        // Handle bottom navigation item selection
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_home) {
                replaceFragment(new HomeFragment(), false);
                return true;
            } else if (itemId == R.id.action_create_post) {
                replaceFragment(new CreatePostFragment(), false);
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

        // Keyboard visibility listener
        KeyboardUtils.setKeyboardVisibilityListener(this, isVisible -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof LoginFragment || currentFragment instanceof RegisterFragment) {
                bottomNavigationView.setVisibility(View.GONE);
            } else {
                bottomNavigationView.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void checkLoginStatus() {
        if (mAuth.getCurrentUser() != null) {
            String loggedInUserID = mAuth.getCurrentUser().getUid();
            db.collection("users").document(loggedInUserID)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            UserModel user = documentSnapshot.toObject(UserModel.class);
                            if (user != null && user.isLoggedIn()) {
                                replaceFragment(new HomeFragment(), false);
                            } else {
                                replaceFragment(new LoginFragment(), false);
                            }
                        } else {
                            replaceFragment(new LoginFragment(), false);
                        }
                    })
                    .addOnFailureListener(e -> {
                        replaceFragment(new LoginFragment(), false);
                    });
        } else {
            replaceFragment(new LoginFragment(), false);
        }
    }

    private void loadUserProfile() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            String loggedInUserID = mAuth.getCurrentUser().getUid();
            db.collection("users").document(loggedInUserID)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            UserModel user = documentSnapshot.toObject(UserModel.class);
                            if (user != null) {
                                String profilePicUrl = user.getProfilePicUrl();
                                if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                                    Picasso.get().load(profilePicUrl).fit().centerInside().into(profilePic, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            // Ensure the profile picture fits within the circle shape
                                            profilePic.setBackgroundResource(R.drawable.circle_shape);
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            profilePic.setImageResource(R.drawable.avatar1); // default avatar
                                        }
                                    });
                                } else {
                                    profilePic.setImageResource(R.drawable.avatar1); // default avatar
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors here
                    });
        }
    }

    public void replaceFragment(Fragment fragment, boolean refresh) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null) // Add this line to add the transaction to the back stack
                .commitAllowingStateLoss(); // Use commitAllowingStateLoss to avoid potential crashes due to state loss

        updateUIForFragment(fragment);

        if (refresh && fragment instanceof HomeFragment) {
            ((HomeFragment) fragment).loadPosts();
        }
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
