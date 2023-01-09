package com.havit.app.ui.edit;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.havit.app.ui.timeline.Timeline;
import com.havit.app.ui.timeline.TimelineArrayAdapter;

import java.util.Locale;

public class EditViewModel extends ViewModel {

    private final MutableLiveData<String> name, templateName;

    public EditViewModel() {
        name = new MutableLiveData<>();
        templateName = new MutableLiveData<>();
    }

    public LiveData<String> getName() {
        Timeline timeline = TimelineArrayAdapter.selectedTimeline;
        name.setValue(timeline.name.toUpperCase(Locale.ROOT));

        return name;
    }

    public LiveData<String> getTemplateName() {
        Timeline timeline = TimelineArrayAdapter.selectedTimeline;
        templateName.setValue(timeline.selectedTemplate.toUpperCase(Locale.ROOT));

        return templateName;
    }
}