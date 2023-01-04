package com.havit.app.ui.timeline;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        // Hide the action bar...
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).hide();

        timelineViewModel =
                new ViewModelProvider(this).get(TimelineViewModel.class);

        binding = FragmentTimelineBinding.inflate(inflater, container, false);

        View root = binding.getRoot();

        ImageButton button = binding.newHabitActionButton;
        ListView listView = binding.timelineListView;
        TextView textView = binding.textNotifications;

        button.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_timeline_to_habit));

        timelineViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        timelineViewModel.getTimelines().observe(getViewLifecycleOwner(), timelines -> {
            // Update the UI with the templates data
            listView.setAdapter(new TimelineArrayAdapter(requireContext(), timelines));

            if (timelines.isEmpty()) {
                listView.setVisibility(View.GONE);
                textView.setVisibility(View.VISIBLE);
            } else {
                listView.setVisibility(View.VISIBLE);
                textView.setVisibility(View.GONE);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}