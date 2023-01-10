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

import com.havit.app.R;

import java.util.List;
import java.util.Locale;

public class TimelineArrayAdapter extends ArrayAdapter<Timeline> {

    public static Timeline selectedTimeline;

    public TimelineArrayAdapter(Context context, List<Timeline> timelines) {
        super(context, 0, timelines);
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
        Button button2 = convertView.findViewById(R.id.template_button2);

        button.setText("Compose");
        button.setOnClickListener(v -> {
            selectedTimeline = timeline;
            Navigation.findNavController(v).navigate(R.id.action_timeline_to_edit);
        });

        button2.setVisibility(View.GONE);

        // Populate the data into the template view using the data object
        // templateImageView.setImageBitmap(template.thumbnail);
        imageView.setMaxWidth(800);

        nameTextView.setText(timeline.name.toUpperCase(Locale.ROOT));
        descriptionTextView.setText(timeline.selectedTemplate.toUpperCase(Locale.ROOT));

        // Return the completed view to render on screen
        return convertView;
    }
}
