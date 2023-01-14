package com.havit.app.ui.timeline;

import java.util.ArrayList;
import java.util.Map;

public class Timeline {
    public String name, selectedTemplate, time;
    public ArrayList<String> dates;

    public Timeline(Map<String, Object> metadata) {
        parseTimelineString(metadata);
    }

    /**
     * Parses the value of the user_timelines in user.json located in the Firebase Firestore
     *
     * @param metadata the json data to parse
     */
    @SuppressWarnings("unchecked")
    private void parseTimelineString(Map<String, Object> metadata) {
        this.name = (String) metadata.get("name");
        this.selectedTemplate = (String) metadata.get("selected_template");
        this.dates = (ArrayList<String>) metadata.get("dates");
        this.time = (String) metadata.get("time");
    }
}
