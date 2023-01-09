package com.havit.app.ui.edit;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.havit.app.R;
import com.havit.app.databinding.FragmentEditBinding;

import java.util.Objects;

public class EditFragment extends Fragment {

    private FragmentEditBinding binding;
    private EditViewModel timelineViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        // Hide the action bar...
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).show();

        timelineViewModel = new ViewModelProvider(this).get(EditViewModel.class);

        binding = FragmentEditBinding.inflate(inflater, container, false);

        View root = binding.getRoot();

        final TextView nameTextView = binding.nameText;
        final TextView templateNameTextView = binding.templateNameText;

        timelineViewModel.getName().observe(getViewLifecycleOwner(), nameTextView::setText);
        timelineViewModel.getTemplateName().observe(getViewLifecycleOwner(), templateNameTextView::setText);

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