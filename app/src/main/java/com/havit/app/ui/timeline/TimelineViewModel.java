package com.havit.app.ui.timeline;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TimelineViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public TimelineViewModel() {
        mText = new MutableLiveData<>();

        mText.setValue("THERE'S NOTHING HERE");
    }

    public LiveData<String> getText() {
        return mText;
    }
}