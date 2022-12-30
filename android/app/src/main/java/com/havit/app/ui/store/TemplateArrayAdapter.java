package com.havit.app.ui.store;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.havit.app.MainActivity;
import com.havit.app.R;

import java.util.List;
import java.util.Locale;

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

        LinearLayout templateContainer = convertView.findViewById(R.id.template_container);

        // Lookup views for data population
        ImageView templateImageView = convertView.findViewById(R.id.template_image);

        TextView templateNameTextView = convertView.findViewById(R.id.template_name);
        TextView templateDescriptionTextView = convertView.findViewById(R.id.template_description);
        TextView templatePriceTextView = convertView.findViewById(R.id.template_price);

        CheckBox templateCheckBox = convertView.findViewById(R.id.template_checkbox);

        templateContainer.setOnClickListener(v -> {
            // Do something when the LinearLayout is clicked
            templateCheckBox.toggle();
        });

        templateCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // The checkbox is checked
                // Do something here
            } else {
                // The checkbox is not checked
                // Do something here
            }
        });

        // Populate the data into the template view using the data object
        // templateImageView.setImageBitmap(template.thumbnail);
        templateImageView.setMaxWidth(800);

        templateNameTextView.setText(template.name.toUpperCase(Locale.ROOT));
        templateDescriptionTextView.setText(template.description.toUpperCase(Locale.ROOT));

        if (template.price != 0) {
            templatePriceTextView.setText(String.format(Locale.ROOT, "$ %d", template.price));
            templatePriceTextView.setTextColor(MainActivity.colorAccent);

        } else {
            templatePriceTextView.setText("FREE");
        }

        // Return the completed view to render on screen
        return convertView;
    }
}
