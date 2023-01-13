package com.havit.app.ui.edit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.havit.app.databinding.PopupLayoutBinding;

public class PopupDialog extends DialogFragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PopupLayoutBinding binding = PopupLayoutBinding.inflate(inflater, container, false);

        final Button dismissButton = binding.btnDismiss;
        dismissButton.setOnClickListener(v -> {
            this.dismiss();
        });

        return binding.getRoot();
    }
}