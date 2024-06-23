package com.example.easyshop.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
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
import com.example.easyshop.Model.UserModel;
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

        cardNumber.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            private int hyphenCountBefore;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                hyphenCountBefore = countHyphens(s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Prevent infinite loop
                if (isFormatting) {
                    isFormatting = false;
                    return;
                }

                isFormatting = true;

                StringBuilder formatted = new StringBuilder(s.toString().replace("-", ""));
                int hyphenCountAfter = countHyphens(formatted);

                // Add hyphens
                for (int i = 4; i < formatted.length(); i += 5) {
                    formatted.insert(i, "-");
                }

                // Set formatted text
                cardNumber.setText(formatted.toString());
                cardNumber.setSelection(formatted.length());
            }

            @Override
            public void afterTextChanged(Editable s) {}

            private int countHyphens(CharSequence s) {
                int count = 0;
                for (int i = 0; i < s.length(); i++) {
                    if (s.charAt(i) == '-') {
                        count++;
                    }
                }
                return count;
            }
        });

        expiryDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 2 && !s.toString().contains("/")) {
                    s.append("/");
                }
            }
        });

        cvv.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        buyButton.setOnClickListener(v -> handlePayment());
        return view;
    }

    public void handlePayment() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        post.setPurchased(true);
        post.setBuyerID(FirebaseAuth.getInstance().getCurrentUser().getUid());

        db.collection("posts").document(post.getPostID())
                .set(post)
                .addOnSuccessListener(aVoid -> {
                    String userID = post.getBuyerID();
                    db.collection("users").document(userID).update("myOrders", FieldValue.arrayUnion(post))
                            .addOnSuccessListener(aVoid1 -> {
                                removeFromGeneralPosts();
                                removeFromSellerPosts();
                                Toast.makeText(getContext(), "Purchase successful", Toast.LENGTH_SHORT).show();
                                FragmentActivity activity = (FragmentActivity) getContext();
                                FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragment_container, new HomeFragment());
                                transaction.commit();
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add to orders", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update post", Toast.LENGTH_SHORT).show());
    }

    private void removeFromGeneralPosts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts").document(post.getPostID()).delete()
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to remove post from general posts", Toast.LENGTH_SHORT).show());
    }

    private void removeFromSellerPosts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(post.getOwnerID()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserModel user = documentSnapshot.toObject(UserModel.class);
                        if (user != null && user.myPosts != null) {
                            PostModel postToRemove = null;
                            for (PostModel userPost : user.myPosts) {
                                if (userPost.getPostID().equals(post.getPostID())) {
                                    postToRemove = userPost;
                                    break;
                                }
                            }
                            if (postToRemove != null) {
                                user.myPosts.remove(postToRemove);
                                db.collection("users").document(post.getOwnerID()).set(user)
                                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update seller's posts", Toast.LENGTH_SHORT).show());
                            }
                        }
                    }
                });
    }
}
