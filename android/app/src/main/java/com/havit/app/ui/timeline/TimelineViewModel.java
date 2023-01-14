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

    /**
     * Constructor for TimelineViewModel. Initializes the LiveData objects and sets default values.
     * TimelineViewModel contains the LiveData objects for the UI to observe and update
     * accordingly. It also contains the methods for loading and retrieving the timeline data.
     */
    public TimelineViewModel() {
        mText = new MutableLiveData<>();
        orderButtonName = new MutableLiveData<>();
        timelines = new MutableLiveData<>();
        timelineList = new ArrayList<>();

        user = FirebaseAuth.getInstance().getCurrentUser();

        mText.setValue("THERE'S NOTHING HERE");
    }

    /**
     * Returns the LiveData object for the list of timelines (getter)
     *
     * @return the LiveData object for the list of timelines
     */
    public LiveData<List<Timeline>> getTimelines() {
        return timelines;
    }

    /**
     * Returns the LiveData object for the text displayed when there are no timelines (getter)
     *
     * @return the LiveData object for the text displayed when there are no timelines
     */
    public LiveData<String> getText() {
        return mText;
    }

    /**
     * Returns the LiveData object for the order button name based on the current ordering of the timelines
     *
     * @return the LiveData object for the order button name
     */
    public LiveData<String> getOrderButtonName() {
        if (TimelineFragment.isOrderNewest) {
            orderButtonName.setValue("Sort by Oldest");
        } else {
            orderButtonName.setValue("Sort by Newest");
        }

        return orderButtonName;
    }

    /**
     * Loads the timelines from the Firestore database and updates the LiveData object for the list of timelines.
     * The timelines are sorted based on the current ordering specified by TimelineFragment.isOrderNewest
     */
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