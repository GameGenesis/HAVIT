package com.havit.app;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
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
import com.havit.app.ui.timeline.TimelineArrayAdapter;

import org.imaginativeworld.whynotimagecarousel.ImageCarousel;
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/* Google Firebase related code snippets from
 * https://firebase.google.com/docs/auth/android/firebaseui
 */

public class LoginActivity extends AppCompatActivity {

    private FirebaseUser user;                  // Current Firebase user

    public static String sDefSystemLanguage;    // Stores the system locale

    private ImageCarousel helpContent;          // Image carousel with instructions
    private Button submitButton, loginButton;   // Get started and Login Buttons
    private TextView textView;                  // Title text on the login page



    /* Called when the activity is first created
     * Initializes the layout, sets up the onClickListeners for the buttons, and handles the login process
     * Checks for internet connection and redirects to the error page if there is none
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

    /**
     * Displays instructions for the user when the app is launched for the first time
     *
     * @param isVisible A boolean value indicating whether the instructions should be visible or not
     */

    private void displayInstructions(boolean isVisible) {
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


    /**
     * Initializes the instructions carousel with images of instructions and sets the properties of the carousel
     */

    private void setUpInstructions() {
        List<CarouselItem> instructionsCarousel = new ArrayList<>();
        int[] instructionImgs = {R.drawable.instructions1, R.drawable.instructions2, R.drawable.instructions3, R.drawable.instructions4, R.drawable.instructions5, R.drawable.instructions6, R.drawable.instructions7};

        helpContent.registerLifecycle(getLifecycle());
        helpContent.setAutoPlay(true);
        helpContent.setAutoPlayDelay(3500);
        helpContent.setTouchToPause(true);
        helpContent.setInfiniteCarousel(false);

        for (int addItem : instructionImgs) {
            instructionsCarousel.add(new CarouselItem(addItem));
        }

        helpContent.addData(instructionsCarousel);
    }

    /**
     * Configures the welcome text that is displayed to the user
     * Checks if the user is logged in or not, and displays appropriate text accordingly.
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

    /**
     * Called when the activity is no longer visible to the user
     * Hides the support action bar to provide a full screen experience
     */

    @Override
    public void onStop() {
        super.onStop();
        Objects.requireNonNull(getSupportActionBar()).show();
    }

    /**
     * Creates the sign-in intent for the user to sign in
     * Sets up the available providers for the user to choose from: email, phone, and google
     * Sets up the theme and logo for the sign-in screen
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

    /**
     * Creates an email link to be sent to the user for sign in.
     * Sets the settings for android package name, whether to install the app if not available, and the minimum version for the app
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

    /**
     * Catches the email link when the user clicks on the link sent to their email
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

    /**
     * Sign out the current user using Firebase UI
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

    /**
     * Delete the current user using Firebase UI
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

    /**
     * For launching the sign in activity with FirebaseAuthUIActivityResultContract and handle the result with a callback method
     */

    // See: https://developer.android.com/training/basics/intents/result
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    /**
     * Handles the result of the sign in process
     * Receives a FirebaseAuthUIAuthenticationResult and checks the result code
     *
     * @param result contains credentials data returned from the FirebaseAuthUI
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

    public static void sendUserTokenToServer() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        assert user != null;
        user.getIdToken(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String accessToken = task.getResult().getToken();

                        // Send the access token to the Next.js API function
                        OkHttpClient client = new OkHttpClient();

                        assert accessToken != null;
                        RequestBody body = new FormBody.Builder()
                                .add("firebase_token", accessToken)
                                .build();
                        Request request = new Request.Builder()
                                .url("https://havit.space/api/firebase-auth")
                                .post(body)
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (response.isSuccessful()) {

                                }
                            }
                        });
                    } else {
                        // Handle error
                    }
                });
    }
}
