package com.havit.app.ui.timeline;

import java.util.Map;

public class Timeline {
    public String name, selectedTemplate;

    public Timeline(Map<String, Object> metadata) {
        parseTimelineString(metadata);
    }

    private void parseTimelineString(Map<String, Object> metadata) {
        this.name = (String) metadata.get("name");
        this.selectedTemplate = (String) metadata.get("selected_template");
    }
}
