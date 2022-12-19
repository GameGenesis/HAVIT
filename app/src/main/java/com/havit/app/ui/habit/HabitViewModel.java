package com.havit.app.ui.habit;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HabitViewModel extends ViewModel {

    private final MutableLiveData<String> description;

    public HabitViewModel() {
        description = new MutableLiveData<>();
    }

    public LiveData<String> getText() {
        return description;
    }
}