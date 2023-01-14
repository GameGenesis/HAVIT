package com.havit.app.ui.edit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;

import android.net.Uri;

import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.provider.MediaStore;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

// import com.arthenica.mobileffmpeg.FFmpeg;
import com.google.android.material.card.MaterialCardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.havit.app.MainActivity;
import com.havit.app.R;
import com.havit.app.databinding.FragmentEditBinding;
import com.havit.app.ui.timeline.Timeline;
import com.havit.app.ui.timeline.TimelineArrayAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.nio.ByteBuffer;
import java.io.FileNotFoundException;
import java.io.FileWriter;

import java.util.ArrayList;
import java.util.Comparator;

import org.imaginativeworld.whynotimagecarousel.ImageCarousel;
import org.imaginativeworld.whynotimagecarousel.listener.CarouselOnScrollListener;
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.util.List;

import java.util.Map;
import java.util.Objects;

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
            try {
                downloadBackgroundMusic();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
     * Downloads a background music file from Firebase Storage.
     * @throws IOException if there is an issue creating the temporary music file.
     */
    private void downloadBackgroundMusic() throws IOException {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://havitcentral.appspot.com/templates/evolution.mp3");

        tempMusicFile = File.createTempFile("background-music", "ext");

        Toast.makeText(requireActivity(), "Downloading the Template...", Toast.LENGTH_SHORT).show();

        storageRef.getFile(tempMusicFile).addOnSuccessListener(taskSnapshot -> {
            // File downloaded successfully
            try {
                downloadTimelinePhotos();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }).addOnFailureListener(exception -> {
            // Handle failed download
        });
    }


    /**
     * Download images from the Firebase storage and store them in an ArrayList
     * Calls the combinePhotosIntoVideo() method to combine the images into a video
     * @throws IOException if an error occurs when trying to combine the images into a video
     */
    // GENERATED BY CHATGPT...
    private void downloadTimelinePhotos() throws IOException {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference folderRef = storage.getReference().child("users/" + user.getEmail() +  "/" + TimelineArrayAdapter.selectedTimeline.name);

        folderRef.listAll().addOnSuccessListener(listResult -> {
            int counter = 0;

            for (StorageReference item : listResult.getItems()) {
                item.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                    Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    images.add(image);

                }).addOnFailureListener(exception -> {
                    Log.e("ERROR_APPENDING_IMAGES", String.valueOf(exception));
                });

                counter++;
            }

            imagesSize = counter;

            try {
                combinePhotosIntoVideo();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    /**
     * Combines the bitmap images in the ArrayList into a video
     * Uses FFmpeg library to concatenate the images and adds background music to the final video
     * The final video is then uploaded to the Firebase storage
     * @throws IOException if an error occurs when trying to create a temporary file for the video or the images
     */
    private void combinePhotosIntoVideo() throws IOException {
        List<String> cmd = new ArrayList<>();
        cmd.add("-y");

        String inputFilePaths = "";

        int index = 0;

        for (Bitmap image : images) {
            File tempImageFile = File.createTempFile("temp-image", ".jpg");
            OutputStream os = new FileOutputStream(tempImageFile);
            image.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.close();

            long deltaTimeMillis = sortedTimestampKeys.get(index)[2];
            long seconds = (long) deltaTimeMillis / 1000;

            inputFilePaths += " -i " + tempImageFile.getAbsolutePath() + " -t " + seconds;

            index++;
        }
        cmd.add(inputFilePaths);

        // Add the background music
        cmd.add("-i");
        cmd.add(tempMusicFile.getAbsolutePath());

        cmd.add("-filter_complex");
        String filterComplex = "";

        for (int i = 0; i < imagesSize; i++) {
            filterComplex += "[" + i + ":v]";
        }
        filterComplex += "concat=n=" + imagesSize + ":v=1";
        filterComplex += "[video];";
        filterComplex += "[" + imagesSize + ":a]";
        filterComplex += "volume=1.5[audio]";

        Log.d("FILTER_COMPLEX", filterComplex);

        cmd.add(filterComplex);

        cmd.add("-map");
        cmd.add("[video]");
        cmd.add("-map");
        cmd.add("[audio]");

        cmd.add("-c:v");
        cmd.add("libx264");
        cmd.add("-crf");
        cmd.add("23");
        cmd.add("-preset");
        cmd.add("veryfast");
        cmd.add("-c:a");
        cmd.add("aac");
        cmd.add("-b:a");
        cmd.add("48k");
        cmd.add("-ar");
        cmd.add("22050");

        tempVideoFile = File.createTempFile("output-video", "ext");
        cmd.add(tempVideoFile.getAbsolutePath());

        String[] command = cmd.toArray(new String[0]);

        Toast.makeText(requireActivity(), "Uploading the exported video to Firebase storage...", Toast.LENGTH_SHORT).show();

//        FFmpeg.executeAsync(command, (executionId, returnCode) -> {
//            if (returnCode == 0) {
//                // command execution successful
//                FirebaseStorage storage = FirebaseStorage.getInstance();
//                StorageReference storageRef = storage.getReference();
//                StorageReference videoRef = storageRef.child("users/" + user.getEmail() + "/" + TimelineArrayAdapter.selectedTimeline.name + "/" + tempVideoFile.getName());
//                UploadTask uploadTask = videoRef.putFile(Uri.fromFile(tempVideoFile));
//
//                uploadTask.addOnFailureListener(exception -> {
//                    // Handle unsuccessful uploads
//                    Log.e("VIDEO_EXPORT_FAILURE", exception.toString());
//
//                }).addOnSuccessListener(taskSnapshot -> {
//                    // Handle successful uploads
//                    PopupDialog popup = new PopupDialog();
//                    popup.show(getChildFragmentManager(), "popup");
//                });
//
//            } else {
//                // command execution failed
//                // callback for onFailure
//                Log.e("RETURN_CODE_FAILURE", "HMM...");
//            }
//        });
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
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            sortedTimestampKeys.sort(Comparator.comparingLong(a -> a[0]));
                        }

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                items.sort((s1, s2) -> {
                    String fileName1 = s1.getName();
                    String fileName2 = s2.getName();

                    long fileNum1 = Long.parseLong(fileName1.replace("img-", ""));
                    long fileNum2 = Long.parseLong(fileName2.replace("img-", ""));

                    return Long.compare(fileNum1, fileNum2);
                });
            }

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