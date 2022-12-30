package com.havit.app.ui.store;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.havit.app.R;

import java.util.List;

public class TemplateArrayAdapter extends ArrayAdapter<Template> {

    public TemplateArrayAdapter(Context context, List<Template> templates) {
        super(context, 0, templates);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the template data for this position
        Template template = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.template_item, parent, false);
        }

        // Lookup views for data population
        ImageView templateImageView = convertView.findViewById(R.id.template_image);
        TextView templateNameTextView = convertView.findViewById(R.id.template_name);

        // Populate the data into the template view using the data object
        // templateImageView.setImageResource(template.imageResId);
        templateNameTextView.setText(template.name);

        // Return the completed view to render on screen
        return convertView;
    }
}
