package com.garethevans.church.opensongtablet.utilities;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.util.Log;
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

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "AudioRecorderPopUp";
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private PopupWindow popupWindow;
    private FloatingActionButton closeButton, webHelpButton, previewButton,
        deleteButton, saveButton;
    private LinearLayout dealWithAudioLayout;
    private ExtendedFloatingActionButton recordButton;
    private FloatWindow floatWindow;
    private final int posX;
    private final int posY;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;
    private final Drawable recordStart;
    private final Drawable recordStop;
    private final int colorInactive, colorActive;
    private final float pageButtonAlpha;
    private float recordingAlpha;
    private File tempFilename;
    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private boolean mediaPlayerSourceSet = false;
    private MyMaterialEditText audioFilename;

    public AudioRecorderPopUp(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        posX = 0;
        posY = (int) ((float) mainActivityInterface.getToolbar().getActionBarHeight(mainActivityInterface.needActionBar())*1.2f);
        recordStart = ContextCompat.getDrawable(c, R.drawable.record);
        recordStop = ContextCompat.getDrawable(c, R.drawable.stop);
        colorInactive = ContextCompat.getColor(c, R.color.colorAltSecondary);
        colorActive = ContextCompat.getColor(c, R.color.red);
        pageButtonAlpha = mainActivityInterface.getMyThemeColors().getPageButtonsSplitAlpha();
        recordingAlpha = 0.7f;
        recordingAlpha = Math.min(pageButtonAlpha,recordingAlpha);
        mediaPlayer.setOnPreparedListener(mediaPlayer -> {
            mediaPlayerSourceSet = true;
            try {
                mediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void floatRecorder(View viewHolder) {
        // If the popup is showing already, dismiss it
        if (popupWindow!=null && popupWindow.isShowing()) {
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
        dialogHeader.setClose(this);

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

    private void setListeners() {
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
            if (tempFilename!=null && tempFilename.exists()) {
                if (!mediaPlayerSourceSet) {
                    try {
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(tempFilename.getAbsolutePath());
                        mediaPlayer.prepareAsync();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    try {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                        } else {
                            mediaPlayer.start();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        deleteButton.setOnClickListener(view -> {
            if (tempFilename!=null) {
                if (tempFilename.delete()) {
                    dealWithAudioLayout.setVisibility(View.GONE);
                    recordButton.setVisibility(View.VISIBLE);
                    try {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        mediaPlayer.reset();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            mediaPlayerSourceSet = false;
        });
        saveButton.setOnClickListener(view -> {
            Log.d(TAG,"doSave");
            // Get the filename, but make sure it ends with .3gp
            if (audioFilename!=null && audioFilename.getText()!=null) {
                String filename = audioFilename.getText().toString();
                if (!filename.endsWith(".3gp")) {
                    filename = filename + ".3gp";
                }
                Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Media","",filename);
                mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true,
                        uri,null,"Media","",filename);
                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    inputStream = new FileInputStream(tempFilename);
                    outputStream = mainActivityInterface.getStorageAccess().getOutputStream(uri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                boolean success = false;
                if (inputStream!=null && outputStream!=null) {
                    success = mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream);
                }
                if (success) {
                    mainActivityInterface.getShowToast().success();
                } else {
                    mainActivityInterface.getShowToast().error();
                }

            }

        });
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

    private void startRecording() {
        isRecording = true;
        recordButton.setIcon(recordStop);
        recordButton.setText(c.getString(R.string.stop));
        recordButton.setBackgroundTintList(ColorStateList.valueOf(colorActive));
        tempFilename = mainActivityInterface.getStorageAccess().getAppSpecificFile("Audio", "", "OpenSongAudio.3gp");
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(tempFilename.getAbsolutePath());
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        floatWindow.setAlpha(recordingAlpha);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        isRecording = false;
        recordButton.setIcon(recordStart);
        recordButton.setText(c.getString(R.string.start));
        recordButton.setBackgroundTintList(ColorStateList.valueOf(colorInactive));
        floatWindow.setAlpha(pageButtonAlpha);

        if (mediaRecorder != null) {
            try {
                mediaRecorder.release();
                mediaRecorder = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        recordButton.setVisibility(View.GONE);
        audioFilename.setText(tempFilename.getName());
        dealWithAudioLayout.setVisibility(View.VISIBLE);
    }

    public void destroyPopup() {
        try {
            if (closeButton != null) {
                closeButton = null;
            }
            if (webHelpButton != null) {
                webHelpButton = null;
            }

            if (floatWindow != null) {
                floatWindow = null;
            }
            if (popupWindow != null) {
                popupWindow.dismiss();
                popupWindow = null;
            }
            mediaRecorder = null;
            mainActivityInterface.removeAudioRecorderPopUp();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
