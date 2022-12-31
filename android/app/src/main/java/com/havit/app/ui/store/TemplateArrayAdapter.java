package com.havit.app.ui.store;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.havit.app.MainActivity;
import com.havit.app.R;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

// Binding doesn't work here...

public class TemplateArrayAdapter extends ArrayAdapter<Template> {

    private FirebaseUser user;

    public TemplateArrayAdapter(Context context, List<Template> templates) {
        super(context, 0, templates);
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the template data for this position
        Template template = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.template_item, parent, false);
        }

        LinearLayout templateContainer = convertView.findViewById(R.id.template_container);

        // Lookup views for data population
        ImageView templateImageView = convertView.findViewById(R.id.template_image);

        TextView templateNameTextView = convertView.findViewById(R.id.template_name);
        TextView templateDescriptionTextView = convertView.findViewById(R.id.template_description);

        Button templateButton = convertView.findViewById(R.id.template_button);

        // Populate the data into the template view using the data object
        // templateImageView.setImageBitmap(template.thumbnail);
        templateImageView.setMaxWidth(800);

        templateNameTextView.setText(template.name.toUpperCase(Locale.ROOT));
        templateDescriptionTextView.setText(template.description.toUpperCase(Locale.ROOT));

        if (template.price != 0) {
            templateButton.setText(String.format(Locale.ROOT, "%d DOLLARS", template.price));
            templateButton.setTextColor(Color.WHITE);
            templateButton.setBackgroundColor(MainActivity.colorAccent);

        } else {
            templateButton.setText("FREE");
        }

        templateButton.setOnClickListener(v -> {


            Navigation.findNavController(v).navigate(R.id.action_store_to_timeline);
        });

        // Return the completed view to render on screen
        return convertView;
    }

    private void uploadUserData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(Objects.requireNonNull(user.getEmail()));

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                // If the user exists in the database...
                if (document.exists()) {
                    Log.d(TAG, "Document exists");

                } else {
                    // If the user does not exist in the database...
                    Log.d(TAG, "Document does not exist, adding it");
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", "John Smith");
                    data.put("age", 30);
                    docRef.set(data);
                }

            } else {
                Log.d(TAG, "Failed to get document", task.getException());
            }
        });
    }
}
