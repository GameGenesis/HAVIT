package com.havit.app.ui.edit;

import android.graphics.Color;
import android.os.Bundle;

import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.havit.app.MainActivity;
import com.havit.app.R;
import com.havit.app.databinding.FragmentEditBinding;
import com.havit.app.ui.timeline.Timeline;
import com.havit.app.ui.timeline.TimelineArrayAdapter;

import java.util.Map;
import java.util.Objects;

public class EditFragment extends Fragment {

    private FragmentEditBinding binding;
    private LinearLayout timelineContainer;

    private Map<String, String> timestamp;
    private String totalLength;

    private final float weightSum = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TransitionInflater inflater = TransitionInflater.from(requireContext());

        setEnterTransition(inflater.inflateTransition(R.transition.fade));
        setExitTransition(inflater.inflateTransition(R.transition.fade));
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        // Hide the action bar...
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).show();

        EditViewModel editViewModel = new ViewModelProvider(this).get(EditViewModel.class);

        binding = FragmentEditBinding.inflate(inflater, container, false);

        View root = binding.getRoot();

        final TextView nameTextView = binding.nameText;
        final TextView templateNameTextView = binding.templateNameText;

        editViewModel.getName().observe(getViewLifecycleOwner(), nameTextView::setText);
        editViewModel.getTemplateName().observe(getViewLifecycleOwner(), templateNameTextView::setText);

        retrieveTimestamp();

        timelineContainer = binding.timelineContainer;
        timelineContainer.setWeightSum(weightSum);

        // Menu navigation: https://developer.android.com/jetpack/androidx/releases/activity#1.4.0-alpha01
        // The usage of an interface lets you inject your own implementation
        MenuHost menuHost = requireActivity();

        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                // Add menu items here
                // e.g. menuInflater.inflate(R.menu.bottom_nav_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                // Handle the menu selection
                if (menuItem.getItemId() == android.R.id.home) {
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.action_edit_to_timeline);
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        return root;
    }

    @SuppressWarnings("unchecked")
    private void retrieveTimestamp() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Timeline selectedTimeline = TimelineArrayAdapter.selectedTimeline;

        DocumentReference docRef = db.collection("templates").document(MainActivity.applyFileNamingScheme(selectedTimeline.selectedTemplate));

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    timestamp = (Map<String, String>) document.get("timestamp");
                    totalLength = (String) document.get("total_length");

                    // Iterate over the timestamp hashmap...
                    assert timestamp != null;

                    Map.Entry<String, String> lastEntry = null;

                    for (Map.Entry<String, String> entry : timestamp.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();

                        String[] keyArray = key.split("-");
                        String[] startTimeArray = keyArray[0].split(":");
                        String[] endTimeArray = keyArray[1].split(":");

                        int startSeconds = MainActivity.parseStringToSeconds(startTimeArray);
                        int endSeconds = MainActivity.parseStringToSeconds(endTimeArray);

                        if (startSeconds <= 0) {
                            View view = new View(requireContext());

                            // Set a random weight
                            float weight = (float) Math.random() * weightSum;
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                    0, ViewGroup.LayoutParams.MATCH_PARENT, weight);
                            view.setLayoutParams(layoutParams);
                            view.setBackgroundColor(Color.rgb(0, 0, 0));

                            timelineContainer.addView(view);
                        }

                        View view = new View(requireContext());

                        // Set a random weight
                        float weight = (float) Math.random() * weightSum;
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                0, ViewGroup.LayoutParams.MATCH_PARENT, weight);
                        view.setLayoutParams(layoutParams);

                        // Set a random color
                        int r = (int) (Math.random() * 256);
                        int g = (int) (Math.random() * 256);
                        int b = (int) (Math.random() * 256);
                        view.setBackgroundColor(Color.rgb(r, g, b));

                        timelineContainer.addView(view);
                    }

                } else {
                    // Document does not exist
                    System.out.println("Document does not exist");
                }
            } else {
                // Failed to get the document
                System.out.println("Failed to get document");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}