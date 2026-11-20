package com.garethevans.church.opensongtablet.utilities;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.core.content.ContextCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.DialogHeader;
import com.garethevans.church.opensongtablet.customviews.FloatWindow;
import com.garethevans.church.opensongtablet.customviews.MyExtendedFloatingActionButton;
import com.garethevans.church.opensongtablet.customviews.MyFloatingActionButton;
import com.garethevans.church.opensongtablet.customviews.MyMaterialEditText;
import com.garethevans.church.opensongtablet.customviews.MyMaterialSimpleTextView;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.AreYouSureBottomSheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class AudioRecorderPopUp {

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String TAG = "AudioRecorderPopUp";
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final int posX, posY, colorInactive, colorActive;
    private final Drawable recordStart, recordStop;
    private final float recordingAlpha;
    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private PopupWindow popupWindow;
    private MyFloatingActionButton closeButton, previewButton, deleteButton, saveButton, linkButton;
    private LinearLayout dealWithAudioLayout;
    private FrameLayout linkButtonLayout;
    private MyExtendedFloatingActionButton recordButton;
    private FloatWindow floatWindow;
    private MediaRecorder mediaRecorder;
    private boolean isRecording = false, isPlaying = false, mediaPlayerIsPrepared = false;
    private File tempFilename;
    private MyMaterialEditText audioFilename;
    private MyMaterialSimpleTextView floatMessage;
    private final String fileExtension = ".m4a";
    private final int bitDepth = 16;
    private final int samplingRate = 44100;
    private final int encodingBitrate = samplingRate * bitDepth;
    private final String audio_link_string, overwrite_exisiting_string,
            success_string, error_string;
    private Handler messageHandler;
    private Runnable showMessageRunnable;
    String alertMessage = "";

    // Initialise the popup class
    public AudioRecorderPopUp(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        posX = 0;
        posY = (int) ((float) mainActivityInterface.getToolbar().getActionBarHeight(mainActivityInterface.needActionBar()) * 1.2f);
        recordStart = ContextCompat.getDrawable(c, R.drawable.record);
        recordStop = ContextCompat.getDrawable(c, R.drawable.stop);
        colorInactive = mainActivityInterface.getPalette().secondaryVariant;
        colorActive = mainActivityInterface.getPalette().errorColor;
        float pageButtonAlpha = mainActivityInterface.getMyThemeColors().getPageButtonAlpha();
        recordingAlpha = Math.max(pageButtonAlpha, 0.7f);
        audio_link_string = c.getString(R.string.link_audio);
        overwrite_exisiting_string = c.getString(R.string.overwrite);
        success_string = c.getString(R.string.success);
        error_string = c.getString(R.string.error);
        mediaPlayerOnPrepared();
    }

    // The views and listeners for the popup
    public void floatRecorder(View viewHolder) {
        // If the popup is showing already, dismiss it
        if (popupWindow != null && popupWindow.isShowing()) {
            try {
                popupWindow.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Set up the views
            setupViews();
            setListeners();

            popupWindow.showAtLocation(viewHolder, Gravity.TOP | Gravity.START, posX, posY);

            // Deal with the moveable element (from the top bar)
            setupDrag();
        }
    }
    private void setupViews() {
        // The popup
        popupWindow = new PopupWindow(c);

        popupWindow.setBackgroundDrawable(null);

        // FIX: Make the popup window focusable so child EditText views can receive focus and show the keyboard
        popupWindow.setFocusable(true);

        // Ensure the soft input mode resizes or adjusts properly when the keyboard appears
        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        popupWindow.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.setOutsideTouchable(false);

        popupWindow.setOnDismissListener(this::destroyPopup);

        // The main layout (FloatWindow is just a custom linearlayout where I've overridden the performclick
        floatWindow = new FloatWindow(c);
        floatWindow.setAlpha(1f);

        View myView = View.inflate(c, R.layout.view_audio_recorder_popup, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //myView.findViewById(R.id.layout).setBackgroundTintList(ColorStateList.valueOf(mainActivityInterface.getPalette().secondary));
            myView.findViewById(R.id.layout).setBackgroundTintList(new ColorStateList(
                    new int[][]{new int[0]},
                    new int[]{mainActivityInterface.getPalette().secondary}
            ));
        } else {
            myView.findViewById(R.id.layout).setBackgroundColor(mainActivityInterface.getPalette().secondary);
        }
        floatWindow.addView(myView);

        DialogHeader dialogHeader = myView.findViewById(R.id.dialogHeader);
        dialogHeader.setText(c.getString(R.string.audio_recorder));
        dialogHeader.setWebHelp(mainActivityInterface, c.getString(R.string.website_audio_recorder));
        closeButton = dialogHeader.getCloseButton();
        recordButton = myView.findViewById(R.id.recordButton);
        recordButton.setIcon(recordStart);
        recordButton.setText(c.getString(R.string.start));

        audioFilename = myView.findViewById(R.id.audioFilename);
        previewButton = myView.findViewById(R.id.previewButton);
        deleteButton = myView.findViewById(R.id.deleteButton);
        saveButton = myView.findViewById(R.id.saveButton);
        linkButtonLayout = myView.findViewById(R.id.linkButtonLayout);
        linkButton = myView.findViewById(R.id.linkButton);
        // Hide the link button until we have saved the file
        linkButtonLayout.setVisibility(View.GONE);
        floatMessage = myView.findViewById(R.id.floatMessage);
        mainActivityInterface.getMyThemeColors().tintPopup(floatMessage,mainActivityInterface.getPalette().secondaryVariant);
        floatMessage.setVisibility(View.GONE);
        messageHandler = mainActivityInterface.getMainHandler();
        showMessageRunnable = () -> {
            floatMessage.setText(alertMessage);
            floatMessage.setVisibility(View.VISIBLE);
            floatMessage.postDelayed(() -> {
                alertMessage = "";
                floatMessage.setText("");
                floatMessage.setVisibility(View.GONE);
            },2000);
        };
        dealWithAudioLayout = myView.findViewById(R.id.dealWithAudioLayout);

        dealWithAudioLayout.setVisibility(View.GONE);

        popupWindow.setContentView(floatWindow);
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
                }
                return true;
            }
        });
    }
    private void setListeners() {
        closeButton.setOnClickListener(view -> destroyPopup());
        recordButton.setOnClickListener(view -> {
            if (mainActivityInterface.getAppPermissions().hasAudioPermissions()) {
                if (isRecording) {
                    stopRecording();
                } else {
                    startRecording();
                }
            } else {
                destroyPopup();
            }
        });
        previewButton.setOnClickListener(view -> {
            if (tempFilename != null && tempFilename.exists()) {
                if (!mediaPlayerIsPrepared && !isPlaying) {
                    prepareMediaPlayer();
                } else {
                    if (isPlaying) {
                        startMediaPlayer();
                    } else {
                        stopMediaPlayer();
                    }
                }
            }
        });
        deleteButton.setOnClickListener(view -> {
            if (tempFilename != null) {
                if (tempFilename.delete()) {
                    dealWithAudioLayout.setVisibility(View.GONE);
                    recordButton.setVisibility(View.VISIBLE);
                    stopMediaPlayer();
                    resetMediaPlayer();
                }
            }
            mediaPlayerIsPrepared = false;
        });
        saveButton.setOnClickListener(view -> doSaveRecording());
        linkButton.setOnClickListener(view -> addAsLink());
    }

    // The mediaRecorder (to start and stop the recording properly)
    private void startRecording() {
        startMediaRecorder();
        recordButton.setIcon(recordStop);
        recordButton.setText(c.getString(R.string.stop));
        //recordButton.setBackgroundTintList(ColorStateList.valueOf(colorActive));
        recordButton.setBackgroundTintList(new ColorStateList(
                new int[][]{new int[0]},
                new int[]{colorActive}
        ));
        floatWindow.setAlpha(recordingAlpha);
    }
    private void stopRecording() {
        stopMediaRecorder();
        recordButton.setIcon(recordStart);
        recordButton.setText(c.getString(R.string.record));
        //recordButton.setBackgroundTintList(ColorStateList.valueOf(colorInactive));
        recordButton.setBackgroundTintList(new ColorStateList(
                new int[][]{new int[0]},
                new int[]{colorInactive}
        ));
        floatWindow.setAlpha(1f);

        recordButton.setVisibility(View.GONE);
        dealWithAudioLayout.setVisibility(View.VISIBLE);
    }
    private void stopMediaRecorder() {
        if (mediaRecorder!=null) {
            if (isRecording) {
                try {
                    mediaRecorder.stop();
                    String possibleFilename = mainActivityInterface.getSong().getTitle() + "-" +
                            mainActivityInterface.getTimeTools().getFormattedTimestamp(System.currentTimeMillis()) +
                                    ".m4a";
                    audioFilename.setText(possibleFilename);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        isRecording = false;
    }
    private void startMediaRecorder() {
        tempFilename = mainActivityInterface.getStorageAccess().getAppSpecificFile("Audio", "", "OpenSongAudio"+fileExtension);
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(encodingBitrate);
        mediaRecorder.setAudioSamplingRate(samplingRate);
        mediaRecorder.setOutputFile(tempFilename.getAbsolutePath());

        if (mediaRecorder!=null) {
            if (!isRecording) {
                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isRecording = true;
            }
        }
    }
    private void doSaveRecording() {
        // Get the filename, but make sure it ends with .m4a the correct extension
        if (audioFilename != null && audioFilename.getText() != null) {
            String filename = audioFilename.getText().toString();
            if (!filename.endsWith(fileExtension)) {
                filename = filename + fileExtension;
            }
            Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Media", "", filename);
            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true,
                    uri, null, "Media", "", filename);
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = new FileInputStream(tempFilename);
                outputStream = mainActivityInterface.getStorageAccess().getOutputStream(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
            boolean success = false;
            if (inputStream != null && outputStream != null) {
                success = mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream);
            }
            if (success) {
                showMessage(success_string);
                // Now show the link button
                linkButtonLayout.setVisibility(View.VISIBLE);
            } else {
                showMessage(error_string);
            }
        }
    }
    private void addAsLink() {
        // Proceed only if we have a filename
        if (mainActivityInterface!=null && audioFilename!=null && audioFilename.getText()!=null) {

            // If we already have a link audio, show the prompt to confirm replacing it
            if (mainActivityInterface.getSong().getLinkaudio() != null && !mainActivityInterface.getSong().getLinkaudio().isEmpty()) {
                String message = audio_link_string + ": " + overwrite_exisiting_string +" (" +
                        mainActivityInterface.getSong().getLinkaudio() + " -> " + "../OpenSong/Media/"+audioFilename.getText().toString() + ")";
                AreYouSureBottomSheet areYouSureBottomSheet = new AreYouSureBottomSheet("linkaudiorecorder",message, null, "AudioRecorderPopup", null, null);
                areYouSureBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"AreYouSure");

            } else {
                doSaveLinkAudio();
            }
        }
    }
    private void releaseMediaRecorder() {
        if (mediaRecorder!=null) {
            try {
                mediaRecorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mediaRecorder = null;
    }

    // The mediaPlayer (to start and stop the preview playback properly)
    private void prepareMediaPlayer() {
        try {
            stopMediaPlayer();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(tempFilename.getAbsolutePath());
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void mediaPlayerOnPrepared() {
        mediaPlayer.setOnPreparedListener(mediaPlayer -> {
            mediaPlayerIsPrepared = true;
            isPlaying = false;
            startMediaPlayer();
        });
    }
    private void startMediaPlayer() {
        try {
            if (mediaPlayerIsPrepared && (!isPlaying || !mediaPlayer.isPlaying())) {
                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        isPlaying = true;
    }
    private void stopMediaPlayer() {
        try {
            if (isPlaying || mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        isPlaying = false;
    }
    private void resetMediaPlayer() {
        if (!isPlaying) {
            try {
                mediaPlayer.reset();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaPlayerIsPrepared = false;
        }
    }
    private void releaseMediaPlayer() {
        if (!isPlaying) {
            try {
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void doSaveLinkAudio() {
        // We have received permission to overwrite the existing song link audio
        // Write a localised version of the filename to the song and save it
        if (audioFilename!=null && audioFilename.getText()!=null) {
            String localiseFilename = "../OpenSong/Media/" + audioFilename.getText().toString();
            mainActivityInterface.getSong().setLinkaudio(localiseFilename);
            boolean success = mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(), false);
            if (success) {
                showMessage(success_string);
            } else {
                showMessage(error_string);
            }
        } else {
            showMessage(error_string);
        }
    }
    // Close down the popup and completely stop and release all resources
    public void destroyPopup() {
        Log.d(TAG,"destroyPopup() called");
        try {
            stopMediaRecorder();
            releaseMediaRecorder();
            stopMediaPlayer();
            releaseMediaPlayer();
            closeButton = null;
            floatWindow = null;
            if (popupWindow != null) {
                popupWindow.dismiss();
                popupWindow = null;
            }
            mainActivityInterface.removeAudioRecorderPopUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMessage(String message) {
        this.alertMessage = message;
        messageHandler.post(showMessageRunnable);
    }
}
