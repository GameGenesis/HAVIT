package com.havit.app.ui.edit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.os.Build;
import android.os.Bundle;

import android.transition.TransitionInflater;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.havit.app.LoginActivity;
import com.havit.app.MainActivity;
import com.havit.app.R;
import com.havit.app.databinding.FragmentEditBinding;
import com.havit.app.ui.timeline.Timeline;
import com.havit.app.ui.timeline.TimelineArrayAdapter;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Comparator;

import org.imaginativeworld.whynotimagecarousel.ImageCarousel;
import org.imaginativeworld.whynotimagecarousel.listener.CarouselOnScrollListener;
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.util.List;

import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditFragment extends Fragment {

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1;
    private FragmentEditBinding binding;
    private LinearLayout timelineContainer;
    private ImageCarousel carousel;
    private SeekBar seekBar;

    private FirebaseUser user;

    private Map<String, String> timestamp;
    private ArrayList<long[]> sortedTimestampKeys;
    private ArrayList<Bitmap> images;
    private String totalLength;

    private File tempMusicFile, tempVideoFile;

    private int imagesSize = 0;

    private long previousEndMillis = 0;
    private long totalLengthMillis;

    private float weightSum = 100;

    private boolean isFlipColor = true;
    private boolean isFullScreen = false;

    private EditViewModel editViewModel;

    private List<String> imgUrl = new ArrayList<String>();

    /**
     * Called when the fragment is first created
     * @param savedInstanceState Bundle containing the state of the fragment if it was previously created
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create a TransitionInflater object from the context of the fragment
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        // Set the enter and exit transitions for the fragment using the fade transition
        setEnterTransition(inflater.inflateTransition(R.transition.fade));
        setExitTransition(inflater.inflateTransition(R.transition.fade));
    }


    /**
     * Sets up the EditFragment View
     *
     * @param inflater           a LayoutInflater that inflates the layout for the fragment's UI
     * @param container          a ViewGroup that is the parent of the fragment's UI
     * @param savedInstanceState a Bundle that saves the state of the fragment
     * @return root, a View that is the root of the fragment's UI
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        // Hide the action bar...
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).show();

        sortedTimestampKeys = new ArrayList<>();
        images = new ArrayList<>();

        editViewModel = new ViewModelProvider(this).get(EditViewModel.class);

        binding = FragmentEditBinding.inflate(inflater, container, false);

        View root = binding.getRoot();

        user = FirebaseAuth.getInstance().getCurrentUser();

        final TextView timelineNameText = binding.timelineNameText;
        final ScrollView scrollView = binding.scrollView;
        final Button exportButton = binding.exportButton;

        editViewModel.getName().observe(getViewLifecycleOwner(), timelineNameText::setText);

        retrieveTimestamp();

        exportButton.setOnClickListener(v -> {
            sendUserTokenToServer();
        });

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
                    long[] millisArray = sortedTimestampKeys.get(i);

                    if (currentMillis >= millisArray[0] && currentMillis <= millisArray[1]) {
                        index = i;

                    } else {
                        // It's a gap clip then...
                    }
                }

                // Only if carousel is not autoplaying...
                if (!carousel.getAutoPlay()) {
                    try {
                        carousel.setCurrentPosition(index);

                    } catch (Exception e) {
                        Log.e("Index Out of Bounds", "Carousel was unable to find the given index");
                    }
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

                carousel.setAutoPlay(false);
                carousel.setShowNavigationButtons(true);

                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) carousel.getLayoutParams();
                params.height = pixels;
                carousel.setLayoutParams(params);

                scrollView.setVisibility(View.GONE);

                isFullScreen = true;

            } else {
                previewButton.setText("Preview");

                int dp = 350;
                int pixels = (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
                
                carousel.setAutoPlay(true);
                carousel.setShowNavigationButtons(false);

                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) carousel.getLayoutParams();
                params.height = pixels;
                carousel.setLayoutParams(params);

                scrollView.setVisibility(View.VISIBLE);

                isFullScreen = false;
            }
        });

        // Menu navigation: ttps://developer.android.com/jetpack/androidx/releases/activity#1.4.0-alpha01
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

    /**
     * Combines the bitmap images in the ArrayList into a video
     * Uses FFmpeg library to concatenate the images and adds background music to the final video
     * The final video is then uploaded to the Firebase storage
     * @throws IOException if an error occurs when trying to create a temporary file for the video or the images
     */

    private void sendUserTokenToServer() {
        assert user != null;
        user.getIdToken(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String accessToken = task.getResult().getToken();

                        // Send the access token to the Next.js API function
                        OkHttpClient client = new OkHttpClient();

                        assert accessToken != null;
                        RequestBody body = new FormBody.Builder()
                                .add("firebase_token", accessToken)
                                .build();
                        Request request = new Request.Builder()
                                .url("https://havit.space/api/firebase-auth")
                                .post(body)
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (response.isSuccessful()) {
                                    combinePhotosIntoVideo();
                                }
                            }
                        });
                    } else {
                        // Handle error
                    }
                });
    }

    private void combinePhotosIntoVideo() throws IOException {
        user.getIdToken(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String accessToken = task.getResult().getToken();

                        // Send the access token to the Next.js API function
                        OkHttpClient client = new OkHttpClient();

                        assert accessToken != null;
                        RequestBody body = new FormBody.Builder()
                                .add("timeline_name", TimelineArrayAdapter.selectedTimeline.name)
                                .add("template_name", TimelineArrayAdapter.selectedTimeline.selectedTemplate)
                                .build();

                        Request request = new Request.Builder()
                                .url("https://havit.space/api/export-video")
                                .post(body)
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (response.isSuccessful()) {
                                    // Get the file name of the combined image from the response
                                    int statusCode = response.code();

                                    Log.d("SERVER_RETURN_STATUS", String.valueOf(statusCode));

                                    if (statusCode == 200) {
                                        // Success...
                                        PopupDialog popup = new PopupDialog();
                                        popup.show(getChildFragmentManager(), "popup");

                                    } else if (statusCode == 400) {
                                        // Failure...
                                        requireActivity().runOnUiThread(new Thread(() -> {
                                            Toast.makeText(requireContext(), "Failed to generate the video", Toast.LENGTH_SHORT).show();
                                        }));
                                    }
                                }
                            }
                        });
                    } else {
                        // Handle error
                    }
                });
    }


    /**
     * Retrieves the timestamp data from the Firebase Firestore for the selected timeline
     * Uses the timestamp data and total length of the timeline to create the slider of the timeline
     * Sorts the timestamp data in ascending order for the gap between the slider blocks
     */
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

                            long startMillis = MainActivity.parseStringToMillis(startTimeArray);
                            long endMillis = MainActivity.parseStringToMillis(endTimeArray);
                            long deltaMillis = MainActivity.parseStringToMillis(endTimeArray);

                            // [startMillis, endMillis, deltaMillis] pairs...
                            sortedTimestampKeys.add(new long[]{startMillis, endMillis, deltaMillis});
                        }

                        // Sort the list in ascending order based on the first element of the nested arrays
                        sortedTimestampKeys.sort(Comparator.comparingLong(a -> a[0]));

                        for (long[] entry : sortedTimestampKeys) {
                            // If the clip is not at the start of the timeline or the difference
                            // between the previous timestamp and the current timestamp exceeds zero
                            // (meaning there's a gap between the clips)

                            long startMillis = entry[0];
                            long endMillis = entry[1];

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
                            seekBar.setMin((int) sortedTimestampKeys.get(0)[0]);
                            seekBar.setMax((int) sortedTimestampKeys.get(sortedTimestampKeys.size() - 1)[2]);

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


    /**
     * Called when the fragment is visible to the user and actively running
     * Used to resume any ongoing processes that were stopped in the onPause method
     */
    @Override
    public void onResume() {
        super.onResume();
    }


    /**
     * Sets the binding variable to null
     * Called when the fragment's view is destroyed
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    /**
     * Populates a carousel view with images from Firebase storage
     * Retrieves the images from the storage and sorts them in an increment order
     * Sets various properties for the carousel view
     * Sets a listener for the carousel scroll for the timeline slider
     */
    private void populateImageCarousel(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        assert user != null;
        String timeLineDirPath = "users/" + user.getEmail() + "/" + MainActivity.applyFileNamingScheme(Objects.requireNonNull(editViewModel.getName().getValue()));

        // Register lifecycle. For activity this will be lifecycle/getLifecycle() and for fragments it will be viewLifecycleOwner/getViewLifecycleOwner().
        carousel.registerLifecycle(getLifecycle());
        carousel.setAutoPlay(true);
        carousel.setAutoPlayDelay(1500);
        carousel.setShowIndicator(false);
        carousel.setShowNavigationButtons(false);
        carousel.setTouchToPause(true);
        carousel.setShowBottomShadow(false);
        carousel.setShowTopShadow(false);

        carousel.setAnimation(new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {

            }
        });

        long[] currentTimestamp = sortedTimestampKeys.get(0);
        seekBar.setProgress((int) ((currentTimestamp[0] + currentTimestamp[1]) / 2));

        carousel.setOnScrollListener(new CarouselOnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int i, int i1, @Nullable CarouselItem carouselItem) {
                try {
                    long[] currentTimestamp = sortedTimestampKeys.get(i1);
                    seekBar.setProgress((int) ((currentTimestamp[0] + currentTimestamp[1]) / 2));

                } catch (Exception e) {
                    Log.e("Index Out of Bounds", "The seekbar was unable to find the given index");
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int i, int i1, int i2, @Nullable CarouselItem carouselItem) {

            }
        });

        List<CarouselItem> list = new ArrayList<>();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference folderRef = storageRef.child(timeLineDirPath);

        folderRef.listAll().addOnSuccessListener(listResult -> {
            List<StorageReference> items = listResult.getItems();

            // create a custom comparator that compares the file name numbers
            items.sort((s1, s2) -> {
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
                    imgUrl.add(url);

                    list.add(new CarouselItem(url));
                    carousel.setData(list);
                });
            }

        }).addOnFailureListener(e -> {
            // Handle any errors
        });
    }
}