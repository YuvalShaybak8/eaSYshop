package com.example.easyshop.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.easyshop.R;
import com.example.easyshop.Model.PostModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.auth.FirebaseAuth;


public class PaymentFragment extends Fragment {

    private PostModel post;

    public PaymentFragment(PostModel post) {
        this.post = post;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment_page, container, false);
        EditText cardNumber = view.findViewById(R.id.cardNumberEditText);
        EditText cardHolder = view.findViewById(R.id.cardHolderEditText);
        EditText expiryDate = view.findViewById(R.id.expirationDateEditText);
        EditText cvv = view.findViewById(R.id.cvvEditText);
        Button buyButton = view.findViewById(R.id.buyButton);
        buyButton.setOnClickListener(v -> handlePayment());
        return view;

    }

    public void handlePayment() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Update the post as purchased
        post.setPurchased(true);
        post.setBuyerID(FirebaseAuth.getInstance().getCurrentUser().getUid());

        db.collection("posts").document(post.getPostID())
                .set(post)
                .addOnSuccessListener(aVoid -> {
                    // Add the post to "my orders" section in the user's database
                    String userID = post.getBuyerID();
                    db.collection("users").document(userID).update("myOrders", FieldValue.arrayUnion(post))
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(getContext(), "Purchase successful", Toast.LENGTH_SHORT).show();
                                // Navigate back to the home fragment
                                FragmentActivity activity = (FragmentActivity) getContext();
                                FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragment_container, new HomeFragment());
                                transaction.commit();
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add to orders", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update post", Toast.LENGTH_SHORT).show());

    }
}
