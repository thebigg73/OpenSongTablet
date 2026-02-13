package com.garethevans.church.opensongtablet.drummer;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.List;
import java.util.Map;

public class SequencerAdapter extends RecyclerView.Adapter<DrumViewGridHolder> {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "SequencerAdapter";
    private int currentVariation = 0; // 0: Main, 1: Full, 2: Half
    private int playheadStep = -1;
    private final MainActivityInterface mainActivityInterface;
    private final String velocity_string, off_string;
    private final int[] velocityColors;
    private DrumSection currentSection = DrumSection.MAIN;

    public SequencerAdapter(Context c) {
        this.mainActivityInterface = (MainActivityInterface) c;
        velocity_string = c.getString(R.string.midi_velocity);
        off_string = c.getString(R.string.off);

        // Lets set up the colours for the velocities and tint them according to the palette
        // 0 -> primaryVariant (changed programmatically to secondary if on the beat)
        // Ghost note: 50 -> purple
        // Quiet note: 75 -> yellow
        // Normal note: 100 -> orange
        // Loud note: 127 -> red

        velocityColors = new int[]{
                ColorUtils.blendARGB(Color.parseColor("#AC37C0"), mainActivityInterface.getPalette().primary, 0.5f),  // Purple
                ColorUtils.blendARGB(Color.parseColor("#FFFF00"), mainActivityInterface.getPalette().primary, 0.5f),  // Yellow
                ColorUtils.blendARGB(Color.parseColor("#FF9800"), mainActivityInterface.getPalette().primary, 0.5f),  // Orange
                ColorUtils.blendARGB(Color.parseColor("#FF0000"), mainActivityInterface.getPalette().primary, 0.5f)}; // Red
    }

    private int getVelocityColor(int velocity) {
        if (velocity == 127) {
                return velocityColors[3]; // Red (Accent)
        } else if (velocity >= 100) {
                return velocityColors[2]; // Orange (Normal)
        } else if (velocity >= 75) {
                return velocityColors[1]; // Yellow (Soft)
        } else if (velocity >= 50) {
                return velocityColors[0]; // Purple (Soft)
        } else {
            // By default 0 = primary variant, but if on beat, will get changed to secondary
            return mainActivityInterface.getPalette().primaryVariant;
        }
    }

    // Separate the visual logic from the listener logic
    private void renderStep(DrumViewGridHolder holder, int position) {

        DrumPatternJson pattern = mainActivityInterface.getDrumViewModel().getCurrentPattern().getValue();
        Map<String, int[]> activeMap = getActiveMap(pattern);

        if (activeMap == null) return;

        int stepsPerBar = mainActivityInterface.getDrumViewModel().getThisStepsPerBar();
        int instrumentIndex = position / stepsPerBar;
        int stepIndex = position % stepsPerBar;

        // Get the instrument name list from your kit to match the row index
        String instrumentName = mainActivityInterface.getDrumViewModel()
                .getDrumSoundManager().getKit().getDrumParts()
                .get(instrumentIndex).getPartName();

        int[] track = activeMap.get(instrumentName);
        int velocity = (track != null) ? track[stepIndex] : 0;

        // 1. Update Background (Highlighting & Beats)
        updateStepHighlight(holder, position, velocity);

        // 2. Update Note Visualization (This is what's missing!)
        if (velocity > 0) {
            // Set your "Active Note" color based on velocity
            holder.itemView.setBackgroundColor(getVelocityColor(velocity));
        }
    }

    private void setupListeners(DrumViewGridHolder holder, int position) {
        int stepsPerBar = mainActivityInterface.getDrumViewModel().getThisStepsPerBar();
        int instrumentIndex = position / stepsPerBar;
        int stepIndex = position % stepsPerBar;
        String instrument = mainActivityInterface.getDrumViewModel().getDrumSoundManager().getKit().getDrumParts().get(instrumentIndex).getPartName();

        boolean isRestricted = (currentVariation == 2 && stepIndex < (stepsPerBar / 2));

        if (isRestricted) {
            holder.itemView.setOnClickListener(null);
        } else {
            holder.itemView.setOnLongClickListener(view -> {
                DrumPatternJson pattern = mainActivityInterface.getDrumViewModel().getDrumPatternJson();
                Map<String, int[]> activeMap = getActiveMap(pattern); // Update return type to int[]
                int[] trackData = activeMap.get(instrument);

                String message = off_string;

                if (message != null) {
                    mainActivityInterface.getShowToast().doIt(message);
                }

                if (trackData != null) {
                    trackData[stepIndex] = 0;
                }

                notifyItemChanged(position);

                return true;
            });
            holder.itemView.setOnClickListener(v -> {
                DrumPatternJson pattern = mainActivityInterface.getDrumViewModel().getDrumPatternJson();
                Map<String, int[]> activeMap = getActiveMap(pattern); // Update return type to int[]
                int[] trackData = activeMap.get(instrument);

                if (trackData != null && stepIndex < trackData.length) {
                    int currentVelo = trackData[stepIndex];

                    // Cycle: 0 -> 50 -> 75 -> 100 -> 127 -> 0
                    int nextVelo = 0;
                    if (currentVelo == 0) {
                        nextVelo = 50;
                    } else if (currentVelo == 50) {
                        nextVelo = 75;
                    } else if (currentVelo == 75) {
                        nextVelo = 100;
                    } else if (currentVelo == 100) {
                        nextVelo = 127;
                    }

                    String message = off_string;
                    if (nextVelo != 0) {
                        message = velocity_string + ": " + nextVelo;
                    }

                    if (message != null) {
                        mainActivityInterface.getShowToast().doIt(message);
                    }

                    trackData[stepIndex] = nextVelo;

                    // Play sound at the selected velocity for feedback
                    if (nextVelo > 0) {
                        mainActivityInterface.getDrumViewModel().getDrumSoundManager().playDrum(instrument, nextVelo);
                    }
                    notifyItemChanged(position);
                }
            });
        }
    }

    @NonNull
    @Override
    public DrumViewGridHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_sequencer_step, parent, false);
        return new DrumViewGridHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DrumViewGridHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            for (Object payload : payloads) {
                if (payload.equals("PLAYHEAD")) {
                    // Only update the background color/highlight
                    renderStep(holder,position);
                }
            }
        } else {
            // Standard full render for initial display or data changes
            renderStep(holder, position);
            setupListeners(holder, position);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull DrumViewGridHolder holder, int position) {
        renderStep(holder, position);
    }

    private void updateStepHighlight(DrumViewGridHolder holder, int position, int velocity) {
        int stepsPerBar = mainActivityInterface.getDrumViewModel().getThisStepsPerBar();
        int stepInBar = position % stepsPerBar;
        int stepsPerPulse = mainActivityInterface.getDrumViewModel().getThisStepsPerPulse();

        // 1. Playhead (Highlighted Column)
        if (stepInBar == playheadStep) {
            holder.itemView.setBackgroundColor(Color.YELLOW);
        }
        // 2. Note (The Drum Beat) - This ensures notes REAPPEAR after the playhead passes
        else if (velocity > 0) {
            holder.itemView.setBackgroundColor(getVelocityColor(velocity));
        }
        // 3. Background Grid (Beats vs Off-beats)
        else {
            boolean isOnBeat = (stepInBar % stepsPerPulse == 0);
            holder.itemView.setBackgroundColor(isOnBeat ? mainActivityInterface.getPalette().secondary : mainActivityInterface.getPalette().primary);
        }
    }

    @Override
    public int getItemCount() {
        int steps = mainActivityInterface.getDrumViewModel().getThisStepsPerBar();
        int instrumentCount = mainActivityInterface.getDrumViewModel().getDrumSoundManager().getKit().getDrumParts().size();

        // Safety check: if things aren't loaded yet, return 0 to prevent crashes
        if (steps <= 0) {
            return 0;
        }

        return instrumentCount * steps;
    }

    public void setVariation(int type) {
        this.currentVariation = type;
        notifyDataSetChanged();
    }

    private Map<String, int[]> getActiveMap(DrumPatternJson pattern) {
        if (pattern == null) return null;

        switch (currentSection) {
            case VARIATION: return pattern.getVariationPattern();
            case FILL_MAIN: return pattern.getFillMainPattern();
            case FILL_VARIATION: return pattern.getFillVariationPattern();
            default: return pattern.getMainPattern();
        }
    }

    public void setSection(DrumSection section) {
        if (this.currentSection != section) {
            this.currentSection = section;
            notifyDataSetChanged(); // Switching the whole map requires a full redraw
        }
    }

    public void updatePlayhead(int newStep) {
        int oldStep = this.playheadStep;
        this.playheadStep = newStep;

        int stepsPerBar = mainActivityInterface.getDrumViewModel().getThisStepsPerBar();
        // Assuming you have a way to know how many rows (instruments) are visible
        int instrumentCount = mainActivityInterface.getDrumViewModel().getDrumSoundManager().getKit().getDrumParts().size();

        for (int i = 0; i < instrumentCount; i++) {
            int oldPos = i * stepsPerBar + oldStep;
            int newPos = i * stepsPerBar + newStep;

            if (oldStep != -1) notifyItemChanged(oldPos, "PLAYHEAD");
            notifyItemChanged(newPos, "PLAYHEAD");
        }
    }

}