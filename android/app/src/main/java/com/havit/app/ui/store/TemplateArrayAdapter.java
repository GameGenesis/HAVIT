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

import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.FieldValue;
import com.havit.app.MainActivity;
import com.havit.app.R;
import com.havit.app.ui.habit.HabitFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        Button templateButton2 = convertView.findViewById(R.id.template_button2);

        // We don't need a secondary button here...
        templateButton2.setVisibility(View.GONE);

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
            Map<String, Object> timelineMetaData = new HashMap<>();
            timelineMetaData.put("name", HabitFragment.name);
            timelineMetaData.put("dates", HabitFragment.daysPicked);
            timelineMetaData.put("time", HabitFragment.time);
            timelineMetaData.put("selected_template", template.name);

            uploadUserData(timelineMetaData);

            Navigation.findNavController(v).navigate(R.id.action_store_to_camera);
        });

        // Return the completed view to render on screen
        return convertView;
    }

    private void uploadUserData(Map<String, Object> timelineMetaData) {
        MainActivity.updateFirestoreDatabase(user, (documentReference, documentSnapshot) -> {
            // If the user exists in the database...
            if (documentSnapshot.exists()) {
                Log.d(TAG, "Document exists");

                Map<String, Object> updates = new HashMap<>();
                updates.put("user_timelines", FieldValue.arrayUnion(timelineMetaData));

                documentReference.update(updates)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Document update successful!"))
                        .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));

            } else {
                // If the user does not exist in the database...
                Log.d(TAG, "Document does not exist, adding it");

                Map<String, Object> data = new HashMap<>();

                ArrayList<Map<String, Object>> array = new ArrayList<>();
                array.add(timelineMetaData);

                data.put("name", user.getDisplayName());
                data.put("user_timelines", array);

                documentReference.set(data);
            }
        });
    }
}
