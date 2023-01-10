package com.havit.app.ui.timeline;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.havit.app.MainActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TimelineViewModel extends ViewModel {

    private final MutableLiveData<String> mText, orderButtonName;
    private final MutableLiveData<List<Timeline>> timelines;
    private final List<Timeline> timelineList;

    private final FirebaseUser user;

    public TimelineViewModel() {
        mText = new MutableLiveData<>();
        orderButtonName = new MutableLiveData<>();
        timelines = new MutableLiveData<>();
        timelineList = new ArrayList<>();

        user = FirebaseAuth.getInstance().getCurrentUser();

        mText.setValue("THERE'S NOTHING HERE");
    }

    public LiveData<List<Timeline>> getTimelines() {
        return timelines;
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<String> getOrderButtonName() {
        if (TimelineFragment.isOrderNewest) {
            orderButtonName.setValue("Sort by Oldest");
        } else {
            orderButtonName.setValue("Sort by Newest");
        }

        return orderButtonName;
    }

    public void removeTimeline(int index) {
        timelineList.remove(index);
        timelines.postValue(timelineList);

        MainActivity.updateFirestoreDatabase(user, (documentReference, documentSnapshot) -> {

        });
    }

    public void loadTimelines() {
        timelineList.clear();

        MainActivity.updateFirestoreDatabase(user, (documentReference, documentSnapshot) -> {
            if (documentSnapshot.exists()) {
                // Document exists, get the fields
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> list = (List<Map<String, Object>>) documentSnapshot.get("user_timelines");

                if (list != null) {
                    for (Map<String, Object> item : list) {
                        timelineList.add(new Timeline(item));
                    }
                    
                     if (TimelineFragment.isOrderNewest) {
                          // List by newest...
                          Collections.reverse(timelineList);
                    }

                    timelines.postValue(timelineList);
                }
            }
        });
    }
}