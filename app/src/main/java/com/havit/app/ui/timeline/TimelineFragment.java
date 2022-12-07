package com.havit.app.ui.timeline;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.havit.app.databinding.FragmentTimelineBinding;

public class TimelineFragment extends Fragment {

    private FragmentTimelineBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        com.havit.app.ui.timeline.TimelineViewModel notificationsViewModel =
                new ViewModelProvider(this).get(com.havit.app.ui.timeline.TimelineViewModel.class);

        binding = FragmentTimelineBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textNotifications;
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}