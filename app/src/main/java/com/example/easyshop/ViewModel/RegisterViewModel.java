package com.example.easyshop.ViewModel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.easyshop.Model.UserModel;
import com.example.easyshop.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.mindrot.jbcrypt.BCrypt;

import java.io.ByteArrayOutputStream;

public class RegisterViewModel extends ViewModel {

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;

    private final MutableLiveData<Boolean> _registerResult = new MutableLiveData<>();
    public LiveData<Boolean> registerResult = _registerResult;

    public RegisterViewModel() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    public void registerUser(String email, String password, String username, Context context) {
        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            _registerResult.setValue(false);
            return;
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String userId = firebaseAuth.getCurrentUser().getUid();
                StorageReference storageRef = storage.getReference().child("profile_pictures").child(userId + ".jpg");

                Bitmap bitmap = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.avatar1)).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = storageRef.putBytes(data);

                uploadTask.addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String profilePicUrl = uri.toString();
                    UserModel userModel = new UserModel(
                            userId,
                            username,
                            email,
                            hashedPassword,
                            profilePicUrl
                    );
                    userModel.setLoggedIn(true);

                    firestore.collection("users").document(userId).set(userModel)
                            .addOnSuccessListener(aVoid -> _registerResult.setValue(true))
                            .addOnFailureListener(e -> _registerResult.setValue(false));
                })).addOnFailureListener(e -> _registerResult.setValue(false));
            } else {
                _registerResult.setValue(false);
            }
        });
    }
}
