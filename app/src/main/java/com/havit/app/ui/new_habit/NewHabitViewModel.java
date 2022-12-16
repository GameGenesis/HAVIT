package com.havit.app.ui.new_habit;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NewHabitViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public NewHabitViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is login page");
    }

    public LiveData<String> getText() {
        return mText;
    }
}