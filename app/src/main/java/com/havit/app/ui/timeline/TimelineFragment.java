package com.havit.app.ui.timeline;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.havit.app.R;
import com.havit.app.databinding.FragmentTimelineBinding;
import com.havit.app.ui.new_habit.NewHabitFragment;

public class TimelineFragment extends Fragment {

    private FragmentTimelineBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        com.havit.app.ui.timeline.TimelineViewModel notificationsViewModel =
                new ViewModelProvider(this).get(com.havit.app.ui.timeline.TimelineViewModel.class);

        binding = FragmentTimelineBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Button button = root.findViewById(R.id.imageButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();;
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.navigation_timeline, new NewHabitFragment());
                fragmentTransaction.addToBackStack(getParentFragment().toString());
                fragmentTransaction.commit();
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