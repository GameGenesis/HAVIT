package com.havit.app.ui.profile;

import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;
import java.util.Objects;

public class ProfileViewModel extends ViewModel {
    // User's profile picture in bitmap
    public Bitmap profilePictureBitmap;
    // Mutable text for displaying user's name
    private final MutableLiveData<String> mText;


    /**
     * Holds the data that is displayed in the profile fragment
     * Sets the value of mText to the user's display name, retrieved from FirebaseAuth
     */
    public ProfileViewModel() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mText = new MutableLiveData<>();

        assert user != null;
        mText.setValue("HOWDY,\n" + Objects.requireNonNull(user.getDisplayName()).toUpperCase(Locale.ROOT));
    }


    /**
     * Returns the MutableLiveData object that holds the text to be displayed in the UI
     @return mText MutableLiveData of type String representing the text to be displayed
     */
    public LiveData<String> getText() {
        return mText;
    }
}