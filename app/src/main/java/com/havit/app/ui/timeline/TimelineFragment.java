package com.havit.app.ui.timeline;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

        public View onCreateView(@NonNull LayoutInflater inflater,
                ViewGroup container, Bundle savedInstanceState) {
            // Hide the action bar...
            Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).hide();

            TimelineViewModel timelineViewModel =
                    new ViewModelProvider(this).get(TimelineViewModel.class);

            binding = FragmentTimelineBinding.inflate(inflater, container, false);

            View root = binding.getRoot();
            ImageButton button = binding.newHabitActionButton;

            button.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_timeline_to_habit));

            final TextView textView = binding.textNotifications;
            timelineViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}