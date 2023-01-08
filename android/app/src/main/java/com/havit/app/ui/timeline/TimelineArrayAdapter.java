package com.havit.app.ui.timeline;

import android.content.Context;
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

import com.havit.app.R;

import java.util.List;
import java.util.Locale;

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
        button.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_timeline_to_edit);
        });

        // Populate the data into the template view using the data object
        // templateImageView.setImageBitmap(template.thumbnail);
        imageView.setMaxWidth(800);

        nameTextView.setText(timeline.name.toUpperCase(Locale.ROOT));
        descriptionTextView.setText(timeline.selectedTemplate.toUpperCase(Locale.ROOT));

        // Return the completed view to render on screen
        return convertView;
    }
}
