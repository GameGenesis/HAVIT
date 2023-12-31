package com.havit.app.ui.timeline;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.havit.app.R;
import com.havit.app.databinding.FragmentTimelineBinding;

import java.util.Objects;

/*
TUTORIAL ON USING NAVIGATION IN ANDROID FRAGMENTS:
https://developer.android.com/codelabs/basic-android-kotlin-training-fragments-navigation-component#0
https://developer.android.com/guide/navigation/navigation-navigate#java
 */

public class TimelineFragment extends Fragment {

    private FragmentTimelineBinding binding;
    private TimelineViewModel timelineViewModel;
    private TimelineArrayAdapter adapter;

    private ListView listView;
    private TextView textView;

    private Button orderButton;

    public static boolean isOrderNewest = true;

    /**
     * Sets the enter and exit fade transitions
     *
     * @param savedInstanceState this fragment gets re-constructed from a previous saved state as given by this parameter
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TransitionInflater inflater = TransitionInflater.from(requireContext());

        setEnterTransition(inflater.inflateTransition(R.transition.fade));
        setExitTransition(inflater.inflateTransition(R.transition.fade));
    }

    /**
     * Load the timelines and set up the new habit and order button.
     * Observes data from the ViewModel to update the UI.
     *
     * @param inflater the LayoutInflater object that can be used to inflate views in the fragment
     * @param container the parent view that the fragment's UI is attached to
     * @param savedInstanceState this fragment is being re-constructed from a previous saved state
     * @return The view for the fragment's UI
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        // Hide the action bar...
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).hide();

        timelineViewModel = new ViewModelProvider(this).get(TimelineViewModel.class);

        binding = FragmentTimelineBinding.inflate(inflater, container, false);

        View root = binding.getRoot();

        ImageButton newHabitButton = binding.newHabitActionButton;
        orderButton = binding.orderButton;

        listView = binding.timelineListView;
        textView = binding.textNotifications;

        newHabitButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_timeline_to_habit));
        orderButton.setOnClickListener(v -> {
            isOrderNewest = !isOrderNewest;
            Navigation.findNavController(v).navigate(R.id.action_timeline_to_timeline);
        });

        timelineViewModel.getOrderButtonName().observe(getViewLifecycleOwner(), orderButton::setText);
        timelineViewModel.loadTimelines();

        Log.d("reload", "Reloading timelines");

        return root;
    }

    /**
     * Sets up the observations for timelineViewModel's LiveData objects,
     * updates the UI with the data, and controls the visibility of
     * certain views based on the data received.
     */
    @Override
    public void onResume() {
        super.onResume();

        timelineViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        timelineViewModel.getTimelines().observe(getViewLifecycleOwner(), timelines -> {
            // Update the UI with the templates data
            adapter = new TimelineArrayAdapter(requireContext(), timelines);
            listView.setAdapter(adapter);

            if (timelines.isEmpty()) {
                orderButton.setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
                textView.setVisibility(View.VISIBLE);
            } else {
                orderButton.setVisibility(View.VISIBLE);
                listView.setVisibility(View.VISIBLE);
                textView.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Called when the fragment's view is being destroyed.
     * Sets the binding variable to null to avoid memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}