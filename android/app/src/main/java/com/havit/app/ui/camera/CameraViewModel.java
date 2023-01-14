package com.havit.app.ui.camera;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;

import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.havit.app.MainActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class CameraViewModel extends ViewModel {

    // MutableLiveData support for the timeline spinner dropdown in the Camera View
    private final MutableLiveData<ArrayList<String>> timelineItems = new MutableLiveData<>();

    /**
     * Setter for timelineItems LiveData
     *
     * @param items the ArrayList of items to set as the value of timelineItems
     */
    public void setTimelineItems(ArrayList<String> items) {
        this.timelineItems.setValue(items);
    }

    /**
     * Getter for timelineItems LiveData
     *
     * @return the timelineItems LiveData
     */
    public LiveData<ArrayList<String>> getTimelineItems() {
        return timelineItems;
    }

    /**
     * Saves the bitmap to the device's gallery (currently not being used)

     * @param context the context of the app
     * @param bitmap the Bitmap to save to the gallery
     */
    public void saveImageToGallery(Context context, Bitmap bitmap) {
        // Save the image to the MediaStore
        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DISPLAY_NAME, "img-" + System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

        Uri imageUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try {
            OutputStream out = context.getContentResolver().openOutputStream(imageUri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
        } catch (IOException e) {
            Toast.makeText(context, "Failed to save image to gallery", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    /**
     * Saves the bitmap image to the Firebase Storage under the current user's email and the selected timeline
     *
     * @param user the current Firebase user
     * @param bitmap the Bitmap to add to the database
     * @param activity the current activity
     * @param selectedItem the selected timeline item name
     */
    public void addImageToDatabase(FirebaseUser user, Bitmap bitmap, FragmentActivity activity, String selectedItem) {
        String filePath = "users/" + user.getEmail() + "/" + MainActivity.applyFileNamingScheme(selectedItem) + "/img-" + System.currentTimeMillis();
        MainActivity.saveImageToDatabase(bitmap, activity, filePath);
    }
}