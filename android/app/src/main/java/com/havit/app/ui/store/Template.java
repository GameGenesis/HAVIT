package com.havit.app.ui.store;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/* This is the official schema for the template documents stored in Google Firestore.
 * Every HAVIT-generated template on the store has to strictly follow this pattern.
 */

public class Template {
    public String id, name, description, music;
    public Map<String, String> timestamp;
    public Bitmap thumbnail;

    public boolean featured;
    public int price;

    public Template(String templateString, String id) {
        timestamp = new HashMap<>();

        parseTemplateString(templateString);
        // getThumbnailFromStorage("templates/thumbnails/" + id + ".jpg");
        // Currently, the thumbnail image has to be a JPEG file...
    }

    private void parseTemplateString(String jsonString) {
        try {
            // Create a JSON object from the JSON string
            JSONObject jsonObject = new JSONObject(jsonString);

            // Extract the values from the JSON object
            this.name = jsonObject.getString("name");
            this.description = jsonObject.getString("description");
            this.music = jsonObject.getString("music");

            this.featured = jsonObject.getBoolean("featured");
            this.price = jsonObject.getInt("price");

            parseTimestampString(jsonObject.getString("timestamp"));

        } catch (JSONException e) {
            // If an error occurs, print the error to the log
            Log.e("JSON Parsing", "Error parsing JSON string: " + jsonString, e);
        }
    }

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

    private void getThumbnailFromStorage(String imagePath) {
        // Get a reference to the Cloud Storage bucket
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Get a reference to the image file in the bucket
        StorageReference imageRef = storageRef.child(imagePath);

        // Download the image file
        final long ONE_MEGABYTE = 1024 * 1024;
        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            // Convert the downloaded bytes into a Bitmap
            thumbnail = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        });
    }
}