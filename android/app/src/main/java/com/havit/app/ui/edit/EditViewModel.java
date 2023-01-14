package com.havit.app.ui.edit;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.havit.app.ui.timeline.Timeline;
import com.havit.app.ui.timeline.TimelineArrayAdapter;

import java.util.Locale;

public class EditViewModel extends ViewModel {
    // store the name and template name of the selected timeline.
    private final MutableLiveData<String> name, templateName;


    /**
     * Initializes the name and templateName MutableLiveData fields
     */
    public EditViewModel() {
        name = new MutableLiveData<>();
        templateName = new MutableLiveData<>();
    }


    /**
     * Returns the name of selected timeline in an uppercase format
     * Sets the value of the name MutableLiveData field to the name of the selected timeline
     * @return the name of selected timeline in an uppercase format as a LiveData object.
     */
    public LiveData<String> getName() {
        Timeline timeline = TimelineArrayAdapter.selectedTimeline;
        name.setValue(timeline.name.toUpperCase(Locale.ROOT));

        return name;
    }


    /**
     * Returns the name of selected template in an uppercase format
     * Sets the value of the templateName MutableLiveData field to the name of the selected template
     * @return the name of selected template in an uppercase format as a LiveData object
     */
    public LiveData<String> getTemplateName() {
        Timeline timeline = TimelineArrayAdapter.selectedTimeline;
        templateName.setValue(timeline.selectedTemplate.toUpperCase(Locale.ROOT));

        return templateName;
    }
}