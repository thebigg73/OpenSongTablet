package com.garethevans.church.opensongtablet.metronome;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDown;
import com.garethevans.church.opensongtablet.customviews.MyFloatingActionButton;
import com.garethevans.church.opensongtablet.customviews.MyMaterialButton;
import com.garethevans.church.opensongtablet.drummer.DrumCalculations;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

public class MetronomeTapTempo {
    // This class is used to deal with tap tempo
    // This can be called from the MetronomeFragment or the EditSongFeaturesFragment

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "MetronomeTapTempo";
    private MainActivityInterface mainActivityInterface;
    private Context c;

    // The tap tempo views
    private MyMaterialButton tapButton;
    private ExposedDropDown timeSigView;
    private ExposedDropDown beatsView;
    private ExposedDropDown divisionsView;
    private ExposedDropDown tempoView;
    private MyFloatingActionButton playButton;


    // The variables used in the calculations
    private int total_calc_bpm = 0;
    private int total_counts = 0;
    private long old_time = 0;

    // The handlers and runnables
    private Runnable tapTempoRunnableCheck, tapTempoRunnableReset;
    private Handler tapTempoHandlerCheck, tapTempoHandlerReset;

    // A reference to the MetronomeFragment (null if this is initialised from the EditSongFeatures
    private MetronomeFragment metronomeFragment;

    public MetronomeTapTempo(Context c, MetronomeFragment metronomeFragment) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        this.metronomeFragment = metronomeFragment;
    }

    public void initialiseTapTempo(Song thisSong, MyMaterialButton tapButton, ExposedDropDown timeSigView,
                                   ExposedDropDown beatsView, ExposedDropDown divisionsView,
                                   ExposedDropDown tempoView, MyFloatingActionButton playButton,
                                   boolean updateMetronomeDrummerForThisSong) {
        this.tapButton = tapButton;
        this.timeSigView = timeSigView;
        this.beatsView = beatsView;
        this.divisionsView = divisionsView;
        this.tempoView = tempoView;
        this.playButton = playButton;

        if (tapButton!=null) {
            tapButton.setOnClickListener(view -> tapTempo(thisSong,updateMetronomeDrummerForThisSong));
        }
        // Initialise the tapTempo values
        total_calc_bpm = 0;
        total_counts = 0;
        tapTempoRunnableCheck = () -> {
            // This is called after 2 seconds when a tap is initiated
            // Any previous instance is of course cancelled first
            declareTapping(false);
            mainActivityInterface.getThreadPoolExecutor().execute(() -> mainActivityInterface.getMainHandler().post(() -> {
                if (this.tapButton!=null) {
                    this.tapButton.setEnabled(false);
                    this.tapButton.setText(c.getString(R.string.reset));
                    this.tapButton.setBackgroundColor(mainActivityInterface.getPalette().primary);
                }
                // Waited too long, reset count
                total_calc_bpm = 0;
                total_counts = 0;
            }));
            if (tapTempoHandlerReset != null) {
                tapTempoHandlerReset.removeCallbacks(tapTempoRunnableReset);
            } else {
                tapTempoHandlerReset = new Handler();
            }
            tapTempoHandlerReset.postDelayed(tapTempoRunnableReset, 500);
        };
        tapTempoRunnableReset = () -> {
            // Reset the tap tempo timer
            declareTapping(false);
            mainActivityInterface.getThreadPoolExecutor().execute(() -> mainActivityInterface.getMainHandler().post(() -> {
                this.tapButton.setEnabled(true);
                this.tapButton.setText(c.getString(R.string.tap_tempo));
                this.tapButton.setBackgroundColor(mainActivityInterface.getPalette().secondary);
            }));
            // Start the metronome if we are in the metronome fragment
            // Alternatively we are in the EditSong page, do nothing
            if (metronomeFragment!=null) {
                // Update the tempo in the metronomeFragment (to trigger a save now)
                tempoView.setText(tempoView.getText().toString());
                mainActivityInterface.getDrumViewModel().setThisBpm(Integer.parseInt(tempoView.getText().toString()));
                mainActivityInterface.getMainHandler().postDelayed(() -> {
                        if (playButton!=null) {
                            playButton.performClick();
                        }
                }, 1000);
            }
        };
    }

    public void tapTempo(Song thisSong, boolean updateMetronomeDrummerForThisSong) {
        // This function checks the previous tap_tempo time and calculates the bpm
        // Variables for tap tempo
        declareTapping(true);

        // When tapping for compound/complex time signatures
        // They sometimes go in double or triple time
        mainActivityInterface.getDrumViewModel().stopMetronome();

        long new_time = System.currentTimeMillis();
        long time_passed = new_time - old_time;
        int calc_bpm = Math.round((1 / ((float) time_passed / 1000)) * 60);

        // Need to decide on the time sig.
        // If it ends in /2, then double the tempo
        // If it ends in /4, then leave as is
        // If it ends in /8, then half it
        // If it isn't set, set it to default as 4/4
        String timeSig = DrumCalculations.getFixedTimeSignatureString(thisSong.getTimesig(),
                        mainActivityInterface.getDrumViewModel().getMetronome().getMetronomeUseDefaults());
        int[] bits = DrumCalculations.getFixedTimeSignature(timeSig);
        float multiplier = 1f;
        if (bits.length==2 && bits[0]!=-1 && bits[1]!=-1) {
            if (bits[1]==8) {
                multiplier = 1.5f;
            }
            if (beatsView!=null && divisionsView!=null) {
                declareTapping(true);
                Log.d(TAG,"updating the beatsView");
                beatsView.setText(String.valueOf(bits[0]));
                divisionsView.setText(String.valueOf(bits[1]));
            } else if (timeSigView!=null) {
                declareTapping(true);
                timeSigView.setText(timeSig);
            }
            thisSong.setTimesig(timeSig);
        }

        if (time_passed < 1500) {
            total_calc_bpm += calc_bpm;
            total_counts++;
        } else {
            // Waited too long, reset count
            total_calc_bpm = 0;
            total_counts = 0;
        }

        int av_bpm = Math.round((((float) total_calc_bpm / (float) total_counts))*multiplier);

        if (av_bpm < 300 && av_bpm >= 40) {
            declareTapping(true);
            tempoView.setText(String.valueOf(av_bpm));
            thisSong.setTempo(String.valueOf(av_bpm));
            if (updateMetronomeDrummerForThisSong) {
                mainActivityInterface.getDrumViewModel().setThisBpm(av_bpm);
            }

        } else if (av_bpm <40) {
            declareTapping(true);
            tempoView.setText("40");
            thisSong.setTempo("40");
            if (updateMetronomeDrummerForThisSong) {
                mainActivityInterface.getDrumViewModel().setThisBpm(40);
            }
        }  else {
            declareTapping(true);
            tempoView.setText("300");
            thisSong.setTempo("300");
            if (updateMetronomeDrummerForThisSong) {
                mainActivityInterface.getDrumViewModel().setThisBpm(300);
            }
        }

        old_time = new_time;

        // Set a handler to check the button tap.
        // If the counts haven't increased after 1.5 seconds, reset it
        if (tapTempoHandlerCheck!=null) {
            tapTempoHandlerCheck.removeCallbacks(tapTempoRunnableCheck);
        } else {
            tapTempoHandlerCheck = new Handler();
        }
        tapTempoHandlerCheck = new Handler();
        tapTempoHandlerCheck.postDelayed(tapTempoRunnableCheck,1500);
    }

    // Declare we are tapping to the metronomeFragment
    private void declareTapping(boolean tapping) {
        if (metronomeFragment!=null) {
            metronomeFragment.setTapping(tapping);
        }
    }


    // Called when clearing this class from calling fragment
    public void cleanUp() {
        this.tapButton = null;
        this.timeSigView = null;
        this.beatsView = null;
        this.divisionsView = null;
        this.tempoView = null;
        this.playButton = null;

        if (tapTempoHandlerCheck!=null) {
            tapTempoHandlerCheck.removeCallbacks(tapTempoRunnableCheck);
        }
        tapTempoHandlerCheck = null;

        if (tapTempoHandlerReset!=null) {
            tapTempoHandlerReset.removeCallbacks(tapTempoRunnableReset);
        }
        tapTempoHandlerReset = null;
        this.metronomeFragment = null;
        c = null;
        mainActivityInterface = null;
    }
}
