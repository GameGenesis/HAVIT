package com.havit.app.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;
import java.util.Objects;

public class ProfileViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ProfileViewModel() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mText = new MutableLiveData<>();

        assert user != null;
        mText.setValue("HOWDY,\n" + Objects.requireNonNull(user.getDisplayName()).toUpperCase(Locale.ROOT));
    }

    public LiveData<String> getText() {
        return mText;
    }
}