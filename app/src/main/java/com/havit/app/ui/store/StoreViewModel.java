package com.havit.app.ui.store;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class StoreViewModel extends ViewModel {

    private final MutableLiveData<List<Template>> templates;

    public StoreViewModel() {
        super();
        // Initialize the templates LiveData
        templates = new MutableLiveData<>();
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
                    // Create a list of templates from the subfolders
                    List<Template> templateList = new ArrayList<>();
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
    }
}