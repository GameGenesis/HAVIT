package com.havit.app.ui.new_habit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.havit.app.databinding.FragmentNewHabitBinding;

public class NewHabitFragment extends Fragment {

    private FragmentNewHabitBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NewHabitViewModel newHabitViewModel =
                new ViewModelProvider(this).get(NewHabitViewModel.class);

        binding = FragmentNewHabitBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}