package com.havit.app.ui.store;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StoreViewModel extends ViewModel {

    private final MutableLiveData<List<Template>> templates;

    // Create a list of templates from the subfolders
    private final List<Template> templateList;

    public StoreViewModel() {
        super();
        // Initialize the templates LiveData
        templates = new MutableLiveData<>();
        templateList = new ArrayList<>();
        // Load the templates data
        loadTemplates();
    }

    public LiveData<List<Template>> getTemplates() {
        return templates;
    }

    private void loadTemplates() {
        // Initialize the Firebase Storage service
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Define the path to the parent folder1
        String folderPath = "templates";

        // Retrieve a reference to the parent folder
        StorageReference folderRef = storage.getReference(folderPath);

        // Use the list() method to retrieve a list of all the subfolders in the parent folder
        folderRef.list(1000)
                .addOnSuccessListener(listResult -> {
                    // The list of subfolders is stored in the prefixes field
                    List<StorageReference> subfolders = listResult.getPrefixes();

                    for (StorageReference subfolder : subfolders) {
                        templateList.add(new Template(subfolder.getName()));
                    }
                    // Set the value of the templates LiveData
                    templates.postValue(templateList);
                })
                .addOnFailureListener(exception -> {
                    // An error occurred while retrieving the list of subfolders
                    Log.e("Error Retrieving the List of Templates...", exception.getMessage());
                });

        for (Template template : templateList) {
            String filePath = "templates/" + template.name + ".json";
            StorageReference nestedStorageRef = storage.getReference(filePath);

            nestedStorageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                // Convert the byte array to a string
                String jsonString = new String(bytes);

                // Parse the JSON string (see next step)
                parseJsonString(jsonString);
            });
        }
    }

    private void parseJsonString(String jsonString) {
        try {
            // Create a JSON object from the JSON string
            JSONObject jsonObject = new JSONObject(jsonString);

            // Extract the values from the JSON object
            String name = jsonObject.getString("name");
            String description = jsonObject.getString("description");

            // Print the values to the log
            Log.d("JSON Parsing", "Name: " + name);
            Log.d("JSON Parsing", "Description: " + description);
        } catch (JSONException e) {
            // If an error occurs, print the error to the log
            Log.e("JSON Parsing", "Error parsing JSON string: " + jsonString, e);
        }
    }
}