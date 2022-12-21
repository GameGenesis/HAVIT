package com.havit.app.ui.camera;

import android.os.Bundle;
import android.util.Size;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.camera.lifecycle.ProcessCameraProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import com.havit.app.databinding.FragmentCameraBinding;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraFragment extends Fragment {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private FragmentCameraBinding binding;

    private PreviewView previewView;

    private Executor executor;

    private ImageCapture imageCapture;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        CameraViewModel cameraViewModel =
                new ViewModelProvider(this).get(CameraViewModel.class);

        binding = FragmentCameraBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        previewView = binding.previewView;
        FloatingActionButton button = binding.cameraShutterButton;

        requestCameraPermission(root);

        button.setOnClickListener(v -> {
            ImageCapture.OutputFileOptions outputFileOptions =
                    new ImageCapture.OutputFileOptions.Builder(new File("")).build();
            imageCapture.takePicture(outputFileOptions, executor,
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            requireActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(requireActivity(), "SAVED!", Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            requireActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(requireActivity(), "ERROR!", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
            );
        });

        return root;
    }

    private void requestCameraPermission(View root) {
        // Check if the camera permission is granted...
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