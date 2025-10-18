package com.garethevans.church.opensongtablet.multitrack;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.garethevans.church.opensongtablet.MainActivity;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.FloatWindow;
import com.garethevans.church.opensongtablet.customviews.MyFloatingActionButton;
import com.garethevans.church.opensongtablet.customviews.TrackSlider;
import com.garethevans.church.opensongtablet.databinding.ViewMultitrackBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MultiTrackPopUp {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "MultiTrackPopUp";

    private PopupWindow popupWindow;
    private MyFloatingActionButton closeButton;
    private FloatWindow floatWindow;
    private int posX;
    private int posY;
    private ViewMultitrackBinding myView;

    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private AudioProcessor audioProcessor;
    private Uri alternativeSongFolderUri;
    private ArrayList<String> audioFiles, filesNeedingConversion;
    private boolean minimised=false, trackInfoExists;
    private final VectorDrawableCompat maximiseDrawable, minimiseDrawable;
    private final String folder_found, folder_not_found, folder_not_valid, web_help;
    private final float multiTrackAlpha;


    @SuppressLint("InflateParams")
    public MultiTrackPopUp(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        maximiseDrawable = VectorDrawableCompat.create(c.getResources(), R.drawable.maximise, c.getTheme());
        minimiseDrawable = VectorDrawableCompat.create(c.getResources(), R.drawable.minimise, c.getTheme());
        folder_found = c.getString(R.string.multitrack_folder_found);
        folder_not_found = c.getString(R.string.multitrack_folder_not_found);
        folder_not_valid = c.getString(R.string.multitrack_folder_not_valid);
        web_help = c.getString(R.string.website_multitrack);

        // Prepare the alpha values - Tries to drop to minimum value of 0.7f or pageButtonAlpha when minimised
        float pageButtonAlpha = mainActivityInterface.getMyThemeColors().getPageButtonAlpha();
        multiTrackAlpha = Math.min(pageButtonAlpha, 0.7f);
    }

    @SuppressLint("InflateParams")
    public void floatMultiTrack(View viewHolder) {
        // If the popup is showing already, dismiss it
        // This is called when a song is about to load
        myView = ViewMultitrackBinding.bind(LayoutInflater.from(c).inflate(R.layout.view_multitrack, null));
        myView.dialogHeading.showMinimiseButton(true);
        myView.dialogHeading.setWebHelp(mainActivityInterface, web_help);

        if (popupWindow!=null && popupWindow.isShowing()) {
            destroyPopup();

            // Let's display the popup sticky note
        } else {
            // Set up the views
            getPositionAndSize();
            setupViews();
            setListeners();
            popupWindow.showAtLocation(viewHolder, Gravity.TOP | Gravity.START, posX, posY);

            // Deal with the moveable element (from the top bar)
            setupDrag();

            // Now check that we have files and that they don't need converted
            checkForMultiTracks();
        }
    }

    private void setupViews() {
        // The popup
        popupWindow = new PopupWindow(c);

        // The main layout (FloatWindow is just a custom linearlayout where I've overridden the performclick
        floatWindow = new FloatWindow(c);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        floatWindow.setLayoutParams(layoutParams);
        floatWindow.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable drawable = (GradientDrawable) ResourcesCompat.getDrawable(c.getResources(),
                R.drawable.popup_bg, c.getTheme());
        if (drawable!=null) {
            drawable.setColor(mainActivityInterface.getPalette().surface);
        }
        popupWindow.setBackgroundDrawable(null);
        floatWindow.setAlpha(1f);
        floatWindow.setBackground(drawable);
        floatWindow.setPadding(16,16,16,16);

        myView.progressText.setMyGravity(Gravity.CENTER_HORIZONTAL);

        // Tint the progressBar as the secondary color
        mainActivityInterface.getMyThemeColors().tintProgressBar(myView.progressBar);

        showProgressBar(true);
        showMultiTrackLayout(false);
        showFolderBar(false);
        showConversionBar(false);

        floatWindow.addView(myView.getRoot());
        popupWindow.setContentView(floatWindow);
    }

    private void setListeners() {
        myView.dialogHeading.getCloseButton().setOnClickListener(v -> destroyPopup());
        myView.dialogHeading.getMinimiseButton().setOnClickListener(view -> minimiseAction());
        myView.folderButton.setOnClickListener(v -> chooseStorageLocation());
        myView.conversionButton.setOnClickListener(v -> doConversion());
        myView.audioPlay.setOnClickListener(view -> mainActivityInterface.getMultiTrackPlayer().play());
        myView.audioPause.setOnClickListener(view -> mainActivityInterface.getMultiTrackPlayer().pause());
        myView.audioStop.setOnClickListener(view -> mainActivityInterface.getMultiTrackPlayer().stop());
    }

    private void getPositionAndSize() {
        posX = mainActivityInterface.getPreferences().getMyPreferenceInt("stickyXPosition", -1);
        posY = mainActivityInterface.getPreferences().getMyPreferenceInt("stickyYPosition", -1);
        int w = c.getResources().getDisplayMetrics().widthPixels;
        int h = c.getResources().getDisplayMetrics().heightPixels;

        // Fix the sizes
        if (posX == -1 || posX > w) {
            posX = w/2 - 32;
        }
        if (posX < 0) {
            posX = 0;
        }
        if (posY == -1 || posY > h) {
            posY = (int) ((float) mainActivityInterface.getToolbar().getActionBarHeight(mainActivityInterface.needActionBar())*1.2f);
        }
        if (posY < 0) {
            posY = 0;
        }
    }

    private void setupDrag() {
        floatWindow.setOnTouchListener(new View.OnTouchListener() {
            int orgX, orgY;
            int offsetX, offsetY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        orgX = (int) event.getX();
                        orgY = (int) event.getY();
                        floatWindow.performClick();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        offsetX = (int) event.getRawX() - orgX;
                        offsetY = (int) event.getRawY() - orgY;
                        popupWindow.update(offsetX, offsetY, -1, -1, true);
                        break;
                    case MotionEvent.ACTION_UP:
                        mainActivityInterface.getPreferences().setMyPreferenceInt("stickyXPosition", offsetX);
                        mainActivityInterface.getPreferences().setMyPreferenceInt("stickyYPosition", offsetY);
                }
                return true;
            }
        });
    }

    public void destroyPopup() {
        try {
            audioProcessor = null;
            mainActivityInterface.getMultiTrackPlayer().setTrackProgressView(null);
            mainActivityInterface.getMultiTrackPlayer().saveMultitrackSettings();
            mainActivityInterface.getMultiTrackPlayer().closeMultitrack();
            mainActivityInterface.nullMultitrackPopUp();

            if (popupWindow != null) {
                popupWindow.dismiss();
                popupWindow = null;
            }
            if (closeButton != null) {
                closeButton = null;
            }
            if (floatWindow != null) {
                floatWindow = null;
            }
            if (myView!=null) {
                myView = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkForMultiTracks() {
        // Do this on a separate thread
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Show the progress bar and hide the rest while we process the data
            showProgressBar(true);
            showMultiTrackLayout(false);
            showFolderBar(false);
            showConversionBar(false);

            // Initialise the audioProcessor class which gets the info, does the checks and conversions
            audioProcessor = new AudioProcessor(c);

            // Get the basic stuff
            String multiTrackSongFolder = audioProcessor.getExpectedMultiTrackFolder();
            Uri multiTrackSongFolderUri = audioProcessor.getMultiTrackSongFolderUri();

            if (alternativeSongFolderUri != null) {
                // We are using an alternative folder
                myView.folderTextView.setHint(mainActivityInterface.getStorageAccess().niceUriString(alternativeSongFolderUri));
                multiTrackSongFolderUri = alternativeSongFolderUri;

            } else {
                myView.folderTextView.setHint("OpenSong/Multitrack/" + multiTrackSongFolder);
            }

            // Keep the record in the AudioProcessor class and use that from now on
            audioProcessor.setMultiTrackFolderUri(multiTrackSongFolderUri);

            if (audioProcessor.getMultiTrackFolderUri() != null && mainActivityInterface.getStorageAccess().uriExists(audioProcessor.getMultiTrackFolderUri())) {
                // We have a song multitrack folder.
                myView.folderTextView.setText(folder_found);

                // Get a list of all audio tracks and also any that need conversion
                // Now index the songs and decide which are audio and which need conversion
                audioProcessor.indexAudioFiles(audioProcessor.getMultiTrackFolderUri());
                audioFiles = audioProcessor.getAudioFiles();
                trackInfoExists = audioProcessor.getTrackInfoExists();
                filesNeedingConversion = audioProcessor.getFilesNeedingConversion();

                if (!audioFiles.isEmpty()) {
                    // We have audio files.  Now decide if we are good to go, or if we need to convert them
                    if (!filesNeedingConversion.isEmpty()) {
                        // We can't proceed until we have converted our audio files to PCM 16bit WAV
                        showConversionBar(true);
                    } else {
                        // We can proceed with building the sliders
                        prepareMultiTracks();
                    }
                } else {
                    mainActivityInterface.getMainHandler().post(() -> {
                        if (myView!=null) {
                            myView.folderTextView.setText(folder_not_valid);
                        }
                    });
                }
            } else {
                myView.folderTextView.setText(folder_not_found);
            }
            // Hide the progress bar
            showProgressBar(false);
            showFolderBar(true);
        });
    }

    private void prepareMultiTracks() {
        // This is called in a non UI thread - be aware
        // Hide while we finish processing
        showProgressBar(true);
        showFolderBar(false);
        showConversionBar(false);
        showMultiTrackLayout(false);

        // Remove any sliders that might be there
        if (myView!=null) {
            mainActivityInterface.getMainHandler().post(() -> {
                if (myView!=null) {
                    myView.sliders.removeAllViews();
                }
            });
        }

        // If we have an trackInfo file
        MultiTrackValues trackInfoJson;
        Uri trackInfoUri = audioProcessor.getAppendedUri(audioProcessor.getMultiTrackFolderUri(),mainActivityInterface.getMultiTrackPlayer().trackInfoFilename);

        // Get the track length in secs - use the first track
        Uri trackUri = Uri.parse(audioProcessor.getMultiTrackFolderUri() + "%2F" + audioFiles.get(0));

        long fileSize = Math.round(mainActivityInterface.getStorageAccess().getFileSizeFromUri(trackUri) *1024f);  // subtract WAV header if WAV
        // For this part, assume we are in the correct format
        int sampleRate = 44100;
        int channels = 2;
        int bytesPerSample = 2;

        long totalFrames = fileSize / (bytesPerSample * channels);
        int trackLengthSecs = Math.round(((totalFrames * 1000f) / (float) sampleRate) / 1000f);

        myView.seekBar.setHint(mainActivityInterface.getTimeTools().timeFormatFixer(0) + " / " +
                mainActivityInterface.getTimeTools().timeFormatFixer(trackLengthSecs));

        if (trackInfoExists) {
            // Load it into a string and set as a json
            InputStream trackInfoInputStream = mainActivityInterface.getStorageAccess().getInputStream(trackInfoUri);
            String trackInfoString = mainActivityInterface.getStorageAccess().readTextFileToString(trackInfoInputStream);
            trackInfoJson = MainActivity.gson.fromJson(trackInfoString, MultiTrackValues.class);
        } else {
            // Create a basic file now
            trackInfoJson = new MultiTrackValues();
            ArrayList<AudioTrackValues> trackInfos = new ArrayList<>();
            for (int i=0;i<audioFiles.size();i++) {
                AudioTrackValues audioTrackValues = new AudioTrackValues();
                audioTrackValues.setTrackName(audioFiles.get(i));
                audioTrackValues.setTrackUri((audioProcessor.getAppendedUri(audioProcessor.getMultiTrackFolderUri(),audioFiles.get(i)).toString()));
                audioTrackValues.setTrackMute(false);
                audioTrackValues.setTrackSolo(false);
                audioTrackValues.setTrackVolume(100);
                audioTrackValues.setTrackPan("C");
                trackInfos.add(audioTrackValues);
            }
            // Also add on the master track
            AudioTrackValues masterTrackValues = new AudioTrackValues();
            masterTrackValues.setTrackName(mainActivityInterface.getMultiTrackPlayer().getMasterTrackIdentifier());
            masterTrackValues.setTrackUri(null);
            masterTrackValues.setTrackMute(false);
            masterTrackValues.setTrackSolo(false);
            masterTrackValues.setTrackVolume(100);
            masterTrackValues.setTrackPan("C");
            trackInfos.add(masterTrackValues);

            // Put them all in the json object
            trackInfoJson.setAudioTrackValues(trackInfos);

            // Write this file to the folder
            mainActivityInterface.getStorageAccess().docContractCreate(audioProcessor.getMultiTrackFolderUri(), "audio/wav", mainActivityInterface.getMultiTrackPlayer().trackInfoFilename);
            OutputStream trackInfoOutputStream = mainActivityInterface.getStorageAccess().getOutputStream(trackInfoUri);
            String outputFile = MainActivity.gson.toJson(trackInfoJson);
            mainActivityInterface.getStorageAccess().writeFileFromString(outputFile,trackInfoOutputStream);
        }

        // Prepare the track information
        mainActivityInterface.getMultiTrackPlayer().initialiseArrays(audioProcessor.getMultiTrackFolderUri(),trackInfoJson.getAudioTrackValues(), trackLengthSecs);
        mainActivityInterface.getMultiTrackPlayer().setTrackProgressView(myView.seekBar);

        // Prepare the sliders
        ArrayList<TrackSlider> trackSliders = new ArrayList<>();
        for (int i=0;i<audioFiles.size();i++) {
            String trackName = mainActivityInterface.getMultiTrackPlayer().getTrackName(i);
            int trackVolume = mainActivityInterface.getMultiTrackPlayer().getTrackVolume(i);
            String trackPan = mainActivityInterface.getMultiTrackPlayer().getTrackPan(i);
            boolean trackMute = mainActivityInterface.getMultiTrackPlayer().getTrackMute(i);
            boolean trackSolo = mainActivityInterface.getMultiTrackPlayer().getTrackSolo(i);

            final int which = i;
            mainActivityInterface.getMainHandler().post(() -> {
                if (myView!=null) {
                    try {
                        TrackSlider trackSlider = new TrackSlider(c, mainActivityInterface.getMultiTrackPlayer(), which,
                                trackName,
                                trackVolume,
                                trackPan,
                                trackMute,
                                trackSolo);

                        myView.sliders.addView(trackSlider);
                        trackSliders.add(trackSlider);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        // Now add the master gain control
        mainActivityInterface.getMainHandler().post(() -> {
            if (myView != null) {
                TrackSlider masterSlider = new TrackSlider(c, mainActivityInterface.getMultiTrackPlayer(),
                        -1, mainActivityInterface.getMultiTrackPlayer().getMasterTrackIdentifier(),
                        mainActivityInterface.getMultiTrackPlayer().getTrackMaster().getTrackVolumeInt(),
                        mainActivityInterface.getMultiTrackPlayer().getTrackMaster().getTrackPanString(), false, false);
                myView.sliders.addView(masterSlider);

                // Now we pass the audio off to the multitrack player to get ready
                mainActivityInterface.getMultiTrackPlayer().setTrackSliders(trackSliders, masterSlider);

            }
        });

        // Now show what we have
        showProgressBar(false);
        showFolderBar(true);
        showMultiTrackLayout(true);
    }

    private void doConversion() {
        // Go through each file in turn and convert it
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            showProgressBar(true);
            showFolderBar(false);
            showConversionBar(false);
            for (int i=0;i<filesNeedingConversion.size();i++) {
                String fileNeedingConversion = filesNeedingConversion.get(i);
                myView.progressBar.setProgress(i + 1);
                String newFilename = fileNeedingConversion;
                if (fileNeedingConversion.contains(".")) {
                    newFilename = fileNeedingConversion.substring(0, fileNeedingConversion.lastIndexOf("."));
                }
                newFilename = newFilename + ".wav";
                String what = fileNeedingConversion + " -> " + newFilename;
                Uri oldFile = audioProcessor.getAppendedUri(audioProcessor.getMultiTrackFolderUri(), fileNeedingConversion);
                Uri newFile = audioProcessor.getAppendedUri(audioProcessor.getMultiTrackFolderUri(), newFilename);
                if (mainActivityInterface.getStorageAccess().docContractCreate(audioProcessor.getMultiTrackFolderUri(), "audio/wav", newFilename)) {
                    audioProcessor.decodeToPCM(c, what, oldFile, newFile, myView.progressText);
                }
            }
            checkForMultiTracks();
        });
    }

    private void showMultiTrackLayout(final boolean show) {
        // Do this on the main UI thread
        mainActivityInterface.getMainHandler().post(() -> {
           if (myView!=null) {
               myView.sliders.setBackgroundColor(mainActivityInterface.getPalette().surface);
               myView.multiTrackLayout.setVisibility(show ? View.VISIBLE:View.GONE);
           }
        });
    }

    private void showFolderBar(final boolean show) {
        // Do this on the main UI thread
        mainActivityInterface.getMainHandler().post(() -> {
            if (myView!=null) {
                myView.folderBar.setVisibility(show ? View.VISIBLE:View.GONE);
            }
        });
    }

    private void showConversionBar(final boolean show) {
        // Do this on the main UI thread
        mainActivityInterface.getMainHandler().post(() -> {
            if (myView!=null) {
                myView.conversionBar.setVisibility(show ? View.VISIBLE:View.GONE);
            }
        });
    }

    private void showProgressBar(final boolean show) {
        // Do this on the main UI thread
        mainActivityInterface.getMainHandler().post(() -> {
            if (myView!=null) {
                myView.progressBar.setVisibility(show ? View.VISIBLE:View.GONE);
                myView.progressText.setVisibility(show ? View.VISIBLE:View.GONE);
                myView.progressText.setText("");
                myView.dialogHeading.getCloseButton().setEnabled(!show);
                myView.dialogHeading.getMinimiseButton().setEnabled(!show);
            }
        });
    }

    private void minimiseAction() {
        // This is only available once we have initialised the sliders
        mainActivityInterface.getMainHandler().post(() -> {
            minimised = !minimised;
            floatWindow.setAlpha(minimised ? multiTrackAlpha:1f);
            myView.dialogHeading.getMinimiseButton().setImageDrawable(minimised ? maximiseDrawable:minimiseDrawable);
            showFolderBar(!minimised);
            showMultiTrackLayout(!minimised);
        });
    }

    private void chooseStorageLocation() {
        if (mainActivityInterface.getAppPermissions().hasStoragePermissions()) {
            Intent intent;
            if (mainActivityInterface.getStorageAccess().lollipopOrLater()) {
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addFlags(mainActivityInterface.getStorageAccess().getAddPersistentWriteUriFlags());

                // IV - 'Commented in' this extra to try to always show internal and sd card storage
                intent.putExtra("android.content.extra.SHOW_ADVANCED", true);

                mainActivityInterface.selectFolder(intent);
            }
        }
    }

    public void processAlternativeFolderUri(Intent resultData) {
        // This is the newer version for Lollipop+ This is preferred!
        if (resultData != null && resultData.getData() != null) {
            Uri uri = resultData.getData();
            if (alternativeSongFolderUri != null && c.getContentResolver()!=null) {
                c.getContentResolver().takePersistableUriPermission(uri,
                        mainActivityInterface.getStorageAccess().getTakePersistentWriteUriFlags());
            }
            // Do this to get a valid uri
            DocumentFile documentFile = DocumentFile.fromTreeUri(c,uri);
            if (documentFile!=null) {
                alternativeSongFolderUri = documentFile.getUri();
            }
        } else {
            alternativeSongFolderUri = null;
        }

        checkForMultiTracks();
    }

}
