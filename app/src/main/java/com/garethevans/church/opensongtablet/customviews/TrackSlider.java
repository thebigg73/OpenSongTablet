package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.multitrack.MultiTrackPlayer;
import com.garethevans.church.opensongtablet.screensetup.Palette;
import com.google.android.material.slider.Slider;

public class TrackSlider extends LinearLayout {

    // This is used as the volume, pan, mute and solo control for the Multitrack player

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "TrackSlider";

    private Slider volumeSlider;
    private Slider panSlider;
    private MyMaterialSimpleTextView trackVolumeTextView;
    private MyMaterialSimpleTextView muteButton, soloButton, masterBoost1, masterBoost2, masterBoost3;
    private String trackName="", trackPan="C";
    private int trackNumber=-1, trackVolume=0;
    private boolean trackMute, trackSolo;
    private int boost=1;
    private int buttonTextOnColor, buttonTextOffColor;
    private MultiTrackPlayer multiTrackPlayer;
    private View levelIndicator;
    private View muteBox, soloBox, boost1Box, boost2Box, boost3Box;
    private Palette palette;

    // Track -1 is the master slider
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

        palette = new Palette(context);

        LinearLayout mainLayout = findViewById(R.id.mainLayout);
        LinearLayout masterBoostButtons = findViewById(R.id.masterBoostButtons);
        LinearLayout muteAndSoloButtons = findViewById(R.id.muteAndSoloButtons);
        volumeSlider = findViewById(R.id.volumeSlider);
        panSlider = findViewById(R.id.panSlider);
        MyMaterialSimpleTextView trackNameTextView = findViewById(R.id.trackNameTextView);
        trackVolumeTextView = findViewById(R.id.trackVolumeTextView);
        muteButton = findViewById(R.id.muteButton);
        soloButton = findViewById(R.id.soloButton);
        masterBoost1 = findViewById(R.id.masterBoost1);
        masterBoost2 = findViewById(R.id.masterBoost2);
        masterBoost3 = findViewById(R.id.masterBoost3);
        levelIndicator = findViewById(R.id.levelIndicator);
        muteBox = findViewById(R.id.muteBox);
        soloBox = findViewById(R.id.soloBox);
        boost1Box = findViewById(R.id.boost1Box);
        boost2Box = findViewById(R.id.boost2Box);
        boost3Box = findViewById(R.id.boost3Box);

        masterBoostButtons.setId(View.generateViewId());
        muteAndSoloButtons.setId(View.generateViewId());
        mainLayout.setId(View.generateViewId());
        volumeSlider.setId(View.generateViewId());
        panSlider.setId(View.generateViewId());
        trackNameTextView.setId(View.generateViewId());
        trackVolumeTextView.setId(View.generateViewId());
        muteButton.setId(View.generateViewId());
        soloButton.setId(View.generateViewId());
        masterBoost1.setId(View.generateViewId());
        masterBoost2.setId(View.generateViewId());
        masterBoost3.setId(View.generateViewId());
        levelIndicator.setId(View.generateViewId());
        muteBox.setId(View.generateViewId());
        soloBox.setId(View.generateViewId());
        boost1Box.setId(View.generateViewId());
        boost2Box.setId(View.generateViewId());
        boost3Box.setId(View.generateViewId());

        buttonTextOnColor = palette.textColor;
        buttonTextOffColor = palette.hintColor;

        int maxHeight = Math.round(176 * context.getResources().getDisplayMetrics().density);
        levelIndicator.setPivotX(0);
        levelIndicator.setPivotY(maxHeight);
        setLevel(0f);

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

        if (trackNumber==-1) {
            // This is the master track
            // Hide the muteAndSolo layout
            muteAndSoloButtons.setVisibility(View.GONE);
            // Show the boost buttons instead
            masterBoostButtons.setVisibility(View.VISIBLE);
            // Set the background color to red
            trackName = context.getString(R.string.mainfoldername);
            mainLayout.setBackgroundColor(palette.secondary);
            muteButton.setEnabled(false);
            soloButton.setEnabled(false);
        } else {
            // This is a normal track
            // Hide the boost buttons layout
            masterBoostButtons.setVisibility(View.GONE);
            // Show the mute and solo buttons instead
            muteAndSoloButtons.setVisibility(View.VISIBLE);
            masterBoost1.setEnabled(false);
            masterBoost2.setEnabled(false);
            masterBoost3.setEnabled(false);
        }
        trackNameTextView.setText(trackName);
        panSlider.setTrackTintList(ColorStateList.valueOf(palette.primaryVariant));
        panSlider.setThumbTintList(ColorStateList.valueOf(palette.secondaryVariant));
        panSlider.setTickTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        volumeSlider.setTrackTintList(ColorStateList.valueOf(palette.primaryVariant));
        volumeSlider.setThumbTintList(ColorStateList.valueOf(palette.secondaryVariant));
        volumeSlider.setTickTintList(ColorStateList.valueOf(Color.TRANSPARENT));

        Drawable box = AppCompatResources.getDrawable(context,R.drawable.rounded_box);
        if (box!=null) {
            DrawableCompat.setTint(box.mutate(), palette.secondary);
            muteBox.setBackground(box);
            soloBox.setBackground(box);
            boost1Box.setBackground(box);
            boost2Box.setBackground(box);
            boost3Box.setBackground(box);
        }
        levelIndicator.setBackgroundColor(palette.secondary);
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
        masterBoost1.setOnClickListener(v -> {
            boost = 1;
            multiTrackPlayer.setMasterGainBoost(1f);
            updateButtons();
        });
        masterBoost2.setOnClickListener(v -> {
            boost = 2;
            multiTrackPlayer.setMasterGainBoost(2f);
            updateButtons();
        });
        masterBoost3.setOnClickListener(v -> {
            boost = 3;
            multiTrackPlayer.setMasterGainBoost(3f);
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
        soloButton.setTextColor(trackSolo ? buttonTextOnColor : buttonTextOffColor);
        masterBoost1.setTextColor(boost==1 ? buttonTextOnColor : buttonTextOffColor);
        masterBoost2.setTextColor(boost==2 ? buttonTextOnColor : buttonTextOffColor);
        masterBoost3.setTextColor(boost==3 ? buttonTextOnColor : buttonTextOffColor);
        muteBox.setVisibility(trackMute ? View.VISIBLE:View.INVISIBLE);
        soloBox.setVisibility(trackSolo ? View.VISIBLE:View.INVISIBLE);
        volumeSlider.setEnabled(!trackMute);
    }
    public String getTrackName() {
        return trackName;
    }

    public void setLevel(float level) {
        if (levelIndicator!=null) {
            levelIndicator.post(() -> {
                if (levelIndicator != null) {
                    levelIndicator.animate().scaleY(level).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(100);
                }
            });
        }
    }
}
