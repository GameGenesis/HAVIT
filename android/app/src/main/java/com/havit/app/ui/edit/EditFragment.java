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

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

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

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1;
    private FragmentEditBinding binding;
    private LinearLayout timelineContainer;
    private MaterialCardView timelineWrapper;
    private ImageCarousel carousel;
    private SeekBar seekBar;

    private FirebaseUser user;
    private ByteBuffer audioBuffer;

    private Map<String, String> timestamp;
    private ArrayList<int[]> sortedTimestampKeys;
    private ArrayList<Bitmap> images;
    private String totalLength;

    private MediaCodec encoder;
    private MediaFormat videoFormat;
    private MediaMuxer muxer;
    private File tempMusicFile, tempVideoFile;

    private int totalLengthMillis, inputBufferIndex, outputBufferIndex, readSampleSize;
    private int previousEndMillis = 0;

    private float weightSum = 100;

    private long intervalUs;

    private boolean isFlipColor = true;
    private boolean isFullScreen = false;

    private EditViewModel editViewModel;

    private List<String> imgUrl = new ArrayList<String>();

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
                    int[] millisArray = sortedTimestampKeys.get(i);

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

    // GENERATED BY CHATGPT...
    private void downloadTimelinePhotos() throws IOException {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference folderRef = storage.getReference().child("users/" + user.getEmail() +  "/" + TimelineArrayAdapter.selectedTimeline.name);

        folderRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference item : listResult.getItems()) {
                item.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                    Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    images.add(image);
                });
            }

            // THIS CODE SNIPPET WAS GENERATED BY CHATGPT...
            try {
                int bitRate = 8000000; // 8Mbps
                int frameRate = 30;

                intervalUs = (long) (1000000.0 / frameRate);

                videoFormat = MediaFormat.createVideoFormat("video/avc", 1080, 1920);
                videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
                videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
                videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);

                // H.264 format...
                encoder = MediaCodec.createEncoderByType("video/avc");
                encoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                encoder.start();

                inputBufferIndex = encoder.dequeueInputBuffer(0);

                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, 0);

                if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER){
                    // Not ready yet

                } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                    // Format Changed, handle it

                } else if (outputBufferIndex < 0){
                    // Something went wrong
                }

                combinePhotosIntoVideo();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        PopupDialog popup = new PopupDialog();
        popup.show(getChildFragmentManager(), "popup");
    }

    private void combinePhotosIntoVideo() throws IOException {
        tempVideoFile = File.createTempFile("output-video", "ext");

        muxer = new MediaMuxer(tempVideoFile.getPath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        int videoTrackIndex = muxer.addTrack(videoFormat);
        muxer.start();

        int index = 0;

        for (Bitmap image : images) {
            // Encode the image as a video frame
            ByteBuffer buffer = encoder.getInputBuffer(inputBufferIndex);

            // Convert a bitmap image into a byte array...
            byte[] pixels = new byte[image.getByteCount()];
            ByteBuffer byteBuffer = ByteBuffer.wrap(pixels);
            image.copyPixelsToBuffer(byteBuffer);

            // Write the image data to the buffer
            buffer.put(pixels);

            int deltaMillis = sortedTimestampKeys.get(index)[2];
            int presentationTimeUs = deltaMillis * 1000;

            // Queue the buffer to the encoder
            encoder.queueInputBuffer(inputBufferIndex, 0, image.getByteCount(), presentationTimeUs, 0);

            // Get the encoded video frame
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            int outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, 0);

            if (outputBufferIndex >= 0) {
                ByteBuffer encodedData = encoder.getOutputBuffer(outputBufferIndex);
                // Write the encoded frame to the muxer
                muxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo);

                // Update the presentation time for the next frame
                presentationTimeUs += intervalUs;
            }

            index++;
        }

        muxer.stop();
        muxer.release();
    }

    private void applyBackgroundMusic() throws IOException {
        MediaExtractor audioExtractor = new MediaExtractor();
        audioExtractor.setDataSource(tempMusicFile.getPath());

        readSampleSize = audioExtractor.readSampleData(audioBuffer, 0);
        audioBuffer = ByteBuffer.allocate(1024*1024);

        int audioTrackIndex = muxer.addTrack(audioExtractor.getTrackFormat(0));

        while (true) {
            int readSampleSize = audioExtractor.readSampleData(audioBuffer, 0);

            if (readSampleSize < 0) {
                break;
            }

            long presentationTime = audioExtractor.getSampleTime();

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            bufferInfo.size = readSampleSize;
            bufferInfo.presentationTimeUs = presentationTime;
            bufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;

            muxer.writeSampleData(audioTrackIndex, audioBuffer, bufferInfo);
            audioExtractor.advance();
        }

        audioExtractor.release();
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

        int[] currentTimestamp = sortedTimestampKeys.get(0);
        seekBar.setProgress((currentTimestamp[0] + currentTimestamp[1]) / 2);

        carousel.setOnScrollListener(new CarouselOnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int i, int i1, @Nullable CarouselItem carouselItem) {
                try {
                    int[] currentTimestamp = sortedTimestampKeys.get(i1);
                    seekBar.setProgress((currentTimestamp[0] + currentTimestamp[1]) / 2);

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