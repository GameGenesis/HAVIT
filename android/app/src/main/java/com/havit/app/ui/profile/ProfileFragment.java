package com.havit.app.ui.profile;

import android.content.Intent;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.havit.app.LoginActivity;
import com.havit.app.R;
import com.havit.app.databinding.FragmentProfileBinding;

import java.util.Locale;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
        ViewGroup container, Bundle savedInstanceState) {
            // Hide the action bar...
            Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).hide();

            ProfileViewModel profileViewModel =
                    new ViewModelProvider(this).get(ProfileViewModel.class);

            binding = FragmentProfileBinding.inflate(inflater, container, false);
            View root = binding.getRoot();

            configureUserProfileText();

            MaterialCardView logoutButton = binding.logoutButton;
            MaterialCardView resetPasswordButton = binding.resetPasswordButton;

            logoutButton.setOnClickListener(v -> {
                Intent i = new Intent(requireActivity(), LoginActivity.class);
                i.putExtra("isSignOut", true);
                startActivity(i);
            });

            resetPasswordButton.setOnClickListener(this::resetPassword);

            return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void resetPassword(View view) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String emailAddress = Objects.requireNonNull(auth.getCurrentUser()).getEmail();

        auth.useAppLanguage();

        assert emailAddress != null;

        auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Password reset email sent
                        Toast.makeText(requireActivity(), "Password reset email sent to " + emailAddress, Toast.LENGTH_SHORT).show();
                    } else {
                        // Error occurred
                        Toast.makeText(requireActivity(), "Error sending password reset email", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void configureUserProfileText() {
        FirebaseUser user;
        user = FirebaseAuth.getInstance().getCurrentUser();

        TextView userFullName = binding.userFullName;
        TextView userId = binding.userId;

        if (user == null) {
            userFullName.setText(R.string.get_started_text);
        } else {
            userFullName.setText(Objects.requireNonNull(user.getDisplayName()).toUpperCase(Locale.ROOT));
            userId.setText(Objects.requireNonNull(user.getEmail()));
        }
    }
}