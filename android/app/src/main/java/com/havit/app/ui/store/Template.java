package com.havit.app.ui.store;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/* This is the official schema for the template documents stored in Firestore.
 * Every HAVIT-generated template on the store has to strictly follow this pattern.
 */

public class Template {
    public String id, name, description, music, totalLength;
    public Map<String, String> timestamp;

    public boolean featured, membershipOnly;

    /**
     * Initializes a new Template object with the specified template string and id
     *
     * @param templateString the json string that contains all the data of the template
     * @param id the id of the template
     */
    public Template(String templateString, String id) {
        timestamp = new HashMap<>();

        this.id = id;

        parseTemplateString(templateString);
    }

    /**
     * Parses the template string and extracts the values from the JSON object
     *
     * @param jsonString the json string that contains all the data of the template
     */
    private void parseTemplateString(String jsonString) {
        try {
            // Create a JSON object from the JSON string
            JSONObject jsonObject = new JSONObject(jsonString);

            // Extract the values from the JSON object
            this.name = jsonObject.getString("name");
            this.description = jsonObject.getString("description");
            this.music = jsonObject.getString("music");
            this.totalLength = jsonObject.getString("total_length");

            this.featured = jsonObject.getBoolean("featured");
            this.membershipOnly = jsonObject.getBoolean("membership_only");

            parseTimestampString(jsonObject.getString("timestamp"));

        } catch (JSONException e) {
            // If an error occurs, print the error to the log
            Log.e("JSON Parsing", "Error parsing JSON string: " + jsonString, e);
        }
    }

    /**
     * Parses the timestamp string and adds the key-value pairs to the timestamp map
     *
     * @param jsonString the json string that contains timestamp data of the template
     */
    private void parseTimestampString(String jsonString) {
        try {
            // Create a JSON object from the JSON string
            JSONObject jsonObject = new JSONObject(jsonString);

            // Iterate through the keys in the JSON object
            Iterator<String> keys = jsonObject.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                String value = jsonObject.getString(key);

                this.timestamp.put(key, value);
            }

        } catch (JSONException e) {
            // If an error occurs, print the error to the log
            Log.e("JSON Parsing", "Error parsing JSON string: " + jsonString, e);
        }
    }
}