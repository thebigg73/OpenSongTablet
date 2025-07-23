package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.multitrack.MultiTrackPlayer;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.textview.MaterialTextView;

public class TrackSlider extends LinearLayout {

    // This is used as the volume, pan, mute and solo control for the Multitrack player

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "TrackSlider";

    private Slider volumeSlider;
    private Slider panSlider;
    private MaterialTextView trackVolumeTextView;
    private MaterialButton muteButton, soloButton;
    private String trackName="", trackPan="C";
    private int trackNumber=-1, trackVolume=0;
    private boolean trackMute, trackSolo;
    private int buttonTextOnColor, buttonTextOffColor, buttonOnColor, buttonOffColor;
    private MultiTrackPlayer multiTrackPlayer;

    public TrackSlider(Context context) {
        super(context);
        setupViews(context);
    }

    public TrackSlider(Context context, AttributeSet attrs) {
        super(context,attrs);
        setupViews(context);
    }

    public TrackSlider(Context context, MultiTrackPlayer multiTrackPlayer, int trackNumber, String trackName, Integer trackVolume, String trackPan, Boolean trackMute, Boolean trackSolo) {
        super(context);
        this.multiTrackPlayer = multiTrackPlayer;
        this.trackNumber = trackNumber;
        this.trackName = trackName==null ? "" : trackName;
        this.trackVolume = trackVolume==null ? 100 : trackVolume;
        this.trackPan = trackPan==null ? "C" : trackPan;
        this.trackMute = trackMute != null && trackMute;
        this.trackSolo = trackSolo != null && trackSolo;
        setupViews(context);
        setupListeners();
    }

    private void setupViews(Context context) {
        inflate(context, R.layout.view_track_slider, this);

        volumeSlider = findViewById(R.id.volumeSlider);
        panSlider = findViewById(R.id.panSlider);
        MaterialTextView trackNameTextView = findViewById(R.id.trackNameTextView);
        trackVolumeTextView = findViewById(R.id.trackVolumeTextView);
        muteButton = findViewById(R.id.muteButton);
        soloButton = findViewById(R.id.soloButton);

        volumeSlider.setId(View.generateViewId());
        panSlider.setId(View.generateViewId());
        trackNameTextView.setId(View.generateViewId());
        trackVolumeTextView.setId(View.generateViewId());
        muteButton.setId(View.generateViewId());
        soloButton.setId(View.generateViewId());

        buttonTextOnColor = getResources().getColor(R.color.yellow);
        buttonTextOffColor = getResources().getColor(R.color.white);
        buttonOnColor = getResources().getColor(R.color.colorSecondary);
        buttonOffColor = getResources().getColor(R.color.colorAltPrimary);

        trackNameTextView.setText(trackName);
        trackVolumeTextView.setText(String.valueOf(trackVolume));
        volumeSlider.setValueFrom(0);
        volumeSlider.setValueTo(100);
        if (trackVolume<0 || trackVolume>100) {
            trackVolume = 100;
        }
        volumeSlider.setValue(trackVolume);
        panSlider.setValueFrom(0);
        panSlider.setValueTo(2);
        setPanSlider(trackPan);
        updateButtons();
    }

    private void setupListeners() {
        volumeSlider.addOnChangeListener((slider, value, fromUser) -> {
            trackVolume = (int) value;
            if (trackVolumeTextView!=null) {
                trackVolumeTextView.setText(String.valueOf((int)value));
            }
            multiTrackPlayer.setVolume(trackNumber, trackVolume);
        });
        panSlider.addOnChangeListener((slider, value, fromUser) -> {
            trackPan = getPanSliderString((int)value);
            multiTrackPlayer.setPan(trackNumber, trackPan);
        });
        muteButton.setOnClickListener(v -> {
            trackMute = !trackMute;
            if (trackMute) {
                setSolo(false);
            }
            multiTrackPlayer.setSolo(trackNumber,false);
            multiTrackPlayer.setMute(trackNumber, trackMute);
            updateButtons();
        });
        soloButton.setOnClickListener(v -> {
            trackSolo = !trackSolo;
            multiTrackPlayer.setSolo(trackNumber,trackSolo);
            if (trackSolo) {
                setMute(false);
                multiTrackPlayer.setMute(trackNumber, false);
            }
            updateButtons();
        });
    }

    public void setMute(boolean trackMute) {
        this.trackMute = trackMute;
        updateButtons();
    }
    public void setSolo(boolean trackSolo) {
        this.trackSolo = trackSolo;
        if (trackSolo) {
            trackMute = false;
        }
        updateButtons();
    }
    public void setPanSlider(String value) {
        if (value==null) {
            value = "C";
        }
        switch (value) {
            case "L":
                panSlider.setValue(0);
                break;
            case "R":
                panSlider.setValue(2);
                break;
            case "C":
            default:
                panSlider.setValue(1);
                break;
        }
    }
    public String getPanSliderString(int value) {
        if (value==0) {
            return "L";
        } else if (value==2) {
            return "R";
        } else {
            return "C";
        }
    }

    public void updateButtons() {
        muteButton.setTextColor(trackMute ? buttonTextOnColor : buttonTextOffColor);
        muteButton.setBackgroundTintList(trackMute ? ColorStateList.valueOf(buttonOnColor) : ColorStateList.valueOf(buttonOffColor));
        soloButton.setTextColor(trackSolo ? buttonTextOnColor : buttonTextOffColor);
        soloButton.setBackgroundTintList(trackSolo ? ColorStateList.valueOf(buttonOnColor) : ColorStateList.valueOf(buttonOffColor));
        volumeSlider.setEnabled(!trackMute);
    }

    public void addOnSliderTouchListener(Slider.OnSliderTouchListener onSliderTouchListener) {
        volumeSlider.addOnSliderTouchListener(onSliderTouchListener);
    }
    public void addOnChangeListener(Slider.OnChangeListener onChangeListener){
        volumeSlider.addOnChangeListener(onChangeListener);
    }

}
