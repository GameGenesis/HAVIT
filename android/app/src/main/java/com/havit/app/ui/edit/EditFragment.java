package com.havit.app.ui.edit;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import android.transition.TransitionInflater;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.havit.app.MainActivity;
import com.havit.app.R;
import com.havit.app.databinding.FragmentEditBinding;
import com.havit.app.ui.timeline.Timeline;
import com.havit.app.ui.timeline.TimelineArrayAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.imaginativeworld.whynotimagecarousel.ImageCarousel;
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.util.List;

import java.util.Map;
import java.util.Objects;

public class EditFragment extends Fragment {

    private FragmentEditBinding binding;
    private LinearLayout timelineContainer;
    private MaterialCardView timelineWrapper;

    private Map<String, String> timestamp;
    private ArrayList<int[]> sortedTimestampKeys;
    private String totalLength;

    private int totalLengthMillis;
    private int previousEndMillis = 0;

    private float weightSum = 100;

    private boolean isFlipColor = true;

    private EditViewModel editViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TransitionInflater inflater = TransitionInflater.from(requireContext());

        setEnterTransition(inflater.inflateTransition(R.transition.fade));
        setExitTransition(inflater.inflateTransition(R.transition.fade));
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        // Hide the action bar...
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).show();

        sortedTimestampKeys = new ArrayList<>();

        editViewModel = new ViewModelProvider(this).get(EditViewModel.class);

        binding = FragmentEditBinding.inflate(inflater, container, false);

        View root = binding.getRoot();

        final TextView nameTextView = binding.nameText;
        final TextView templateNameTextView = binding.templateNameText;

        timelineWrapper = binding.timelineWrapper;

        final SeekBar seekBar = binding.seekBar;

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
           @Override
           public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
               // Do something here with the changed progress value
           }

           @Override
           public void onStartTrackingTouch(SeekBar seekBar) {
               // Do something here when the user starts sliding the thumb
           }

           @Override
           public void onStopTrackingTouch(SeekBar seekBar) {

           }
       });

        editViewModel.getName().observe(getViewLifecycleOwner(), nameTextView::setText);
        editViewModel.getTemplateName().observe(getViewLifecycleOwner(), templateNameTextView::setText);

        retrieveTimestamp();

        timelineContainer = binding.timelineContainer;

        // Menu navigation: https://developer.android.com/jetpack/androidx/releases/activity#1.4.0-alpha01
        // The usage of an interface lets you inject your own implementation
        MenuHost menuHost = requireActivity();

        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                // Add menu items here
                // e.g. menuInflater.inflate(R.menu.bottom_nav_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                // Handle the menu selection
                if (menuItem.getItemId() == android.R.id.home) {
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.action_edit_to_timeline);
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        populateImageCarousel();

        return root;
    }

    @SuppressWarnings("unchecked")
    private void retrieveTimestamp() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Arguably the most important line...
        Timeline selectedTimeline = TimelineArrayAdapter.selectedTimeline;

        DocumentReference docRef = db.collection("templates").document(MainActivity.applyFileNamingScheme(selectedTimeline.selectedTemplate));

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    timestamp = (Map<String, String>) document.get("timestamp");
                    totalLength = (String) document.get("total_length");

                    if (totalLength != null) {
                        totalLengthMillis = MainActivity.parseStringToMillis(totalLength.split(":"));
                        weightSum = totalLengthMillis;

                        timelineContainer.setWeightSum(weightSum);
                    }

                    // Iterate over the timestamp hashmap...
                    if (timestamp != null) {
                        for (Map.Entry<String, String> entry : timestamp.entrySet()) {
                            String key = entry.getKey();
                            String value = entry.getValue();

                            String[] keyArray = key.split("-");
                            String[] startTimeArray = keyArray[0].split(":");
                            String[] endTimeArray = keyArray[1].split(":");

                            int startMillis = MainActivity.parseStringToMillis(startTimeArray);
                            int endMillis = MainActivity.parseStringToMillis(endTimeArray);
                            int deltaMillis = MainActivity.parseStringToMillis(endTimeArray);

                            // [startMillis, endMillis, deltaMillis] pairs...
                            sortedTimestampKeys.add(new int[]{startMillis, endMillis, deltaMillis});
                        }

                        // Sort the list in ascending order based on the third element of the nested arrays
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Collections.sort(sortedTimestampKeys, Comparator.comparingInt(a -> a[0]));
                        }

                        for (int[] entry : sortedTimestampKeys) {
                            // If the clip is not at the start of the timeline or the difference
                            // between the previous timestamp and the current timestamp exceeds zero
                            // (meaning there's a gap between the clips)

                            int startMillis = entry[0];
                            int endMillis = entry[1];

                            if (startMillis - previousEndMillis > 0) {
                                View view = new View(requireContext());

                                // Set a weight corresponding to the gap between the previous clip and the current clip...
                                float weight = (float) startMillis - previousEndMillis;

                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                        0, ViewGroup.LayoutParams.MATCH_PARENT, weight);

                                view.setLayoutParams(layoutParams);

                                view.setBackgroundColor(Color.DKGRAY);

                                timelineContainer.addView(view);
                            }

                            View view = new View(requireContext());

                            float weight = (float) (endMillis - startMillis);

                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                    0, ViewGroup.LayoutParams.MATCH_PARENT, weight);

                            view.setLayoutParams(layoutParams);

                            if (isFlipColor) {
                                view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.navy));
                                isFlipColor = false;
                            } else {
                                view.setBackgroundColor(Color.rgb(38, 68, 77));
                                isFlipColor = true;
                            }

                            timelineContainer.addView(view);

                            previousEndMillis = endMillis;
                        }
                    }

                } else {
                    // Document does not exist
                    System.out.println("Document does not exist");
                }
            } else {
                // Failed to get the document
                System.out.println("Failed to get document");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void populateImageCarousel(){
        ImageCarousel carousel = binding.carousel;

        ActivityResultLauncher<Intent> galleryActivityResultLauncher;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        List<String> timelineImgpath = new ArrayList<>();

        String timeLineDirPath = "users/" + user.getEmail() + "/" + editViewModel.getName().getValue() + "/";

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(timeLineDirPath);

        List<String> imageUrls = new ArrayList<>();



        // Register lifecycle. For activity this will be lifecycle/getLifecycle() and for fragments it will be viewLifecycleOwner/getViewLifecycleOwner().
        carousel.registerLifecycle(getLifecycle());

        List<CarouselItem> list = new ArrayList<>();

        // Image URL with caption
        list.add(
                new CarouselItem(
                        "https://firebasestorage.googleapis.com/v0/b/havitcentral.appspot.com/o/users%2Fpasswordtesting%40gmail.com%2Fqqqqqq%2Fimg-1673276856461?alt=media&token=51d24ce3-5011-4235-8a6b-69d4a36fef80",
                        "YEAR TWO"
                )
        );

        carousel.setData(list);
    }
}