package com.havit.app.ui.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Size;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.camera.lifecycle.ProcessCameraProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import com.havit.app.databinding.FragmentCameraBinding;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraFragment extends Fragment {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private FragmentCameraBinding binding;

    private PreviewView previewView;
    private ImageView imageView;

    private Executor executor;

    private ImageCapture imageCapture;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        CameraViewModel cameraViewModel =
                new ViewModelProvider(this).get(CameraViewModel.class);

        binding = FragmentCameraBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        previewView = binding.previewView;
        imageView = binding.imageView;
        FloatingActionButton button = binding.cameraShutterButton;

        addCameraProvider(root);

        button.setOnClickListener(v -> {
            takePhoto();
        });

        return root;
    }

    private void takePhoto() {
        imageCapture.takePicture(ContextCompat.getMainExecutor(getActivity()), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                // Get the image data as a Bitmap
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);

                // Display the image on the ImageView
                imageView.setRotation(90);
                imageView.setImageBitmap(bitmapImage);

                // Close the image
                image.close();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                // Image capture failed
            }
        });
    }

    /*private void savePhoto() {
        File photoFile = null;

        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(requireContext(),
                    "com.havit.app.FileProvider",
                    photoFile);

            String path = photoFile.getPath();

            ImageCapture.OutputFileOptions outputFileOptions =
                    new ImageCapture.OutputFileOptions.Builder(photoFile).build();
            imageCapture.takePicture(outputFileOptions, executor,
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireActivity(), path, Toast.LENGTH_LONG).show();
                                Bitmap bitmap = BitmapFactory.decodeFile(path);
                                imageView.setImageBitmap(bitmap);
                            });
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            requireActivity().runOnUiThread(() -> Toast.makeText(requireActivity(), "ERROR!", Toast.LENGTH_LONG).show());
                        }
                    }
            );
        }
    }

    // Code snippet from: https://github.com/1010code/android-take-photo-sand-save-gallery
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = DateFormat.getDateTimeInstance().format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";

        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  *//* prefix *//*
                ".jpg",         *//* suffix *//*
                storageDir      *//* directory *//*
        );

        // Save a file: path for use with ACTION_VIEW intents
        String currentPhotoPath = image.getAbsolutePath();
        return image;
    }*/

    private void addCameraProvider(View root) {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider, root);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider, View root) {
        Preview preview = new Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        WindowManager windowManager = requireActivity().getWindowManager();
        Display display = windowManager.getDefaultDisplay();

        int rotation;

        if (display != null) {
            rotation = display.getRotation();

            imageCapture =
                    new ImageCapture.Builder()
                            .setTargetRotation(rotation)
                            .build();

            ImageAnalysis imageAnalysis =
                    new ImageAnalysis.Builder()
                            // enable the following line if RGBA output is needed.
                            //.setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                            .setTargetResolution(new Size(1280, 720))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build();

            executor = Executors.newSingleThreadExecutor();

            imageAnalysis.setAnalyzer(executor, image -> {
                // Perform image analysis here
            });

            // Image Provider variable has to be fixed...
            cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, imageCapture, imageAnalysis, preview);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}