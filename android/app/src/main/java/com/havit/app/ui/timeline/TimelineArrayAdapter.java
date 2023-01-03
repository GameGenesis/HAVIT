package com.havit.app.ui.timeline;

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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.havit.app.MainActivity;
import com.havit.app.R;
import com.havit.app.ui.habit.HabitFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

// Binding doesn't work here...

public class TimelineArrayAdapter extends ArrayAdapter<Timeline> {

    private FirebaseUser user;

    public TimelineArrayAdapter(Context context, List<Timeline> timelines) {
        super(context, 0, timelines);
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the template data for this position
        Timeline timeline = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.template_item, parent, false);
        }

        LinearLayout container = convertView.findViewById(R.id.template_container);

        // Lookup views for data population
        ImageView imageView = convertView.findViewById(R.id.template_image);

        TextView nameTextView = convertView.findViewById(R.id.template_name);
        TextView descriptionTextView = convertView.findViewById(R.id.template_description);

        Button button = convertView.findViewById(R.id.template_button);

        button.setText("Edit");

        // Populate the data into the template view using the data object
        // templateImageView.setImageBitmap(template.thumbnail);
        imageView.setMaxWidth(800);

        nameTextView.setText(timeline.name.toUpperCase(Locale.ROOT));
        descriptionTextView.setText(timeline.selectedTemplate.toUpperCase(Locale.ROOT));

        // Return the completed view to render on screen
        return convertView;
    }
}
