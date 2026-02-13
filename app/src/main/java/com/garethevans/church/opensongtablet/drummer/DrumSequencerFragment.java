package com.garethevans.church.opensongtablet.drummer;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyMaterialSimpleTextView;
import com.garethevans.church.opensongtablet.databinding.SettingsDrumSequencerBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class DrumSequencerFragment extends Fragment {

    private final String TAG = "DrumSequencerFragment";
    private final String[] instruments = {"Kick", "Snare", "HiHat", "HiHatOpen", "TomLo", "TomMid", "TomHi", "Crash", "Ride", "RimShot"};
    private MainActivityInterface mainActivityInterface;
    private SettingsDrumSequencerBinding myView;
    private SequencerAdapter adapter;
    private Drawable start, stop;
    private String drummer_string="", drummer_website="";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = SettingsDrumSequencerBinding.inflate(inflater, container, false);

        // Check the song has been set up
        mainActivityInterface.getDrumViewModel().prepareSongValues(mainActivityInterface.getSong());

        // Stop the drummer from playing as we are about to potentially edit it
        mainActivityInterface.getDrumViewModel().stopDrummer();

        // Prepare the strings
        prepareStrings();

        // Update the title
        mainActivityInterface.updateToolbar(drummer_string);
        mainActivityInterface.updateToolbarHelp(drummer_website);

        // Set up the sequencer views
        setupIcons();
        setupLabels();
        setupBeatIndicators();
        setupGrid();
        setupListeners();

        // Link the ViewModel's step to the Adapter's playhead
        mainActivityInterface.getDrumViewModel().getCurrentStep().observe(getViewLifecycleOwner(), totalSteps -> {
            if (adapter != null && totalSteps != null) {
                int stepsPerBar = mainActivityInterface.getDrumViewModel().getThisStepsPerBar();
                int stepInBar = totalSteps % stepsPerBar;

                // This triggers the partial update in the Adapter
                adapter.updatePlayhead(stepInBar);
            }
        });

        // Observe the active section to switch the grid view automatically
        mainActivityInterface.getDrumViewModel().getActiveSection().observe(getViewLifecycleOwner(), section -> {
            if (adapter != null && section != null) {
                adapter.setSection(section);
            }
        });

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            drummer_string = getString(R.string.drummer);
            drummer_website = getString(R.string.website_drummer);
        }
    }
    private void setupIcons() {
        if (getContext() != null) {
            stop = ContextCompat.getDrawable(getContext(), R.drawable.stop);
            start = ContextCompat.getDrawable(getContext(), R.drawable.play);
            stop.setTintList(ColorStateList.valueOf(mainActivityInterface.getPalette().textColor));
            stop.setTintList(ColorStateList.valueOf(mainActivityInterface.getPalette().textColor));
        }
    }

    private void setupLabels() {
        if (getContext() != null) {
            myView.instrumentLabelContainer.removeAllViews();

            // 1. ADD A SPACER at the top to account for the beat indicators
            // This spacer must match the height of your beatIndicatorContainer
            View spacer = new View(getContext());
            // Assuming beatIndicatorContainer is ~24-30dp, adjust height to match exactly
            int spacerHeight = dpToPx(24);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, spacerHeight));
            myView.instrumentLabelContainer.addView(spacer);

            // 2. Add instrument names as before

            for (int i = 0; i < mainActivityInterface.getDrumViewModel().getDrumSoundManager().getKit().getDrumParts().size(); i++) {
                String partName = mainActivityInterface.getDrumViewModel().getDrumSoundManager().getKit().getDrumParts().get(i).getPartName();
                String translation = mainActivityInterface.getDrumViewModel().getDrumSoundManager().getKit().getDrumParts().get(i).getPartTranslation();

                MyMaterialSimpleTextView tv = new MyMaterialSimpleTextView(getContext());
                tv.setText(translation);
                tv.setTag(partName);
                tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
                tv.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(42))); // Match item height
                tv.setPadding(0, 0, 16, 0);
                myView.instrumentLabelContainer.addView(tv);
            }
        }
    }

    private void setupBeatIndicators() {
        myView.beatIndicatorContainer.removeAllViews();

        int totalBeats = mainActivityInterface.getDrumViewModel().getThisBeats();
        int stepsPerPulse = mainActivityInterface.getDrumViewModel().getThisStepsPerPulse();

        // Each "step" in your grid is 40dp wide + 2dp margin (from your xml)
        int stepWidthPx = dpToPx(42);

        if (getContext() != null) {
            for (int i = 1; i <= totalBeats; i++) {
                MyMaterialSimpleTextView tv = new MyMaterialSimpleTextView(getContext());
                tv.setText(String.valueOf(i));
                tv.setTextColor(mainActivityInterface.getPalette().textColor);
                tv.setGravity(Gravity.CENTER);

                // The width of the beat indicator should span all steps in that beat
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        stepWidthPx * stepsPerPulse,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                tv.setLayoutParams(params);

                myView.beatIndicatorContainer.addView(tv);
            }
        }
    }

    private void setupGrid() {
        int steps = mainActivityInterface.getDrumViewModel().getThisStepsPerBar();
        myView.sequencerRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), steps));

        // 1. Tell RV that item bounds don't change (HUGE performance boost)
        myView.sequencerRecyclerView.setHasFixedSize(true);

        // 2. Disable all animations. Fading between colors uses massive CPU.
        if (myView.sequencerRecyclerView.getItemAnimator() instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) myView.sequencerRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        }

        if (getContext() != null) {
            adapter = new SequencerAdapter(getContext());
            myView.sequencerRecyclerView.setAdapter(adapter);
        }
    }

    private void setupListeners() {
        myView.btnPlayStop.setOnClickListener(view -> mainActivityInterface.getDrumViewModel().toggleDrummer());
        myView.btnFill.setOnClickListener(view -> mainActivityInterface.getDrumViewModel().drummerFill());
        myView.btnTransition.setOnClickListener(view -> mainActivityInterface.getDrumViewModel().drummerTransition());

        // Listen for variation changes
        myView.variationGroup.setOnCheckedChangeListener((r, id) -> {
            if (id == R.id.radioMain) adapter.setVariation(0);
            else if (id == R.id.radioFull) adapter.setVariation(1);
            else if (id == R.id.radioHalf) adapter.setVariation(2);
        });

        /*myView.btnClearPattern.setOnClickListener(view -> {
            DrumPatternJson pattern = mainActivityInterface.getDrumViewModel().getDrumPatternJson();
            if (pattern != null && adapter != null) {
                int variation = adapter.getCurrentVariation();
                Map<String, int[]> targetMap;
                if (variation == 1) targetMap = pattern.fillFullPattern;
                else if (variation == 2) targetMap = pattern.fillHalfPattern;
                else targetMap = pattern.mainPattern;

                for (int[] track : targetMap.values()) {
                    java.util.Arrays.fill(track, 0);
                }
                adapter.notifyDataSetChanged();
            }
        });*/
        myView.btnResetToDefault.setOnClickListener(view -> {
            DrumPatternJson pattern = mainActivityInterface.getDrumViewModel().getDrumPatternJson();
            if (pattern != null) {
                // Force the builder to overwrite the current live pattern with defaults
                DrumPatternBuilder.buildStandardPattern(
                        pattern,
                        mainActivityInterface.getDrumViewModel().getThisBeats(),
                        mainActivityInterface.getDrumViewModel().getThisDivisions(),
                        mainActivityInterface.getDrumViewModel().getThisStepsPerPulse()
                );
                adapter.notifyDataSetChanged();
            }
        });

        myView.btnSavePattern.setOnClickListener(view -> {
            // Show a quick dialog to ask for the name
            Log.d(TAG, "TODO showSaveDialog()");
        });
    }

    // Call this whenever the "Feel" (Triplet/Straight) changes
    private void updateGridDimensions() {
        int steps = mainActivityInterface.getDrumViewModel().getThisStepsPerBar();
        // If you don't update the span count, the items will wrap incorrectly
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), steps);
        myView.sequencerRecyclerView.setLayoutManager(layoutManager);

        // Refresh the adapter data
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}