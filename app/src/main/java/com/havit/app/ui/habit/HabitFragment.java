package com.havit.app.ui.habit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.havit.app.R;
import com.havit.app.databinding.FragmentHabitBinding;

import android.view.MenuItem;

public class HabitFragment extends Fragment {

    private FragmentHabitBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HabitViewModel habitViewModel =
                new ViewModelProvider(this).get(HabitViewModel.class);

        binding = FragmentHabitBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // NOTE: Only works for the back key on the phone, not the back button on the top bar
                Navigation.findNavController(root).navigate(R.id.action_habit_to_timeline);
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_habit_to_timeline);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}