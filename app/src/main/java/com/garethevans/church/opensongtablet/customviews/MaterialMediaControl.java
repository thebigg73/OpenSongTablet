package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.textview.MaterialTextView;

import java.util.Timer;
import java.util.TimerTask;

public class MaterialMediaControl extends LinearLayout {
    private final Context c;
    private MainActivityInterface mainActivityInterface;
    private MaterialTextView playbackTime, playbackLength;
    private Slider playbackSlider;
    private FloatingActionButton playbackStartStop;
    private MediaPlayer mediaPlayer;
    private Timer timer;
    private TimerTask timerTask;
    private boolean mediaIsOk = false;
    private Drawable startDrawable, stopDrawable;

    public MaterialMediaControl(Context c) {
        super(c);
        this.c = c;
        inflate(c, R.layout.view_media_control, this);
        setupViews(c);
    }

    public MaterialMediaControl(Context c, @Nullable AttributeSet attrs) {
        super(c, attrs);
        this.c = c;
        inflate(c, R.layout.view_media_control, this);
        setupViews(c);
    }

    private void setupViews(Context c) {
        playbackTime = findViewById(R.id.playbackTime);
        playbackLength = findViewById(R.id.playbackLength);
        playbackSlider = findViewById(R.id.playbackSlider);
        playbackStartStop = findViewById(R.id.playbackStartStop);
        playbackStartStop.setOnClickListener(view -> startStop());
        startDrawable = ResourcesCompat.getDrawable(c.getResources(),R.drawable.play,null);
        stopDrawable = ResourcesCompat.getDrawable(c.getResources(),R.drawable.stop,null);
        checkEnabled();
    }

    private int getMediaPosition() {
        int pos = (int)((float)mediaPlayer.getCurrentPosition()/1000f);
        if (pos<0) {
            pos = 0;
        }
        return pos;
    }

    public void setPlaybackTime(int timeSecs) {
        if (timeSecs<0) {
            timeSecs = 0;
        }
        String timeText = mainActivityInterface.getTimeTools().timeFormatFixer(timeSecs);
        playbackTime.setText(timeText);
        if (timeSecs<=playbackSlider.getValueTo()) {
            playbackSlider.setValue(timeSecs);
        }
    }

    public void setPlaybackLength(int timeSecs) {
        if (timeSecs<0) {
            timeSecs = 0;
        }
        String timeText = mainActivityInterface.getTimeTools().timeFormatFixer(timeSecs);
        playbackLength.setText(timeText);
        playbackSlider.setValueTo(timeSecs);
    }

    public int getPlayBackSliderPos() {
        return (int) playbackSlider.getValue();
    }

    public void prepareMediaPlayer(MainActivityInterface mainActivityInterface, Uri uri) {
        // Pass reference for mainActivityInterface
        this.mainActivityInterface = mainActivityInterface;

        // Initialise the times to 0:00 until we load
        mediaIsOk = false;
        if (mediaPlayer!=null) {
            mediaPlayer.release();
        }

        checkEnabled();
        setPlaybackLength(0);
        setPlaybackTime(0);

        // Purge any timer tasks and get them ready
        purgeTimers();
        prepareTimers();

        // Now prepare the media player
        mediaPlayer = new MediaPlayer();
        mediaPlayer.prepareAsync();
        mediaPlayer.setOnPreparedListener(mediaPlayer -> {
            int length = (int)((float)mediaPlayer.getDuration()/1000f);
            setPlaybackLength(length);
            setPlaybackTime(0);
            checkEnabled();
        });
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            timerTask.cancel();
            mediaPlayer.seekTo(0);
            setPlaybackTime(0);
            playbackStartStop.setImageDrawable(startDrawable);
        });
        try {
            mediaPlayer.setDataSource(c, uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startStop() {
        mediaIsOk = checkEnabled();
        if (mediaIsOk && mediaPlayer.isPlaying() && timer!=null && timerTask!=null) {
            // Pause the media and set the button ready to play
            mediaPlayer.pause();
            timer.cancel();
            playbackStartStop.setImageDrawable(startDrawable);
        } else if (mediaIsOk) {
            // Play the media and set the button ready to stop
            // Check we are where we the slide is and if not, move there
            if ((int)(mediaPlayer.getCurrentPosition()/1000f)!=getPlayBackSliderPos() * 1000) {
                mediaPlayer.seekTo(getPlayBackSliderPos() * 1000);
            }
            timer = new Timer();
            if (timerTask!=null) {
                mediaPlayer.start();
                timer.scheduleAtFixedRate(timerTask, 1000, 1000);
                playbackStartStop.setImageDrawable(stopDrawable);
            }
        } else {
            mediaPlayer = new MediaPlayer();
            purgeTimers();
            playbackStartStop.setImageDrawable(startDrawable);
        }
    }

    private void prepareTimers() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                int pos = getMediaPosition();
                setPlaybackTime(pos);
            }
        };
        timer = new Timer();
    }

    private void purgeTimers() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (timer!=null) {
            timer.cancel();
            timer.purge();
        }
    }

    private boolean checkEnabled() {
        boolean enabled = mediaPlayer!=null && mediaPlayer.getDuration()>0;
        playbackSlider.setEnabled(enabled);
        playbackStartStop.setEnabled(enabled);
        return enabled;
    }
}
