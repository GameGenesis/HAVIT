package com.havit.app.ui.store;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.havit.app.R;
import com.havit.app.databinding.FragmentStoreBinding;

import java.util.ArrayList;
import java.util.Objects;

public class StoreFragment extends Fragment {

    private FragmentStoreBinding binding;

    public static ArrayList<String> templateNames;

    /**
     * Creates the view for the store fragment and updates the list view with the stored templates.
     * Also, sets up back button navigation
     *
     * @param inflater The LayoutInflater object that is used to inflate views in the fragment
     * @param container the parent view that the fragment's UI is attached to
     * @param savedInstanceState this fragment gets re-constructed from a previous saved state as given by this parameter
     * @return the View for the fragment's UI, or null
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        templateNames = new ArrayList<>();

        // Hide the action bar...
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).show();

        StoreViewModel storeViewModel =
                new ViewModelProvider(this).get(StoreViewModel.class);

        binding = FragmentStoreBinding.inflate(inflater, container, false);

        View root = binding.getRoot();

        storeViewModel.getTemplates().observe(getViewLifecycleOwner(), templates -> {
            // Update the UI with the templates data
            ListView templateListView = binding.templateListView;
            templateListView.setAdapter(new TemplateArrayAdapter(requireContext(), templates));
        });

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
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.action_store_to_habit);
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        return root;
    }

    /**
     * Sets up the enter and exit transitions. Called after onAttach and before onCreateView
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
     * Called when the fragment's view is being destroyed.
     * Sets the binding variable to null to avoid memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}