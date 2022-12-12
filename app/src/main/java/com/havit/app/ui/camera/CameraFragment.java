package com.havit.app.ui.camera;

import static com.havit.app.ui.camera.CameraViewModel.getCameraInstance;

import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.havit.app.R;
import com.havit.app.databinding.FragmentCameraBinding;

public class CameraFragment extends Fragment {

    private FragmentCameraBinding binding;

    private Camera mCamera;
    private CameraPreview mPreview;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        CameraViewModel cameraViewModel =
                new ViewModelProvider(this).get(CameraViewModel.class);

        binding = FragmentCameraBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(getActivity(), mCamera);
        FrameLayout preview = (FrameLayout) container.findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}