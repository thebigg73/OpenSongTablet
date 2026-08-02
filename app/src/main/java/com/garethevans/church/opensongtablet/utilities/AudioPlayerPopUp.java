package com.garethevans.church.opensongtablet.utilities;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.DialogHeader;
import com.garethevans.church.opensongtablet.customviews.FloatWindow;
import com.garethevans.church.opensongtablet.customviews.MyFloatingActionButton;
import com.garethevans.church.opensongtablet.customviews.MyMaterialSlider;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.screensetup.Palette;
import com.google.android.material.slider.Slider;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AudioPlayerPopUp {

    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private int posX, posY;
    private final float pageButtonAlpha, playingAlpha;
    private boolean isPlaying = false, isPaused = false, mediaPlayerIsPrepared = false,
            isDragging = false, playingBeforeDrag = false, minimised = false;
    private PopupWindow popupWindow;
    private FloatWindow floatWindow;
    private MyFloatingActionButton closeButton, minimiseButton, playPauseButton, stopButton;
    private MyMaterialSlider seekBar;
    private LinearLayout playContent;
    private String web_help;
    private ScheduledExecutorService scheduledExecutorService;
    private Runnable runnable;
    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private int songDuration;
    private int[] songTime;
    private Drawable maximiseDrawable, minimiseDrawable, playDrawable, pauseDrawable;

    // Initialise the popup class
    public AudioPlayerPopUp(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        posX = 0;
        posY = (int) ((float) mainActivityInterface.getToolbar().getActionBarHeight(mainActivityInterface.needActionBar()) * 1.2f);
        pageButtonAlpha = mainActivityInterface.getMyThemeColors().getPageButtonAlpha();
        playingAlpha = Math.min(pageButtonAlpha, 0.7f);
        maximiseDrawable = VectorDrawableCompat.create(c.getResources(), R.drawable.maximise, c.getTheme());
        minimiseDrawable = VectorDrawableCompat.create(c.getResources(), R.drawable.minimise, c.getTheme());
        playDrawable = VectorDrawableCompat.create(c.getResources(), R.drawable.play, c.getTheme());
        pauseDrawable = VectorDrawableCompat.create(c.getResources(), R.drawable.pause, c.getTheme());
        prepareStrings();
        setUpMediaPlayer();
    }

    // The views and listeners for the popup
    public void floatPlayer(View viewHolder) {
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

        View myView = View.inflate(c, R.layout.view_audio_player_popup, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //myView.findViewById(R.id.layout).setBackgroundTintList(ColorStateList.valueOf(mainActivityInterface.getPalette().secondary));
            myView.findViewById(R.id.label).setBackgroundTintList(new ColorStateList(
                    new int[][]{new int[0]},
                    new int[]{mainActivityInterface.getPalette().secondary}
            ));
        } else {
            myView.findViewById(R.id.layout).setBackgroundColor(mainActivityInterface.getPalette().secondary);
        }
        floatWindow.addView(myView);

        DialogHeader dialogHeader = myView.findViewById(R.id.dialogHeader);
        dialogHeader.setText(c.getString(R.string.audio_player));
        dialogHeader.setWebHelp(mainActivityInterface, c.getString(R.string.website_audio_player));
        dialogHeader.showMinimiseButton(true);
        minimiseButton = dialogHeader.getMinimiseButton();
        closeButton = dialogHeader.getCloseButton();
        playPauseButton = myView.findViewById(R.id.playPauseButton);
        stopButton = myView.findViewById(R.id.stopButton);
        playContent = myView.findViewById(R.id.playContent);
        seekBar = myView.findViewById(R.id.seekBar);

        try {
            dialogHeader.findViewById(R.id.headerLayout).setPadding(8,0,8,0);
            seekBar.hideText();
            seekBar.setSliderPadding(8,0,8,0);
            playContent.setPadding(8,0,8,0);

        } catch (Exception e) {
            e.printStackTrace();
        }
        // Update the palette colours for the seekbar and buttons
        Palette tempPalette = new Palette(c);
        tempPalette.secondaryVariant = mainActivityInterface.getPalette().textColor;
        tempPalette.secondary = mainActivityInterface.getPalette().hintColor;
        seekBar.setPalette(tempPalette);

        tempPalette.secondary = mainActivityInterface.getPalette().secondaryVariant;
        playPauseButton.setPalette(tempPalette);
        stopButton.setPalette(tempPalette);


        // Disable everything until the mediaPlayer is prepared
        setEnabled(false);

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


    private void prepareStrings() {
        if (c!=null) {
            web_help = c.getString(R.string.website_audio_player);
        }
    }

    private void setListeners() {
        playPauseButton.setOnClickListener((fab) -> {
            if (isPlaying) {
                pauseAudio();
            } else {
                playAudio();
            }
        });
        stopButton.setOnClickListener((fab) -> stopAudio());
        seekBar.addOnChangeListener((slider, value, fromUser) -> updateTime());
        seekBar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                isDragging = true;
                playingBeforeDrag = isPlaying;
                pauseAudio();
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                isDragging = false;
                seekAudio();
                updateTime();
                if (playingBeforeDrag) {
                    playAudio();
                }
                playingBeforeDrag = false;
            }
        });
        seekBar.setLabelFormatter(value -> {
            int[] time = mainActivityInterface.getTimeTools().getMinsSecsFromSecs(Math.round(value));
            return String.format(Locale.getDefault(),"%02d", time[0]) + ":" +
                    String.format(Locale.getDefault(),"%02d", time[1]);
        });
        minimiseButton.setOnClickListener(view -> minimiseAction());
        closeButton.setOnClickListener(view -> destroyPopup());
    }

    private void setupTimers() {
        // If we are playing, we need to start the timer service
        if (isPlaying) {
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            runnable = () -> {
                // Update the seekbar to match the mediaPlayer position
                // Only do this while we are playing and not dragging the slider
                if (isPlaying && !isDragging) {
                    mainActivityInterface.getMainHandler().post(() -> {
                        if (seekBar != null) {
                            seekBar.setValue(Math.round(mediaPlayer.getCurrentPosition() / 1000f));
                            updateTime();
                        }
                    });
                }
            };
            scheduledExecutorService.scheduleWithFixedDelay(runnable, 0, 200, TimeUnit.MILLISECONDS);
        } else {
            if (scheduledExecutorService != null) {
                scheduledExecutorService.shutdown();
            }
        }
    }

    private void setUpMediaPlayer() {
        mediaPlayer.setOnPreparedListener(mediaPlayer -> {
            if (seekBar!=null) {
                songDuration = Math.round(mediaPlayer.getDuration()/1000f);
                songTime = mainActivityInterface.getTimeTools().getMinsSecsFromSecs(songDuration);
                seekBar.setValueTo(songDuration);
                updateTime();
                mediaPlayer.seekTo(0);
                setEnabled(true);
            }
        });
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            isPlaying = false;
            isPaused = false;
            seekBar.setValue(seekBar.getValueTo());
            updateTime();
        });
        if (mainActivityInterface.getImportUri()!=null && c!=null) {
            try {
                mediaPlayer.setVolume(1f,1f);
                mediaPlayer.setDataSource(c, mainActivityInterface.getImportUri());
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateTime() {
        int[] current = mainActivityInterface.getTimeTools().getMinsSecsFromSecs(Math.round(mediaPlayer.getCurrentPosition()/1000f));
        seekBar.setHint(String.format(Locale.getDefault(),"%02d", current[0]) + ":" +
                String.format(Locale.getDefault(),"%02d", current[1]) + " / " +
                String.format(Locale.getDefault(),"%02d", songTime[0]) + ":" +
                String.format(Locale.getDefault(),"%02d", songTime[1]));
    }

    private void setEnabled(boolean enabled) {
        seekBar.setEnabled(enabled);
        playPauseButton.setEnabled(enabled);
        stopButton.setEnabled(enabled);
    }

    private void seekAudio() {
        // Move the mediaPlayer to the correct position
        if (!isPlaying) {
            try {
                int valueRequested = Math.round(seekBar.getValue() * 1000f);
                if (valueRequested>mediaPlayer.getDuration()) {
                    valueRequested = mediaPlayer.getDuration();
                }
                mediaPlayer.seekTo(valueRequested);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void playAudio() {
        // Play the audio
        try {
            mediaPlayer.start();
            isPlaying = true;
            isPaused = false;
        } catch (Exception e) {
            e.printStackTrace();
            isPlaying = false;
            isPaused = false;
        }
        playPauseButton.setImageDrawable(isPlaying ? pauseDrawable : playDrawable);
        floatWindow.setAlpha(isPlaying ? playingAlpha : pageButtonAlpha);

        setupTimers();
    }

    private void pauseAudio() {
        if (isPaused) {
            isPaused = false;
            playAudio();
        } else if (isPlaying) {
            isPlaying = false;
            try {
                mediaPlayer.pause();
                isPaused = true;
            } catch (Exception e) {
                e.printStackTrace();
                isPaused = false;
            }
        }
        playPauseButton.setImageDrawable(isPlaying ? pauseDrawable : playDrawable);
        floatWindow.setAlpha(isPlaying ? playingAlpha : pageButtonAlpha);

        setupTimers();
    }

    private void stopAudio() {
        isPlaying = false;
        isPaused = false;
        try {
            mediaPlayer.seekTo(0);
            mediaPlayer.pause();
            seekBar.setValue(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
        playPauseButton.setImageDrawable(playDrawable);
        floatWindow.setAlpha(pageButtonAlpha);

        setupTimers();
    }

    private void minimiseAction() {
        // This is only available once we have initialised the sliders
        mainActivityInterface.getMainHandler().post(() -> {
            minimised = !minimised;
            floatWindow.setAlpha(minimised ? playingAlpha:pageButtonAlpha);
            minimiseButton.setImageDrawable(minimised ? maximiseDrawable:minimiseDrawable);
            playContent.setVisibility(minimised ? View.GONE:View.VISIBLE);
        });
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
            stopAudio();
            releaseMediaPlayer();
            closeButton = null;
            floatWindow = null;
            seekBar = null;
            playPauseButton = null;
            stopButton = null;
            minimiseButton = null;
            if (popupWindow != null) {
                popupWindow.dismiss();
                popupWindow = null;
            }
            mainActivityInterface.removeAudioPlayerPopUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
