package com.havit.app.ui.camera;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;

import android.net.Uri;
import android.provider.MediaStore;

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

    public void addImageToDatabase(FirebaseUser user, Bitmap bitmap, FragmentActivity activity, String selectedItem) {
        String filePath = "users/" + user.getEmail() + "/" + MainActivity.applyFileNamingScheme(selectedItem) + "/img-" + System.currentTimeMillis();
        MainActivity.saveImageToDatabase(bitmap, activity, filePath);
    }
}