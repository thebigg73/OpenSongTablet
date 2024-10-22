package com.garethevans.church.opensongtablet.utilities;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.databinding.BottomSheetAudioPlayerBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AudioPlayerBottomSheet extends BottomSheetDialogFragment {

    // This is used to play an audio file that the user has selected using the file picker
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "AudioPlayerBottomSheet";
    private MainActivityInterface mainActivityInterface;
    private BottomSheetAudioPlayerBinding myView;
    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private int songDuration;
    private int[] songTime;
    private boolean isPlaying=false, isPaused=false, isDragging=false,
            playingBeforeDrag=false;
    ScheduledExecutorService scheduledExecutorService;
    Runnable runnable;

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            FrameLayout bottomSheet = ((BottomSheetDialog) dialog1).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setDraggable(false);
            }
        });
        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetAudioPlayerBinding.inflate(inflater,container,false);
        myView.dialogHeading.setClose(this);

        setupViews();

        // Set up the listeners
        setListeners();

        // The timers for checking the play position and seekbar
        setupTimers();

        // Prepare the audio in the mediaPlayer
        setUpMediaPlayer();

        return myView.getRoot();
    }

    private void setupViews() {
        // Disable everything until the mediaPlayer is prepared
        setEnabled(false,false,false,false);
    }

    private void setListeners() {
        myView.audioPlay.setOnClickListener((fab) -> playAudio());
        myView.audioStop.setOnClickListener((fab) -> stopAudio());
        myView.audioPause.setOnClickListener((fab) -> pauseAudio());
        myView.seekBar.addOnChangeListener((slider, value, fromUser) -> updateTime());
        myView.seekBar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
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
        myView.seekBar.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                int[] time = mainActivityInterface.getTimeTools().getMinsSecsFromSecs(Math.round(value));
                return String.format(Locale.getDefault(),"%02d", time[0]) + ":" +
                        String.format(Locale.getDefault(),"%02d", time[1]);
            }
        });
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
                        if (myView != null) {
                            myView.seekBar.setValue(Math.round(mediaPlayer.getCurrentPosition() / 1000f));
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
            if (myView!=null) {
                songDuration = Math.round(mediaPlayer.getDuration()/1000f);
                songTime = mainActivityInterface.getTimeTools().getMinsSecsFromSecs(songDuration);
                myView.seekBar.setValueTo(songDuration);
                updateTime();
                mediaPlayer.seekTo(0);
                setEnabled(true,true,true,true);
            }
        });
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            isPlaying = false;
            isPaused = false;
            myView.seekBar.setValue(myView.seekBar.getValueTo());
            updateTime();
        });
        if (mainActivityInterface.getImportUri()!=null && getContext()!=null) {
            try {
                mediaPlayer.setVolume(1f,1f);
                mediaPlayer.setDataSource(getContext(), mainActivityInterface.getImportUri());
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateTime() {
        int[] current = mainActivityInterface.getTimeTools().getMinsSecsFromSecs(Math.round(mediaPlayer.getCurrentPosition()/1000f));
        myView.seekBar.setHint(String.format(Locale.getDefault(),"%02d", current[0]) + ":" +
                String.format(Locale.getDefault(),"%02d", current[1]) + " / " +
                String.format(Locale.getDefault(),"%02d", songTime[0]) + ":" +
                String.format(Locale.getDefault(),"%02d", songTime[1]));
    }

    private void setEnabled(boolean sliderEnabled, boolean playEnabled,
                            boolean pauseEnabled, boolean stopEnabled) {
        myView.seekBar.setEnabled(sliderEnabled);
        myView.audioPlay.setEnabled(playEnabled);
        myView.audioPause.setEnabled(pauseEnabled);
        myView.audioStop.setEnabled(stopEnabled);
    }

    private void seekAudio() {
        // Move the mediaPlayer to the correct position
        if (!isPlaying) {
            try {
                int valueRequested = Math.round(myView.seekBar.getValue() * 1000f);
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
        setupTimers();
    }

    private void stopAudio() {
        isPlaying = false;
        isPaused = false;
        try {
            mediaPlayer.seekTo(0);
            mediaPlayer.pause();
            myView.seekBar.setValue(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
        setupTimers();
    }
}
