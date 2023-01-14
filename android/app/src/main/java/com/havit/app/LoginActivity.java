package com.havit.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;

import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.havit.app.databinding.ActivityLoginBinding;
import com.havit.app.ui.profile.ProfileFragment;

import org.imaginativeworld.whynotimagecarousel.ImageCarousel;
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/* Google Firebase related code snippets from
 * https://firebase.google.com/docs/auth/android/firebaseui
 */

public class LoginActivity extends AppCompatActivity {

    private FirebaseUser user;

    private TextView textView;

    public static String sDefSystemLanguage;

    private LinearLayout loginContainer;

    private ImageCarousel helpContent;

    private Button submitButton, loginButton;

    /**
     * Called when the fragment is first created
     * @param savedInstanceState Bundle containing the state of the fragment if it was previously created
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get system locale...
        sDefSystemLanguage = Locale.getDefault().getLanguage();

        Objects.requireNonNull(getSupportActionBar()).hide();

        Intent intent = getIntent();

        boolean isSignOut = intent.getBooleanExtra("isSignOut", false);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (MainActivity.isNotConnected(this)) {
            Intent i = new Intent(getApplicationContext(), ErrorActivity.class);
            startActivity(i);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return;
        }

        if (user != null) {
            if (isSignOut) {
                signOut();
                intent.putExtra("isSignOut", false);

            } else {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                return;
            }
        }

        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        submitButton = binding.submitButton;
        loginButton = binding.loginButton;

        loginContainer = binding.loginContainer;
        helpContent = binding.helpContent;

        textView = binding.textView;

        textView.setVisibility(View.VISIBLE);
        submitButton.setVisibility(View.VISIBLE);
        loginButton.setVisibility(View.GONE);
        helpContent.setVisibility(View.GONE);

        submitButton.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                displayInstructions(true);
            } else {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        loginButton.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                createSignInIntent();
            } else {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    /*
     * Display instructions for the user to follow
     * @param isVisible boolean value to determine if the instructions should be displayed or not
     */

    private void displayInstructions(boolean isVisible){
        if (isVisible) {
            textView.setVisibility(View.GONE);
            submitButton.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
            helpContent.setVisibility(View.VISIBLE);

            setUpInstructions();
        } else {
            textView.setVisibility(View.VISIBLE);
            submitButton.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.GONE);
            helpContent.setVisibility(View.GONE);
        }

    }

    /*
     * Sets up the instructions for the user to follow
     */

    private void setUpInstructions(){
        List<CarouselItem> instructionsCarousel = new ArrayList<>();
        int[] instructionImgs = {R.drawable.instructions1, R.drawable.instructions2, R.drawable.instructions3, R.drawable.instructions4, R.drawable.instructions5, R.drawable.instructions6, R.drawable.instructions7};

        helpContent.registerLifecycle(getLifecycle());
        helpContent.setAutoPlay(true);
        helpContent.setAutoPlayDelay(3500);
        helpContent.setTouchToPause(true);
        helpContent.setInfiniteCarousel(false);

        for (int addItem : instructionImgs){
            instructionsCarousel.add(new CarouselItem(addItem));
        }

        helpContent.addData(instructionsCarousel);
    }

    /*
     * Configures the welcome text for the user
     */

    private void configureWelcomeText() {
        if (user == null) {
            textView.setText(R.string.get_started_text);
            textView.setTextSize(48);
        } else {
            loginButton.setVisibility(View.GONE);
            helpContent.setVisibility(View.GONE);
            textView.setText(String.format("YOU'RE BACK,\n%s", Objects.requireNonNull(user.getDisplayName()).toUpperCase(Locale.ROOT)));
            textView.setTextSize(24);
        }
    }

    /*
     * Part of the Android Activity Lifecycle
     * @see <a href="https://developer.android.com/guide/components/activities/activity-lifecycle">Android Activity Lifecycle</a>
     */

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getSupportActionBar()).hide();
    }

    /*
     * Part of the Android Activity Lifecycle
     * Shows the action bar
     * @see <a href="https://developer.android.com/guide/components/activities/activity-lifecycle">Android Activity Lifecycle</a>
     */

    @Override
    public void onStop() {
        super.onStop();
        Objects.requireNonNull(getSupportActionBar()).show();
    }

    /*
     * Creates the sign in intent
     * @see <a href="https://firebase.google.com/docs/auth/android/firebaseui">FirebaseUI</a>
     */

    public void createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        // Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.LoginTheme)
                .setLogo(R.drawable.ic_big_logo)
                .build();
        signInLauncher.launch(signInIntent);
        // [END auth_fui_create_intent]
    }

    /*
     * Creates the sign in intent with email link
     * @see <a href="https://firebase.google.com/docs/auth/android/firebaseui">FirebaseUI</a>
     */

    public void emailLink() {
        // [START auth_fui_email_link]
        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setAndroidPackageName(
                        /* yourPackageName= */ "com.havit.app",
                        /* installIfNotAvailable= */ true,
                        /* minimumVersion= */ null)
                .setHandleCodeInApp(true) // This must be set to true
                .setUrl("https://havit.page.link") // This URL needs to be whitelisted
                .build();

        List<AuthUI.IdpConfig> providers = Collections.singletonList(
                new AuthUI.IdpConfig.EmailBuilder()
                        .enableEmailLinkSignIn()
                        .setActionCodeSettings(actionCodeSettings)
                        .build()
        );
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        signInLauncher.launch(signInIntent);
        // [END auth_fui_email_link]
    }

    /*
     * Catches the email link
     * @see <a href="https://firebase.google.com/docs/auth/android/firebaseui">FirebaseUI</a>
     */

    public void catchEmailLink() {
        List<AuthUI.IdpConfig> providers = Collections.emptyList();

        // [START auth_fui_email_link_catch]
        if (AuthUI.canHandleIntent(getIntent())) {
            if (getIntent().getExtras() == null) {
                return;
            }
            String link = getIntent().getExtras().getString("email_link_sign_in");
            if (link != null) {
                Intent signInIntent = AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setEmailLink(link)
                        .setAvailableProviders(providers)
                        .build();
                signInLauncher.launch(signInIntent);
            }
        }
        // [END auth_fui_email_link_catch]
    }

    /*
     * Signs out the user
     * @see <a href="https://firebase.google.com/docs/auth/android/firebaseui">FirebaseUI</a>
     */

    // Default context would be the "this" keyword...
    public void signOut() {
        // [START auth_fui_signout]
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    // ...
                });
        // [END auth_fui_signout]
    }

    /*
     * Deletes the user profile
     * @see <a href="https://firebase.google.com/docs/auth/android/firebaseui">FirebaseUI</a>
     */

    public void delete() {
        // [START auth_fui_delete]
        AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener(task -> {
                    // ...
                });
        // [END auth_fui_delete]
    }

    /*
     * Displays the FireAuthUI sign in intent
     * @see <a href="https://firebase.google.com/docs/auth/android/firebaseui">FirebaseUI</a>
     */

    // See: https://developer.android.com/training/basics/intents/result
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    /*
     * Defines the result of the FireAuthUI sign in intent
     * @see <a href="https://firebase.google.com/docs/auth/android/firebaseui">FirebaseUI</a>
     */

    // [START auth_fui_result]
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            displayInstructions(false);

            // Successfully signed in
            user = FirebaseAuth.getInstance().getCurrentUser();
            configureWelcomeText();
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
        }
    }
    // [END auth_fui_result]

}
