package com.havit.app.ui.habit;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.havit.app.R;
import com.havit.app.databinding.FragmentHabitBinding;

import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HabitFragment extends Fragment {

    private FragmentHabitBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Hide the action bar...
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).show();

        HabitViewModel habitViewModel =
                new ViewModelProvider(this).get(HabitViewModel.class);

        binding = FragmentHabitBinding.inflate(inflater, container, false);

        Button createButton = binding.createButton;

        createButton.setOnClickListener(v -> createNewHabit(v));

        View root = binding.getRoot();

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
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.action_habit_to_timeline);
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        return root;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(requireContext());

        setEnterTransition(inflater.inflateTransition(R.transition.fade));
        setExitTransition(inflater.inflateTransition(R.transition.fade));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Called when the Create Button is pressed
    private void createNewHabit(View view) {
        // Name and Description fields
        String name = binding.nameFieldEdit.getText().toString();
        String description = binding.descriptionFieldEdit.getText().toString();

        // Array of what days were picked. Starts at Sunday (index 0)
        boolean[] daysPicked = new boolean[7];
        LinearLayout dayPickerLayout = binding.dayPickerLayout;

        // Whether at least one day was chosen
        boolean anyDaysPicked = false;

        for (int i = 0; i < dayPickerLayout.getChildCount(); i++) {
            ToggleButton toggle = (ToggleButton) dayPickerLayout.getChildAt(i);
            daysPicked[i] = toggle.isChecked();

            if (toggle.isChecked()) {
                anyDaysPicked = true;
            }
        }

        // Gets the hour and minute values from the time picker
        TimePicker timePicker = binding.timePicker;
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        // Checks whether all of the required fields have been filled
        if (!name.isEmpty() && !description.isEmpty() && anyDaysPicked) {
            // Navigated to the timeline fragment
            Navigation.findNavController(view).navigate(R.id.action_habit_to_timeline);
            Toast.makeText(requireActivity(), "Successfully Created Timeline", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(requireActivity(), "Required Fields are Empty", Toast.LENGTH_LONG).show();
        }
    }
}