package com.havit.app.ui.store;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StoreViewModel extends ViewModel {

    private final MutableLiveData<List<Template>> templates;
    private final List<Template> templateList;

    /**
     * Initializes and loads the templates data (MutableLiveData)
     */
    public StoreViewModel() {
        super();
        // Initialize the templates LiveData
        templates = new MutableLiveData<>();
        templateList = new ArrayList<>();
        // Load the templates data
        loadTemplates();
    }

    /**
     * Getter for the templates LiveData
     *
     * @return templates LiveData
     */
    public LiveData<List<Template>> getTemplates() {
        return templates;
    }

    /**
     * Loads the templates from Firestore database.
     * If the task is successful, the documents in the collection are fetched and added to the templateList and
     * the templateList is then posted to the templates LiveData object.
     * If the task is unsuccessful, an error message is logged.
     */
    private void loadTemplates() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference colRef = db.collection("templates");

        colRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                List<DocumentSnapshot> documents = querySnapshot.getDocuments();

                for (DocumentSnapshot document : documents) {
                    Map<String, Object> data = document.getData();

                    if (data != null) {
                        String jsonString = new JSONObject(data).toString();

                        templateList.add(new Template(jsonString, document.getId()));
                    }
                }

                templates.postValue(templateList);
                // do something with the array
            } else {
                Log.e("Fatal Error", "Problem in retrieving the JSON template files from the server! It is likely that it's associated with the permission conflict.");
            }
        });
    }
}