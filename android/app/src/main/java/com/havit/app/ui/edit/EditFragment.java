package com.havit.app.ui.edit;

import android.graphics.Color;

import android.os.Build;
import android.os.Bundle;

import android.transition.TransitionInflater;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

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
import org.imaginativeworld.whynotimagecarousel.listener.CarouselOnScrollListener;
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.util.List;

import java.util.Map;
import java.util.Objects;

public class EditFragment extends Fragment {

    private FragmentEditBinding binding;
    private LinearLayout timelineContainer;
    private MaterialCardView timelineWrapper;
    private ImageCarousel carousel;
    private SeekBar seekBar;

    private Map<String, String> timestamp;
    private ArrayList<int[]> sortedTimestampKeys;
    private String totalLength;

    private int totalLengthMillis;
    private int previousEndMillis = 0;

    private float weightSum = 100;

    private boolean isFlipColor = true;
    private boolean isFullScreen = false;

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
        final TextView titleText = binding.titleText;
        final TextView nameText = binding.nameText;

        final ScrollView scrollView = binding.scrollView;

        editViewModel.getName().observe(getViewLifecycleOwner(), nameTextView::setText);
        editViewModel.getTemplateName().observe(getViewLifecycleOwner(), templateNameTextView::setText);

        retrieveTimestamp();

        timelineContainer = binding.timelineContainer;
        carousel = binding.carousel;
        seekBar = binding.seekBar;

        seekBar.setOnClickListener(v -> carousel.setAutoPlay(false));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int index = 0;

                // Do something here with the changed progress value
                for (int i = 0; i < sortedTimestampKeys.size(); i++) {
                    int currentMillis = seekBar.getProgress();
                    int[] millisArray = sortedTimestampKeys.get(i);

                    if (currentMillis >= millisArray[0] && currentMillis <= millisArray[1]) {
                        index = i;

                    } else {
                        // It's a gap clip then...
                    }
                }

                // Only if carousel is not autoplaying...
                if (!carousel.getAutoPlay()) {
                    carousel.setCurrentPosition(index);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do something here when the user starts sliding the thumb
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Button previewButton = binding.previewButton;
        previewButton.setOnClickListener(v -> {
            if (!isFullScreen) {
                previewButton.setText("Close");

                int dp = 500;
                int pixels = (int) (dp * getResources().getDisplayMetrics().density + 0.5f);

                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) carousel.getLayoutParams();
                params.height = pixels;
                carousel.setLayoutParams(params);

                titleText.setVisibility(View.GONE);
                nameText.setVisibility(View.GONE);
                scrollView.setVisibility(View.GONE);

                isFullScreen = true;

            } else {
                previewButton.setText("Preview");

                int dp = 250;
                int pixels = (int) (dp * getResources().getDisplayMetrics().density + 0.5f);

                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) carousel.getLayoutParams();
                params.height = pixels;
                carousel.setLayoutParams(params);

                titleText.setVisibility(View.VISIBLE);
                nameText.setVisibility(View.VISIBLE);
                scrollView.setVisibility(View.VISIBLE);

                isFullScreen = false;
            }
        });

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

                        // Sort the list in ascending order based on the first element of the nested arrays
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

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            seekBar.setMin(sortedTimestampKeys.get(0)[0]);
                            seekBar.setMax(sortedTimestampKeys.get(sortedTimestampKeys.size() - 1)[2]);

                            populateImageCarousel();
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String timeLineDirPath = "users/" + user.getEmail() + "/" + MainActivity.applyFileNamingScheme(Objects.requireNonNull(editViewModel.getName().getValue()));

        // Register lifecycle. For activity this will be lifecycle/getLifecycle() and for fragments it will be viewLifecycleOwner/getViewLifecycleOwner().
        carousel.registerLifecycle(getLifecycle());
        carousel.setAutoPlay(true);
        carousel.setAutoPlayDelay(1000);
        carousel.setShowIndicator(false);

        int[] currentTimestamp = sortedTimestampKeys.get(0);
        seekBar.setProgress((currentTimestamp[0] + currentTimestamp[1]) / 2);

        carousel.setOnScrollListener(new CarouselOnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int i, int i1, @Nullable CarouselItem carouselItem) {
                int[] currentTimestamp = sortedTimestampKeys.get(i1);
                seekBar.setProgress(currentTimestamp[0] + currentTimestamp[1] / 2);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int i, int i1, int i2, @Nullable CarouselItem carouselItem) {

            }
        });

        List<CarouselItem> list = new ArrayList<>();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference folderRef = storageRef.child(timeLineDirPath);

        Log.d("EMAIL", timeLineDirPath);

        folderRef.listAll().addOnSuccessListener(listResult -> {
            List<StorageReference> items = listResult.getItems();

            // create a custom comparator that compares the file name numbers
            Collections.sort(items, (s1, s2) -> {
                String fileName1 = s1.getName();
                String fileName2 = s2.getName();

                long fileNum1 = Long.parseLong(fileName1.replace("img-", ""));
                long fileNum2 = Long.parseLong(fileName2.replace("img-", ""));

                return Long.compare(fileNum1, fileNum2);
            });

            // Now items is sorted in an increment order (oldest comes first)
            for (StorageReference item : items) {
                item.getDownloadUrl().addOnSuccessListener(uri -> {
                    String url = uri.toString();

                    list.add(new CarouselItem(url));
                    carousel.setData(list);
                });
            }

        }).addOnFailureListener(e -> {
            // Handle any errors
        });
    }
}