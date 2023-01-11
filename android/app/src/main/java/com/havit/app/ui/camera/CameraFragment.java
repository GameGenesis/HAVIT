package com.havit.app.ui.camera;

import static android.content.Context.VIBRATOR_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.MediaActionSound;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import androidx.navigation.Navigation;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.havit.app.LoginActivity;

import com.havit.app.R;
import com.havit.app.databinding.FragmentCameraBinding;
import com.havit.app.ui.timeline.TimelineFragment;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraFragment extends Fragment {
    // Below code determines whether device language is set to Korean or Japanese. If so, the camera shutter sound has to be on due to the local legislation...
    public static boolean forceCameraSound = Objects.equals(LoginActivity.sDefSystemLanguage, "ko") || Objects.equals(LoginActivity.sDefSystemLanguage, "ja");

    private CameraViewModel viewModel;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private FragmentCameraBinding binding;
    private CameraSelector lensFacing = CameraSelector.DEFAULT_BACK_CAMERA;

    private int flashMode = ImageCapture.FLASH_MODE_OFF;

    private PreviewView previewView;
    private ImageView imageView;
    private TextView emptyText;

    private FloatingActionButton shutterButton;
    private ImageButton cancelButton, flipButton, flashButton;
    private Button addButton, createHabitButton;

    private Spinner habitSpinner;
    private ImageCapture imageCapture;
    private Bitmap bitmapImage;

    private AudioManager am;

    private FirebaseUser user;

    private final ArrayList<String> timelineItems = new ArrayList<>();

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        user = FirebaseAuth.getInstance().getCurrentUser();

        // Hide the action bar...
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).hide();

        viewModel = new ViewModelProvider(this).get(CameraViewModel.class);

        binding = FragmentCameraBinding.inflate(inflater, container, false);
        am = (AudioManager)requireActivity().getSystemService(Context.AUDIO_SERVICE);

        View root = binding.getRoot();
        addCameraProvider();

        previewView = binding.previewView;
        emptyText = binding.emptyText;

        emptyText.setTextColor(Color.WHITE);

        imageView = binding.imageView;
        imageView.setVisibility(View.GONE);

        shutterButton = binding.shutterButton;
        shutterButton.setOnClickListener(v -> {
            loadTemplates();
            hapticFeedback(v);
            handleShutter();
            takePhoto();
        });

        cancelButton = binding.cancelButton;
        cancelButton.setVisibility(View.GONE);
        cancelButton.setOnClickListener(v -> {
            hapticFeedback(v);
            closeImageView();
        });

        flipButton = binding.flipButton;
        flipButton.setOnClickListener(v -> {
            hapticFeedback(v);
            flipCamera();
        });

        flashButton = binding.flashButton;
        if (lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA) {
            flashButton.setVisibility(View.GONE);
        }

        flashButton.setOnClickListener(v -> {
            hapticFeedback(v);
            toggleFlash();
        });

        createHabitButton = binding.createHabitButton;
        createHabitButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_camera_to_timeline);
        });

        habitSpinner = binding.habitSpinner;
        habitSpinner.setVisibility(View.GONE);

        addButton = binding.addButton;
        addButton.setVisibility(View.GONE);
        addButton.setOnClickListener(v -> {
            if (bitmapImage != null) {
                // Gets the string of the selected template...
                String selectedItem = habitSpinner.getSelectedItem().toString();

                viewModel.addImageToDatabase(user, bitmapImage, requireActivity(), selectedItem);
                closeImageView();
            }
        });

        detectHorizontalSwipe(root);
        setUpNavigation();

        return root;
    }

    private void setUpNavigation() {
        // When the user uses the back navigation button or gesture
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (imageView.getVisibility() == View.GONE) {
                    // If the user is in the camera view, exits the app
                    requireActivity().finish();
                } else {
                    // If the user is in the image view, closes the image view
                    closeImageView();
                }
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }

    private void loadTemplates() {
        if (user == null || user.getEmail() == null)
            return;

        timelineItems.clear();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(user.getEmail());

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    // the value is in the document.get() method
                    List<Map<String, Object>> timelines = (List<Map<String, Object>>) document.get("user_timelines");

                    if (timelines != null) {
                        for (Map<String, Object> timeline : timelines) {
                            timelineItems.add(Objects.requireNonNull(timeline.get("name")).toString());
                        }

                        setUpSpinner();
                        cancelDisplayEmptyTimelineMessage();
                    }

                } else {
                    // the document does not exist
                    displayEmptyTimelineMessage();
                }
            } else {
                Log.e("Fatal Error", "Problem in retrieving the JSON template files from the server! It is likely that it's associated with the permission conflict.");
            }
        });
    }

    private void setUpSpinner() {
        // Create a new ArrayAdapter
        if (TimelineFragment.isOrderNewest) {
            Collections.reverse(timelineItems);
        }

        if (timelineItems.size() <= 1) {
            habitSpinner.setEnabled(false);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireActivity(), android.R.layout.simple_spinner_item, timelineItems) {
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
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setAllCaps(true);
                    ((TextView) view).setGravity(Gravity.CENTER);
                    ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                }
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner...
        habitSpinner.setAdapter(adapter);
    }

    private void takePhoto() {
        imageView.setVisibility(View.VISIBLE);
        imageCapture.takePicture(ContextCompat.getMainExecutor(requireActivity()), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                bitmapImage = captureBitmap(image);
                // If the user hasn't created any timelines...
                if (timelineItems.isEmpty()) {
                    displayEmptyTimelineMessage();
                }

                // Display the image on the ImageView
                imageView.setImageBitmap(bitmapImage);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                // Image capture failed
            }
        });
    }

    private void displayEmptyTimelineMessage() {
        emptyText.setVisibility(View.VISIBLE);
        habitSpinner.setVisibility(View.GONE);
        createHabitButton.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);
        previewView.setVisibility(View.GONE);
        addButton.setVisibility(View.GONE);
    }

    private void cancelDisplayEmptyTimelineMessage() {
        emptyText.setVisibility(View.GONE);
        habitSpinner.setVisibility(View.VISIBLE);
        createHabitButton.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);
        previewView.setVisibility(View.VISIBLE);
        addButton.setVisibility(View.VISIBLE);
    }

    private void closeImageView() {
        previewView.setVisibility(View.VISIBLE);
        imageView.setImageBitmap(null);
        imageView.setVisibility(View.GONE);
        shutterButton.show();
        cancelButton.setVisibility(View.GONE);
        addButton.setVisibility(View.GONE);
        habitSpinner.setVisibility(View.GONE);
        flipButton.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);
        createHabitButton.setVisibility(View.GONE);

        if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) {
            flashButton.setVisibility(View.VISIBLE);
        }
    }

    private void addCameraProvider() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    public Bitmap captureBitmap(@NonNull ImageProxy image) {
        // Get the image data as a Bitmap
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);

        // Close the image
        image.close();

        bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);

        // Rotate the bitmap image 90 degrees (landscape -> portrait)
        Matrix matrix = new Matrix();

        if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) {
            matrix.postRotate(90);

        } else {
            // Flip the image horizontally...
            matrix.postScale(-1.0f, 1.0f);
            // For selfie images...
            matrix.postRotate(90);
        }

        // Correct preview output to account for display rotation
        float rotationDegrees = 0;

        switch (imageView.getDisplay().getRotation()) {
            case Surface.ROTATION_0:
                rotationDegrees = 0;
                break;
            case Surface.ROTATION_90:
                rotationDegrees = 90;
                break;
            case Surface.ROTATION_180:
                rotationDegrees = 180;
                break;
            case Surface.ROTATION_270:
                rotationDegrees = 270;
                break;
        }

        matrix.postRotate(-rotationDegrees);

        bitmapImage = Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.getWidth(), bitmapImage.getHeight(), matrix, true);

        return bitmapImage;
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        Preview preview = new Preview.Builder().build();

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
                    .setFlashMode(flashMode)
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
            cameraProvider.bindToLifecycle(getViewLifecycleOwner(), lensFacing, imageCapture, imageAnalysis, preview);
        }
    }

    private void flipCamera() {
        if (lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA) {
            lensFacing = CameraSelector.DEFAULT_BACK_CAMERA;
            flashButton.setVisibility(View.VISIBLE);
        }
        else if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) {
            lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA;
            flashButton.setVisibility(View.GONE);
        }

        try {
            ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
            bindPreview(cameraProvider);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toggleFlash() {
        if (imageCapture.getFlashMode() == ImageCapture.FLASH_MODE_OFF) {
            flashMode = ImageCapture.FLASH_MODE_ON;
            imageCapture.setFlashMode(flashMode);
            flashButton.setImageResource(R.drawable.ic_baseline_flash_on);
        } else if (imageCapture.getFlashMode() == ImageCapture.FLASH_MODE_ON) {
            flashMode = ImageCapture.FLASH_MODE_OFF;
            imageCapture.setFlashMode(flashMode);
            flashButton.setImageResource(R.drawable.ic_baseline_flash_off);
        }
        // There is also ImageCapture.FLASH_MODE_AUTO
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
            flipButton.setVisibility(View.GONE);
            flashButton.setVisibility(View.GONE);
        }, 250);

        handler.postDelayed(() -> {
            habitSpinner.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
            if (!timelineItems.isEmpty()) {
                addButton.setVisibility(View.VISIBLE);
            }
        }, 300);

        // Play the snap sound...
        if (forceCameraSound || am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            MediaActionSound sound = new MediaActionSound();
            sound.play(MediaActionSound.SHUTTER_CLICK);
        }
    }

    private void hapticFeedback(View view) {
        Vibrator vibrator = (Vibrator) requireActivity().getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(10);
    }

    private void detectHorizontalSwipe(View view){
        final GestureDetector gesture = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                final int SWIPE_MIN_DISTANCE = 120;
                final int SWIPE_MAX_OFF_PATH = 250;
                final int SWIPE_THRESHOLD_VELOCITY = 200;
                try {
                    if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                        return false;
                    if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        Log.d("TOUCH", "Right to Left");
                    } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        Log.d("TOUCH", "Left to Right");
                    }
                } catch (Exception e) {
                    // nothing
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                return gesture.onTouchEvent(event);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}