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
import com.google.firebase.storage.StorageReference;
import com.havit.app.MainActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class CameraViewModel extends ViewModel {

    // MutableLiveData support for the spinner...
    private final MutableLiveData<ArrayList<String>> timelineItems = new MutableLiveData<>();

    public void setTimelineItems(ArrayList<String> items) {
        this.timelineItems.setValue(items);
    }

    public LiveData<ArrayList<String>> getTimelineItems() {
        return timelineItems;
    }

    public void saveImageToGallery(Context context, Bitmap bitmap) throws IOException {
        // Save the image to the MediaStore
        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DISPLAY_NAME, "img-" + System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

        Uri imageUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        OutputStream out = context.getContentResolver().openOutputStream(imageUri);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.close();
    }

    // TODO: save the image to the storage and link it to the json in db
    public void addImageToDatabase(FirebaseUser user, Bitmap bitmap, FragmentActivity activity, String selectedItem) {
        // Run a new thread for an asynchronous operation...
        new Thread() {
            public void run() {
                // To save the image...
                try {
                    // Convert the bitmap to a byte array...
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream.toByteArray();

                    // users/<selected_user_email>/<selected_template_name>/img-<date_taken>.jpg
                    StorageReference image = MainActivity.storageReference.child("users/" + user.getEmail() + "/" + MainActivity.applyFileNamingScheme(selectedItem) + "/img-" + System.currentTimeMillis());
                    image.putBytes(byteArray).addOnSuccessListener(taskSnapshot -> {
                        // Run the code below on the main thread that handles the UI events...
                        activity.runOnUiThread(() -> Toast.makeText(activity, "Photo was successfully uploaded", Toast.LENGTH_LONG).show());
                    }).addOnFailureListener(e -> {
                        // Run the code below on the main thread that handles the UI events...
                        activity.runOnUiThread(() -> Toast.makeText(activity, "An error occurred while uploading the photo", Toast.LENGTH_LONG).show());
                    });
                    //// viewModel.saveImageToGallery(requireActivity(), viewModel.getCapturedBitmap());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}