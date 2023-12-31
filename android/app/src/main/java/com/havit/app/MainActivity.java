package com.havit.app;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.util.Log;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;

import androidx.navigation.ui.NavigationUI;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.havit.app.databinding.ActivityMainBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public static final int MY_CAMERA_REQUEST_CODE = 100;   // request code for camera intent

    public static StorageReference storageReference;        // reference to the Firebase storage

    public static int colorAccent;                          // accent color value

    /**
     * Called when the Activity is first created
     * Sets the bottom navigation and setup action bar with navigation controller
     * @param savedInstanceState Bundle containing the state of the fragment if it was previously created
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        colorAccent = getResources().getColor(com.firebase.ui.auth.R.color.colorAccent, getTheme());

        if (MainActivity.isNotConnected(this)) {
            Intent i = new Intent(getApplicationContext(), ErrorActivity.class);
            startActivity(i);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return;
        }

//        try {
//            // Sends the account credential token to the Flask_RESTful server...
//            LoginActivity.sendAccountTokenToServer();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        storageReference = FirebaseStorage.getInstance().getReference();

        LoginActivity.sDefSystemLanguage = Locale.getDefault().getLanguage();

        Objects.requireNonNull(getSupportActionBar()).show();

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }

        // Locks the orientation to vertical...
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = binding.navView;

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_camera, R.id.navigation_profile, R.id.navigation_timeline)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    /*
     * Asks the user for permission to use the camera
     * @param requestCode The request code passed in requestPermissions(android.app.Activity, String[], int)
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Toast.makeText(this, "Camera permission granted!", Toast.LENGTH_LONG).show();
            } else {
                // Toast.makeText(this, "Camera permission denied!", Toast.LENGTH_LONG).show();
            }
        }
    }

    /*
     * Hides the keyboard when the user touches outside of the EditText
     * @param ev The motion event
     * @return True if the event was handled, false otherwise
     */

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }


    /**
     * Checks the internet connection of the device
     * Uses ConnectivityManager to get the active network and checks if it is null or not connected
     *
     * @param Activity activity The activity for which the internet connection is being checked
     * @return boolean Return true if the device is not connected to the internet, false otherwise
     */
    public static boolean isNotConnected(Activity activity) {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork == null || !activeNetwork.isConnectedOrConnecting();
    }

    /*
     * Applies the file naming scheme to the selected item
     * Consists of replacing spaces with hyphens and converting the string to lowercase
     * 
     * @param selectedItem The selected item
     * @return The selected item with the file naming scheme applied
     */

    public static String applyFileNamingScheme(String selectedItem) {
        return selectedItem.replace(" ","-").toLowerCase(Locale.ROOT);
    }

    /*
     * Decodes the file naming scheme to the selected item
     * Consists of replacing hyphens with spaces and converting the string to title case
     * Essentially the opposite of applyFileNamingScheme(String)
     * 
     * @param fileName The file name
     * @return The file name with the file naming scheme decoded
     */

    public static String decodeFileNamingScheme(String fileName) {
        return toTitleCase(fileName.replace("-", " "));
    }


    /**
     * Converts the given phrase to title case
     *
     * @param String phrase The phrase that needs to be converted to title case
     * @return String the phrase in title case.
     */
    public static String toTitleCase(String phrase) {
        // convert the string to an array
        char[] phraseChars = phrase.toCharArray();

        for (int i = 0; i < phraseChars.length - 1; i++) {
            if(phraseChars[i] == ' ') {
                phraseChars[i+1] = Character.toUpperCase(phraseChars[i+1]);
            }
        }

        // convert the array to string
        return String.valueOf(phraseChars);
    }


    /**
     * Converts the given string array to milliseconds
     * Converts the first element to minutes, second element to seconds and the third element to hours and then adds them all up to get the total time in milliseconds
     *
     * @param String[] startTimeArray the array that needs to be converted to milliseconds
     * @return long the total time in milliseconds
     */
    public static long parseStringToMillis(String[] startTimeArray) {
        long startMinute = Integer.parseInt(startTimeArray[0]) * 60;
        long startSeconds = Integer.parseInt(startTimeArray[1]);
        long startMillis = Integer.parseInt(startTimeArray[2]) * 100;

        return (startMinute + startSeconds) * 1000 + startMillis;
    }


    /**
     * Convert the given time in milliseconds to a string format
     * Takes the time in milliseconds and converts it to minutes, seconds and milliseconds respectively, and returns them in the format "minutes:seconds:milliseconds"
     *
     * @param int millis the time in milliseconds
     * @return String the time in the format "minutes:seconds:milliseconds"
     */
    public static String parseMillisToString(int millis) {
        int startMinute = Math.floorDiv(millis, (60 * 1000));
        int startSeconds = Math.floorDiv(millis % (60 * 1000), 1000);
        int startMillis = millis % (60 * 1000) % 1000;

        return startMinute + ":" + startSeconds + ":" + startMillis;
    }


    /**
     * Saves the image to the Firebase Storage asynchronously to prevent the app from freezing
     * Takes a bitmap, the current activity, and a file path to convert the bitmap and upload it to the firebase storage
     *
     * @param Bitmap bitmap The image that needs to be saved
     * @param FragmentActivity activity The current activity
     * @param String filePath The file path where the image needs to be saved
     */
    public static void saveImageToDatabase(Bitmap bitmap, FragmentActivity activity, String filePath) {
        // Run a new thread for an asynchronous operation, separate from the main thread...
        new Thread() {
            public void run() {
                // To save the image...
                try {
                    // Convert the bitmap to a byte array...
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream.toByteArray();

                    StorageReference image = MainActivity.storageReference.child(filePath);
                    image.putBytes(byteArray).addOnSuccessListener(taskSnapshot -> {
                        // Run the code below on the main thread that handles the UI events...
                        activity.runOnUiThread(() -> Toast.makeText(activity, "Photo was successfully uploaded", Toast.LENGTH_LONG).show());
                    }).addOnFailureListener(e -> {
                        // Run the code below on the main thread that handles the UI events...
                        activity.runOnUiThread(() -> Toast.makeText(activity, "An error occurred while uploading the photo", Toast.LENGTH_LONG).show());
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    /**
     * Crops an image into a circle shape
     *
     * @param Bitmap bitmap The image that needs to be cropped
     * @return Bitmap the image in the form of a circle
     */
    public static Bitmap cropImage(Bitmap bitmap){
        Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(circleBitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        float centerX = (float) bitmap.getWidth() / 2;
        float centerY = (float) bitmap.getHeight() / 2;

        canvas.drawCircle(centerX, centerY, Math.min(centerX, centerY), paint);

        return circleBitmap;
    }

    /*
     * Interface for the updateFirestoreDatabase method
     * It is used to invoke the method in the MainActivity class
     */

    @FunctionalInterface
    public interface OnTaskSuccessful {
        void invoke(DocumentReference documentReference, DocumentSnapshot documentSnapshot);
    }

    /*
     * Updates the user's data in the Firestore database
     * 
     * @param user The user
     * @param onTaskSuccessful The interface
     */

    public static void updateFirestoreDatabase(FirebaseUser user, OnTaskSuccessful onTaskSuccessful) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection("users").document(Objects.requireNonNull(user.getEmail()));

        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                onTaskSuccessful.invoke(documentReference, documentSnapshot);

            } else {
                Log.d(TAG, "Failed to get document", task.getException());
            }
        });
    }
}