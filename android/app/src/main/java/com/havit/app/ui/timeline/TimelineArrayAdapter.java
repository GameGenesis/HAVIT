package com.havit.app.ui.timeline;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.havit.app.MainActivity;
import com.havit.app.R;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class TimelineArrayAdapter extends ArrayAdapter<Timeline> {

    public static Timeline selectedTimeline;

    private final FirebaseUser user;

    /**
     * Initializes a custom ArrayAdapter to display a list of Timeline objects in a ListView
     *
     * @param context - The current context
     * @param timelines - The list of Timeline objects to be displayed in the ListView
     */
    public TimelineArrayAdapter(Context context, List<Timeline> timelines) {
        super(context, 0, timelines);
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    /**
     * Gets a View that displays the data at the specified position in the data set.
     * It uses the template_item.xml layout to display the data and has 2 buttons, "Compose" and "Edit"
     *
     * @param position - The position of the item within the adapter's data set of the item whose view we want
     * @param convertView - The old view to reuse, if possible
     * @param parent - The parent that this view will eventually be attached to
     * @return a View corresponding to the data at the specified position
     */
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
        imageView.setVisibility(View.GONE);

        TextView nameTextView = convertView.findViewById(R.id.template_name);
        TextView descriptionTextView = convertView.findViewById(R.id.template_description);

        Button button = convertView.findViewById(R.id.template_button);
        Button button2 = convertView.findViewById(R.id.template_button2);

        button.setText("Compose");
        button.setOnClickListener(v -> {
            selectedTimeline = timeline;
            Navigation.findNavController(v).navigate(R.id.action_timeline_to_edit);
        });

        button2.setText("Edit");
        button2.setOnClickListener(v -> {
            // Inflate the menu resource
            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            popupMenu.inflate(R.menu.menu_popup);

            // Set a listener for menu items
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.menu_item_1:
                        // Do something here
                        return true;

                    case R.id.menu_item_2:
                        // Do something here
                        // Write code for deleting a timeline here...
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("ERASE TIMELINE");
                        builder.setMessage("ARE YOU SURE YOU WANT TO DELETE THIS?");

                        builder.setPositiveButton("Delete", (dialog, which) -> {
                            MainActivity.updateFirestoreDatabase(user, (documentReference, documentSnapshot) -> {
                                // Get the current value of the array field
                                List<Object> array = (List<Object>) documentSnapshot.get("user_timelines");

                                if (TimelineFragment.isOrderNewest && array != null) {
                                    // List by newest...
                                    Collections.reverse(array);
                                }

                                // Replace the element at the specified index with null
                                Objects.requireNonNull(array).remove(position);

                                documentReference.update("user_timelines", array)
                                        .addOnSuccessListener(aVoid -> {
                                            // update successful
                                            notifyDataSetChanged();
                                            Navigation.findNavController(parent).navigate(R.id.action_timeline_to_timeline);
                                        })
                                        .addOnFailureListener(e -> {
                                            // update failed
                                        });
                            });
                        });

                        builder.setNegativeButton("Cancel", (dialog, which) -> {
                            // Cancel the dialog
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();

                    default:
                        return false;
                }
            });

            // Show the menu
            popupMenu.show();
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
