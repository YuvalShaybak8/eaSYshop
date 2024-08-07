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
import com.google.firebase.firestore.QueryDocumentSnapshot;

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

    public void updateHeaderProfilePicture() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            String loggedInUserID = mAuth.getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
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
                                    profilePic.setImageResource(R.drawable.avatar1);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                    });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        loadUserProfile();

        updateHeaderProfilePicture();

        profilePic.setOnClickListener(v -> {
            replaceFragment(new ProfileFragment(), false);
        });

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

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment != null) {
                updateUIForFragment(currentFragment);
            }
        });

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
                                    profilePic.setImageResource(R.drawable.avatar1);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
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
                .addToBackStack(null)
                .commitAllowingStateLoss();

        updateUIForFragment(fragment);

        if (refresh && fragment instanceof HomeFragment) {
            ((HomeFragment) fragment).loadPosts();
        }
    }

    public void updateUIForFragment(Fragment fragment) {
        if (fragment instanceof LoginFragment || fragment instanceof RegisterFragment) {
            bottomNavigationView.setVisibility(View.GONE);
            header.setVisibility(View.GONE);
        } else {
            bottomNavigationView.setVisibility(View.VISIBLE);
            header.setVisibility(View.VISIBLE);
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

        // Delete all posts (uncomment if needed for testing)
        // postDao.deleteAllPosts();

        addPostsFromFirestoreToSQLite(postDao);

        List<PostModel> posts = postDao.getAllPosts();

        Log.d(TAG, "Total Posts: " + posts.size());
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

    private void addPostsFromFirestoreToSQLite(PostDao postDao) {
        db.collection("posts").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    PostModel post = document.toObject(PostModel.class);
                    post.setPostID(document.getId());
                    postDao.insertPost(post);
                }
                Log.d(TAG, "All posts from Firestore have been added to SQLite.");
                Log.d(TAG, "Total Posts: " + postDao);
            } else {
                Log.d(TAG, "Error getting posts from Firestore: ", task.getException());
            }
        });
    }
}
