package com.example.easyshop.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.easyshop.Fragments.CreatePostFragment;
import com.example.easyshop.Fragments.HomeFragment;
import com.example.easyshop.Fragments.LoginFragment;
import com.example.easyshop.Fragments.MenuBottomSheetFragment;
import com.example.easyshop.Fragments.ProfileFragment;
import com.example.easyshop.Fragments.RegisterFragment;
import com.example.easyshop.Model.CommentModel;
import com.example.easyshop.Model.PostModel;
import com.example.easyshop.Model.UserModel;
import com.example.easyshop.R;
import com.example.easyshop.Services.DatabaseHelper;
import com.example.easyshop.Services.PostDao;
import com.example.easyshop.Utils.KeyboardUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

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

    private static final String TAG = "MainActivity";

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

        // Test SQLite operations
        testSQLiteOperations();
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
                                    Glide.with(this).load(profilePicUrl).into(profilePic);
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

    public void updateHeaderProfilePicture(String profilePicUrl) {
        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
            Glide.with(this).load(profilePicUrl).into(profilePic);
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

    private void testSQLiteOperations() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        PostDao postDao = new PostDao(this);

        // Create test data
        List<CommentModel> comments = new ArrayList<>();
        comments.add(new CommentModel("user1", "This is a comment", "post1"));

        PostModel post = new PostModel(
                "post1",
                "Test Post",
                "This is a test description",
                "image_url",
                100.0,
                "Test Location",
                "owner1",
                new Timestamp(System.currentTimeMillis() / 1000, 0),
                false,
                null,
                new ArrayList<>()
        );

        post.setComments(comments);

        // Insert the test data
        postDao.insertPost(post);

        // Query the data
        List<PostModel> posts = postDao.getAllPosts();

        // Log the results
        for (PostModel p : posts) {
            Log.d(TAG, "Post ID: " + p.getPostID());
            Log.d(TAG, "Title: " + p.getTitle());
            Log.d(TAG, "Description: " + p.getDescription());
            Log.d(TAG, "Image: " + p.getImage());
            Log.d(TAG, "Price: " + p.getPrice());
            Log.d(TAG, "Location: " + p.getLocation());
            Log.d(TAG, "Owner ID: " + p.getOwnerID());
            Log.d(TAG, "Timestamp: " + p.getTimestamp().toDate().toString());
            Log.d(TAG, "Purchased: " + p.isPurchased());
            Log.d(TAG, "Buyer ID: " + p.getBuyerID());
            for (CommentModel c : p.getComments()) {
                Log.d(TAG, "Comment User ID: " + c.getUserID());
                Log.d(TAG, "Comment Text: " + c.getCommentText());
            }
        }
    }
}
