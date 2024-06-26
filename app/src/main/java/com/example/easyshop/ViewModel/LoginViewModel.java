package com.example.easyshop.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;

public class LoginViewModel extends ViewModel {

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final MutableLiveData<Boolean> _loginResult = new MutableLiveData<>();

    public LiveData<Boolean> getLoginResult() {
        return _loginResult;
    }

    public void login(String email, String password) {
        if (email != null && !email.isEmpty() && password != null && !password.isEmpty()) {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                _loginResult.setValue(task.isSuccessful());
            });
        } else {
            _loginResult.setValue(false);
        }
    }
}
