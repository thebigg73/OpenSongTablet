package com.garethevans.church.opensongtablet.utilities;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.core.content.ContextCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.DialogHeader;
import com.garethevans.church.opensongtablet.customviews.FloatWindow;
import com.garethevans.church.opensongtablet.customviews.MyMaterialEditText;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    private final float pageButtonAlpha, recordingAlpha;
    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private PopupWindow popupWindow;
    private FloatingActionButton closeButton, previewButton, deleteButton, saveButton;
    private LinearLayout dealWithAudioLayout;
    private ExtendedFloatingActionButton recordButton;
    private FloatWindow floatWindow;
    private MediaRecorder mediaRecorder;
    private boolean isRecording = false, isPlaying = false, mediaPlayerIsPrepared = false;
    private File tempFilename;
    private MyMaterialEditText audioFilename;
    private final String fileExtension = ".m4a";
    final int bitDepth = 16;
    final int samplingRate = 44100;
    final int encodingBitrate = samplingRate * bitDepth;


    // Initialise the popup class
    public AudioRecorderPopUp(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        posX = 0;
        posY = (int) ((float) mainActivityInterface.getToolbar().getActionBarHeight(mainActivityInterface.needActionBar()) * 1.2f);
        recordStart = ContextCompat.getDrawable(c, R.drawable.record);
        recordStop = ContextCompat.getDrawable(c, R.drawable.stop);
        colorInactive = ContextCompat.getColor(c, R.color.colorAltSecondary);
        colorActive = ContextCompat.getColor(c, R.color.red);
        pageButtonAlpha = mainActivityInterface.getMyThemeColors().getPageButtonsSplitAlpha();
        recordingAlpha = Math.min(pageButtonAlpha, 0.7f);
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

        // The main layout (FloatWindow is just a custom linearlayout where I've overridden the performclick
        floatWindow = new FloatWindow(c);
        floatWindow.setAlpha(pageButtonAlpha);

        View myView = View.inflate(c, R.layout.view_audio_recorder_popup, null);
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
    }

    // The mediaRecorder (to start and stop the recording properly)
    private void startRecording() {
        startMediaRecorder();
        recordButton.setIcon(recordStop);
        recordButton.setText(c.getString(R.string.stop));
        recordButton.setBackgroundTintList(ColorStateList.valueOf(colorActive));
        floatWindow.setAlpha(recordingAlpha);
    }
    private void stopRecording() {
        stopMediaRecorder();
        recordButton.setIcon(recordStart);
        recordButton.setText(c.getString(R.string.record));
        recordButton.setBackgroundTintList(ColorStateList.valueOf(colorInactive));
        floatWindow.setAlpha(pageButtonAlpha);

        recordButton.setVisibility(View.GONE);
        audioFilename.setText(tempFilename.getName());
        dealWithAudioLayout.setVisibility(View.VISIBLE);
    }
    private void stopMediaRecorder() {
        if (mediaRecorder!=null) {
            if (isRecording) {
                try {
                    mediaRecorder.stop();
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
                mainActivityInterface.getShowToast().success();
            } else {
                mainActivityInterface.getShowToast().error();
            }
        }
    }
    private void releaseMediaRecorder() {
        try {
            mediaRecorder.release();
        } catch (Exception e) {
            e.printStackTrace();
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

    // Close down the popup and completely stop and release all resources
    public void destroyPopup() {
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

}
