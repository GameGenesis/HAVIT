package com.havit.app.ui.habit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.havit.app.databinding.FragmentHabitBinding;

public class HabitFragment extends Fragment {

    private FragmentHabitBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HabitViewModel habitViewModel =
                new ViewModelProvider(this).get(HabitViewModel.class);

        binding = FragmentHabitBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}