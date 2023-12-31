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

import com.havit.app.R;
import com.havit.app.databinding.FragmentHabitBinding;

import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Objects;

public class HabitFragment extends Fragment {

    private FragmentHabitBinding binding;

    public static String name, time;
    public static ArrayList<String> daysPicked;

    /**
     * Sets the enter and exit transition for the fragment using the fade transition
     *
     * @param savedInstanceState a bundle of the saved state of the fragment
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TransitionInflater inflater = TransitionInflater.from(requireContext());

        setEnterTransition(inflater.inflateTransition(R.transition.fade));
        setExitTransition(inflater.inflateTransition(R.transition.fade));
    }

    /**
     * Sets up the Fragment View
     * Adds the onClickListener to the create button and implements back button menu navigation
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return the view for the fragment's UI
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Hide the action bar...
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).show();

        daysPicked = new ArrayList<>();

        binding = FragmentHabitBinding.inflate(inflater, container, false);

        Button createButton = binding.createButton;

        createButton.setOnClickListener(this::createNewHabit);

        View root = binding.getRoot();

        // Menu navigation: https://developer.android.com/jetpack/androidx/releases/activity#1.4.0-alpha01
        // The usage of an interface lets you inject your own implementation
        MenuHost menuHost = requireActivity();

        // [From the android studio changelog]
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

    /**
     * Called when the fragment's view is being destroyed.
     * Sets the binding variable to null to avoid memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Called when the Create Button is pressed.
     * Retrieves the name and days picked from the UI and
     * checks if all required fields are filled. If so, it navigates to the timeline fragment.
     * If not, it displays a popup toast message to inform the user that required fields are empty.
     *
     * @param view The current view
     */
    private void createNewHabit(View view) {
        // Name and Description fields
        name = Objects.requireNonNull(binding.nameFieldEdit.getText()).toString();

        // Array of what days were picked. Starts at Sunday (index 0)
        boolean[] isPickedArray = new boolean[7];

        LinearLayout dayPickerLayout = binding.dayPickerLayout;

        // Whether at least one day was chosen
        boolean anyDaysPicked = false;

        for (int i = 0; i < dayPickerLayout.getChildCount(); i++) {
            Days day = Days.fromValue(i);
            ToggleButton toggle = (ToggleButton) dayPickerLayout.getChildAt(i);
            isPickedArray[i] = toggle.isChecked();

            if (toggle.isChecked()) {
                anyDaysPicked = true;
                daysPicked.add(day.getName());
            }
        }

        // Gets the hour and minute values from the time picker
        TimePicker timePicker = binding.timePicker;

        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        time = hour + ":" + minute;

        // Checks whether all of the required fields have been filled
        if (!name.isEmpty() && anyDaysPicked) {
            // Navigated to the timeline fragment
            Navigation.findNavController(view).navigate(R.id.action_habit_to_store);
        } else {
            Toast.makeText(requireActivity(), "Required Fields are Empty", Toast.LENGTH_LONG).show();
        }
    }
}