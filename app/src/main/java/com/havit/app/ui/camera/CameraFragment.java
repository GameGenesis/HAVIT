package com.havit.app.ui.camera;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Size;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.AspectRatio;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.camera.lifecycle.ProcessCameraProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import com.havit.app.LoginActivity;
import com.havit.app.databinding.FragmentCameraBinding;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraFragment extends Fragment {
    // Below code determines whether device language is set to Korean or Japanese. If so, the camera shutter sound has to be on due to the local laws...
    public static boolean forceCameraSound = Objects.equals(LoginActivity.sDefSystemLanguage, "ko") || Objects.equals(LoginActivity.sDefSystemLanguage, "ja");

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private FragmentCameraBinding binding;

    private PreviewView previewView;
    private ImageView imageView;

    private FloatingActionButton shutterButton;
    private ImageButton cancelButton;
    private Button addButton;

    private Spinner habitSpinner;

    private Bitmap bitmapImage;
    private ImageCapture imageCapture;

    private AudioManager am;

    private enum CameraOrientation {
        VERTICAL,
        HORIZONTAL
    }

    private CameraOrientation curOrientation = CameraOrientation.VERTICAL;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        // Hide the action bar...
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).hide();

        CameraViewModel cameraViewModel =
                new ViewModelProvider(this).get(CameraViewModel.class);

        binding = FragmentCameraBinding.inflate(inflater, container, false);
        am = (AudioManager)requireActivity().getSystemService(Context.AUDIO_SERVICE);

        View root = binding.getRoot();
        addCameraProvider(root);

        previewView = binding.previewView;
        imageView = binding.imageView;

        shutterButton = binding.shutterButton;
        shutterButton.setOnClickListener(v -> {
            handleShutter();
            takePhoto();
        });

        cancelButton = binding.cancelButton;
        cancelButton.setVisibility(View.GONE);
        cancelButton.setOnClickListener(v -> {
            closeImageView();
        });

        addButton = binding.addButton;
        addButton.setVisibility(View.GONE);
        addButton.setOnClickListener(v -> {
            addPhoto();
        });

        habitSpinner = binding.habitSpinner;
        habitSpinner.setVisibility(View.GONE);
        setUpSpinner();

        return root;
    }

    private void setUpSpinner() {
        String[] items = {"First Timeline", "Second Timeline", "Third Timeline"};
        // Create a new ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireActivity(), android.R.layout.simple_spinner_item, items) {
            // Override the getView() and getDropDownView() methods to set the textAllCaps attribute
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setAllCaps(true);
                    ((TextView) view).setGravity(Gravity.CENTER);
                    ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                }
                return view;
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setAllCaps(true);
                    ((TextView) view).setGravity(Gravity.CENTER);
                    ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                }
                return view;
            }
        };
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        habitSpinner.setAdapter(adapter);

        // Called when an item is selected
        habitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Do something here when an item in the Spinner is selected
                switch (position) {
                    case 0:
                        // Whatever you want to happen when the first item gets selected
                        break;
                    case 1:
                        // Whatever you want to happen when the second item gets selected
                        break;
                    case 2:
                        // Whatever you want to happen when the third item gets selected
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do something here when nothing is selected in the Spinner
            }
        });
    }

    private void takePhoto() {
        imageCapture.takePicture(ContextCompat.getMainExecutor(requireActivity()), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                // Get the image data as a Bitmap
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);

                bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);

                // Rotate the bitmap image 90 degrees (landscape -> portrait)
                Matrix matrix = new Matrix();

                if (curOrientation == CameraOrientation.VERTICAL) {
                    matrix.postRotate(90);
                }

                bitmapImage = Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.getWidth(), bitmapImage.getHeight(), matrix, true);

                // Display the image on the ImageView
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

    private void saveImageToGallery(Bitmap bitmap) throws IOException {
        // Save the image to the MediaStore
        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DISPLAY_NAME, "Image-" + System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

        Uri imageUri = requireActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        OutputStream out = requireActivity().getContentResolver().openOutputStream(imageUri);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.close();
    }

    private void closeImageView() {
        imageView.setImageBitmap(null);
        shutterButton.show();
        cancelButton.setVisibility(View.GONE);
        addButton.setVisibility(View.GONE);
        habitSpinner.setVisibility(View.GONE);
    }

    private void addPhoto() {
        // To save the image
        try {
            saveImageToGallery(bitmapImage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(requireActivity(), "Saved To Gallery", Toast.LENGTH_LONG).show();
        closeImageView();
    }

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

    @SuppressLint("UnsafeOptInUsageError")
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
                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .setTargetRotation(rotation)
                        // Below code minimizes the shutter lag...
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_ZERO_SHUTTER_LAG)
                    .build();

            ImageAnalysis imageAnalysis =
                    new ImageAnalysis.Builder()
                            // enable the following line if RGBA output is needed.
                            //.setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                            .setTargetResolution(new Size(1280, 720))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build();

            Executor executor = Executors.newSingleThreadExecutor();

            imageAnalysis.setAnalyzer(executor, image -> {
                // Perform image analysis here
            });

            // Image Provider variable has to be fixed...
            cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, imageCapture, imageAnalysis, preview);

            listenToOrientation();
        }
    }

    private void handleShutter() {
        shutterButton.setScaleX(1.25f);
        shutterButton.setScaleY(1.25f);

        shutterButton.setAlpha(0.5f);

        // Asynchronous shutter button animation...
        Handler handler = new Handler();

        handler.postDelayed(() -> {
            shutterButton.setScaleX(1);
            shutterButton.setScaleY(1);

            shutterButton.setAlpha(1f);
            shutterButton.hide();
        }, 250);

        handler.postDelayed(() -> {
            habitSpinner.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.VISIBLE);
        }, 300);

        // Play the snap sound...
        if (forceCameraSound || am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            MediaActionSound sound = new MediaActionSound();
            sound.play(MediaActionSound.SHUTTER_CLICK);
        }
    }

    private void listenToOrientation() {
        OrientationEventListener mOrientationListener = new OrientationEventListener(
                requireContext()) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == 0 || orientation == 180) {
                    // Portrait...
                    curOrientation = CameraOrientation.VERTICAL;

                } else if (orientation == 90 || orientation == 270) {
                    // Landscape...
                    curOrientation = CameraOrientation.HORIZONTAL;
                }
            }
        };

        if (mOrientationListener.canDetectOrientation()) {
            mOrientationListener.enable();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}