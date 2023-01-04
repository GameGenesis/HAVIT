package com.havit.app.ui.timeline;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TimelineViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private final MutableLiveData<List<Timeline>> timelines;
    private final List<Timeline> timelineList;

    private final FirebaseUser user;

    public TimelineViewModel() {
        mText = new MutableLiveData<>();
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

    public void clearTimelines() {
        timelineList.clear();
        timelines.postValue(timelineList);
    }

    public void loadTimelines() {
        timelineList.clear();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(Objects.requireNonNull(user.getEmail()));

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Document exists, get the fields
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> list = (List<Map<String, Object>>) document.get("user_timelines");

                    if (list != null) {
                        for (Map<String, Object> item : list) {
                            timelineList.add(new Timeline(item));
                        }

                        timelines.postValue(timelineList);
                    }
                } else {
                    // Document does not exist
                    System.out.println("Document does not exist");
                }
            } else {
                // Failed to get the document
                System.out.println("Failed to get document");
            }
        });
    }
}