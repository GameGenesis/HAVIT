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

    // MutableLiveData support for Havit Spinner...
    private MutableLiveData<ArrayList<String>> items = new MutableLiveData<>();

    public void setItems(ArrayList<String> items) {
        this.items.setValue(items);
    }

    public LiveData<ArrayList<String>> getItems() {
        return items;
    }

    public void saveImageToGallery(Context context, Bitmap bitmap) throws IOException {
        // Save the image to the MediaStore
        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DISPLAY_NAME, "Image-" + System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

        Uri imageUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        OutputStream out = context.getContentResolver().openOutputStream(imageUri);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.close();
    }

    // TODO: save the image to the storage and link it to the json in db
    public void addImageToDatabase(FirebaseUser user, Bitmap bitmap, FragmentActivity activity) {

        // To save the image...
        try {
            // Convert the bitmap to a byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            StorageReference image = MainActivity.storageReference.child("users/" + user.getEmail() + "/img-" + System.currentTimeMillis());
            image.putBytes(byteArray).addOnSuccessListener(taskSnapshot -> Toast.makeText(activity, "Photo was successfully uploaded", Toast.LENGTH_LONG).show()).addOnFailureListener(e -> Toast.makeText(activity, "An error occurred while uploading the photo", Toast.LENGTH_LONG).show());
            //// viewModel.saveImageToGallery(requireActivity(), viewModel.getCapturedBitmap());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}