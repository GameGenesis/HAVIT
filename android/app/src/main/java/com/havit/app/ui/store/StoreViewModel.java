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

                        templateList.add(new Template(jsonString));
                    }
                }

                templates.postValue(templateList);
                // do something with the array
            } else {
                Log.e("Fatal Error", "Problem in retrieving the JSON template files from the server!");
            }
        });
    }
}