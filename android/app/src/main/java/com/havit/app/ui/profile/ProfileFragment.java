package com.havit.app.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.havit.app.LoginActivity;
import com.havit.app.MainActivity;
import com.havit.app.R;
import com.havit.app.databinding.FragmentProfileBinding;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;

    private FragmentProfileBinding binding;

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher;

    private FirebaseUser user;

    private TextView userFullName;
    private EditText editUsernameField;
    private TextView updateUsernameText;

    public View onCreateView(@NonNull LayoutInflater inflater,
        ViewGroup container, Bundle savedInstanceState) {
        // Hide the action bar...
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).hide();

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        user = FirebaseAuth.getInstance().getCurrentUser();

        userFullName = binding.userFullName;
        userFullName.setVisibility(View.VISIBLE);

        editUsernameField = binding.editUsernameField;
        editUsernameField.setVisibility(View.GONE);

        editUsernameField.setOnKeyListener((v, keyCode, event) -> {
            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Perform action on key press
                updateUsername(requireView());
                return true;
            }
            return false;
        });

        configureUserProfileText();

        MaterialCardView logoutButton = binding.logoutButton;

        logoutButton.setOnClickListener(v -> {
            Intent i = new Intent(requireActivity(), LoginActivity.class);
            i.putExtra("isSignOut", true);
            startActivity(i);
        });

        MaterialCardView resetPasswordButton = binding.resetPasswordButton;
        resetPasswordButton.setOnClickListener(this::resetPassword);

        MaterialCardView updateProfileButton = binding.updateProfileButton;
        updateProfileButton.setOnClickListener(this::updateProfile);

        MaterialCardView updateUsernameButton = binding.updateUsernameButton;
        updateUsernameButton.setOnClickListener(this::updateUsername);

        updateUsernameText = binding.updateUsernameText;

        setUpProfilePicture();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setUpProfilePicture() {
        CircleImageView profileImage = binding.profileImage;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        assert user != null;
        final String profilePictureFilepath = "users/" + user.getEmail() + "/profile-picture";

        galleryActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        Uri imageUri = data.getData();
                        profileImage.setImageURI(imageUri);

                        try {
                            InputStream inputStream = requireActivity().getContentResolver().openInputStream(imageUri);
                            profileViewModel.profilePictureBitmap = BitmapFactory.decodeStream(inputStream);
                            inputStream.close();

                            MainActivity.saveImageToDatabase(profileViewModel.profilePictureBitmap, requireActivity(), profilePictureFilepath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

        if (profileViewModel.profilePictureBitmap != null) {
            profileImage.setImageBitmap(profileViewModel.profilePictureBitmap);
            return;
        }

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(profilePictureFilepath);

        // Download the image file
        final long ONE_MEGABYTE = 1024 * 1024;
        storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            // Data for "images/image.jpg" is returned, use this as needed
            profileViewModel.profilePictureBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            profileImage.setImageBitmap(profileViewModel.profilePictureBitmap);
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Log.d("profile", exception.getMessage());
        });
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

    private void updateProfile(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryActivityResultLauncher.launch(intent);
    }

    private void updateUsername(View view) {
        if (userFullName.getVisibility() == View.VISIBLE) {
            userFullName.setVisibility(View.GONE);
            editUsernameField.setVisibility(View.VISIBLE);
            editUsernameField.requestFocus();
            updateUsernameText.setText("Confirm Change");
            configureUserProfileText();
        }
        else {
            userFullName.setVisibility(View.VISIBLE);
            editUsernameField.setVisibility(View.GONE);
            updateUsernameText.setText("Change Username");

            String newUsername = editUsernameField.getText().toString();

            editUsernameField.clearFocus();

            if (newUsername.isEmpty())
                return;

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newUsername)
                    /*.setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))*/
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            configureUserProfileText();
                            Log.d("Profile", "User profile updated.");
                        }
                    });
        }
    }

    private void configureUserProfileText() {
        TextView userId = binding.userId;

        if (user == null) {
            userFullName.setText(R.string.get_started_text);
            editUsernameField.setText(R.string.get_started_text);
        } else {
            String username = Objects.requireNonNull(user.getDisplayName());
            userFullName.setText(username.toUpperCase(Locale.ROOT));
            editUsernameField.setText(username);
            userId.setText(Objects.requireNonNull(user.getEmail()));
        }
    }
}