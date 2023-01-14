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

import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

import org.imaginativeworld.whynotimagecarousel.ImageCarousel;
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Provides the user with a profile view.
 * Includes the app instructions, user's name, profile picture, and the options to update their username, reset their password, and log out of their account.
 */
public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;  // instance of the ProfileViewModel class

    private FragmentProfileBinding binding;     // instance of the FragmentProfileBinding class

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher;   // instance of the ActivityResultLauncher class

    private FirebaseUser user;              // instance of the FirebaseUser class, which represents the currently signed-in user
    private TextView userFullName;          // displays the user's full name
    private EditText editUsernameField;     // allows the user to edit their username
    private TextView updateUsernameText;    // displays the update username text
    private ImageCarousel helpContent;      // displays the instruction images


    /**
     * Sets up the Fragment View
     *
     * @param inflater           a LayoutInflater that inflates the layout for the fragment's UI
     * @param container          a ViewGroup that is the parent of the fragment's UI
     * @param savedInstanceState a Bundle that saves the state of the fragment
     * @return root, a View that is the root of the fragment's UI
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
        ViewGroup container, Bundle savedInstanceState) {

        // Hide the action bar
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).hide();

        // Initialize profileViewModel
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Inflate binding
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set user variable as currently signed in user
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize the variables and bind with layout
        userFullName = binding.userFullName;
        editUsernameField = binding.editUsernameField;
        updateUsernameText = binding.updateUsernameText;
        helpContent = binding.helpContent;

        // Set visibility for text field and edit text
        userFullName.setVisibility(View.VISIBLE);
        editUsernameField.setVisibility(View.GONE);

        setEnterButtonListener();
        configureUserProfileText();
        setProfileButtonsListner();
        setUpProfilePicture();
        toggleHelpContent();
        setHelpContent();

        return root;
    }


    /**
     * Sets the binding variable to null
     * Called when the fragment's view is destroyed
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    /**
     * Sets OnClickListeners for logout, reset password, update profile and update username buttons
     */
    private void setProfileButtonsListner(){
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
    }


    /**
     * Sets an OnKeyListener for the editUsernameField
     * Listens for the enter key and calls the updateUsername() method when pressed
     */
    private void setEnterButtonListener(){
        editUsernameField.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                updateUsername(requireView());
                return true;
            }
            return false;
        });
    }


    /**
     * Sets up the profile picture for the user by allowing them to select an image from the gallery
     * Uploads the image to Firebase Storage and retrieves the profile picture from Firebase Storage if it exists.
     */
    private void setUpProfilePicture() {
        // Circle view for the profile img
        CircleImageView profileImage = binding.profileImage;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Set path for the profile picture for firebase storage
        assert user != null;
        final String profilePictureFilepath = "users/" + user.getEmail() + "/profile-picture";

        // Set listener for gallery activity
        galleryActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        // getting the data from the result and the image uri
                        Intent data = result.getData();
                        Uri imageUri = data.getData();
                        profileImage.setImageURI(imageUri);

                        // setting the selected image as the profile picture
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

        // Storage reference for the firebase storage
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


    /**
     * Sends a password reset email to the currently logged in user's email address.
     * @param view The view that triggers the function call.
     */
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


    /**
     * Updates the user's profile picture by allowing them to choose an image from their gallery
     * @param view The view that triggers the function call.
     */
    private void updateProfile(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryActivityResultLauncher.launch(intent);
    }


    /**
     * Updates the user's name by allowing them to enter their desired name on editText view
     * @param view The view that triggers the function call.
     */
    private void updateUsername(View view) {
        // If the view was showing the user's name, change it to the editText to allow them to type their desired name
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

            // Error handling for empty strings
            if (newUsername.isEmpty())
                return;

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newUsername)
                    /*.setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))*/
                    .build();]

            // Update their information on firebase storage
            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            configureUserProfileText();
                            Log.d("Profile", "User profile updated.");
                        }
                    });
        }
    }


    /**
     * Configures the user's name by default when first launched
     * Retrieves name and user's email address
     */
    private void configureUserProfileText() {
        TextView userId = binding.userId;

        // Set their default name and email values
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

    /**
     * Toggles the help content view on the profile screen when help button clicked
     */
    private void toggleHelpContent(){
        RelativeLayout profileView = binding.profileView;           // profile ui wrapper
        LinearLayout firstRowButtons = binding.firstRowButtons;     // logout and reset password buttons
        LinearLayout secondRowButtons = binding.secondRowButtons;   // change profile picture and name buttons
        ImageButton helpButton = binding.helpButton;                // help button


        // Initialize the help content (instructions) and help button visibility
        helpContent.setVisibility(View.GONE);
        helpButton.setImageResource(R.drawable.ic_baseline_help_24);

        // Set onclick listenr for the help button
        helpButton.setOnClickListener(v -> {
            // If the help button is clicked
            if (helpContent.getVisibility() == View.GONE){
                // Set visibility for contents on the profile page
                helpContent.setVisibility(View.VISIBLE);
                profileView.setVisibility(View.GONE);
                firstRowButtons.setVisibility(View.GONE);
                secondRowButtons.setVisibility(View.GONE);

                // Change help button to close button
                helpButton.setImageResource(R.drawable.ic_baseline_close_24);
            // If the close button i sclicked
            } else if (helpContent.getVisibility() == View.VISIBLE){
                // Set visibility for contents on the profile page
                helpContent.setVisibility(View.GONE);
                profileView.setVisibility(View.VISIBLE);
                firstRowButtons.setVisibility(View.VISIBLE);
                secondRowButtons.setVisibility(View.VISIBLE);

                // Change close button to help button
                helpButton.setImageResource(R.drawable.ic_baseline_help_24);
            }
        });
    }


    /**
     * Sets the instructions content for the help view carousel
     */
    private void setHelpContent(){
        // list of carousel views
        List<CarouselItem> instructionsCarousel = new ArrayList<>();
        // list of instruction drawables
        int[] instructionImgs = {R.drawable.instructions1, R.drawable.instructions2, R.drawable.instructions3, R.drawable.instructions4, R.drawable.instructions5, R.drawable.instructions6, R.drawable.instructions7};

        // Layout and visual attributes for the help content carousel view
        helpContent.registerLifecycle(getLifecycle());
        helpContent.setAutoPlay(true);
        helpContent.setAutoPlayDelay(3500);
        helpContent.setTouchToPause(true);
        helpContent.setInfiniteCarousel(true);

        // add all the instruction drawables to the list of carousel views
        for (int addItem : instructionImgs){
            instructionsCarousel.add(new CarouselItem(addItem));
        }

        // set the carousel view
        helpContent.addData(instructionsCarousel);
    }
}