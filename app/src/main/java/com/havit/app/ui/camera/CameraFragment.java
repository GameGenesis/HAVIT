package com.havit.app.ui.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.camera.lifecycle.ProcessCameraProvider;

import com.google.common.util.concurrent.ListenableFuture;

import com.havit.app.R;
import com.havit.app.databinding.FragmentCameraBinding;

import java.util.concurrent.ExecutionException;

public class CameraFragment extends Fragment {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private FragmentCameraBinding binding;

        private PreviewView previewView;

        public View onCreateView(@NonNull LayoutInflater inflater,
                ViewGroup container, Bundle savedInstanceState) {
            CameraViewModel cameraViewModel =
                    new ViewModelProvider(this).get(CameraViewModel.class);

            binding = FragmentCameraBinding.inflate(inflater, container, false);
            View root = binding.getRoot();

            previewView = binding.previewView;

        // Check if the camera permission is granted...
        if (ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
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

        return root;
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider, View root) {
        Preview preview = new Preview.Builder()
                .build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}