package com.example.easyshop.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.easyshop.R;

import com.example.easyshop.Fragments.CreatePostFragment;
import com.example.easyshop.Fragments.LoginFragment;
import com.example.easyshop.Fragments.RegisterFragment;
import com.example.easyshop.Fragments.HomeFragment;
import com.example.easyshop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private FrameLayout menuContainer;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        menuContainer = findViewById(R.id.menu_container);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            replaceFragment(new LoginFragment());
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();

        updateBottomNavigationVisibility(fragment);
    }

    private void updateBottomNavigationVisibility(Fragment fragment) {
        if (fragment instanceof LoginFragment || fragment instanceof RegisterFragment || fragment instanceof CreatePostFragment) {
            bottomNavigationView.setVisibility(View.GONE);
        } else {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
    }
}