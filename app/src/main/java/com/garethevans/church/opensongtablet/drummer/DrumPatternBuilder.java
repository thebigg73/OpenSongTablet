package com.garethevans.church.opensongtablet.drummer;

import java.util.Map;

public class DrumPatternBuilder {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final static String TAG = "DrumPatternBuilder";

    /**
     * Builds a standard pattern based on the time signature.
     * @param pattern The DrumPattern object to populate.
     * @param beats The numerator (1-16).
     * @param divisions The denominator (2, 4, 8).
     * @param stepsPerPulse Calculated as (16 / divisions).
     */
    public static void buildStandardPattern(DrumPatternJson pattern, int beats, int divisions, int stepsPerPulse) {
        // 1. Correctly clear the mainPattern tracks
        for (int[] track : pattern.mainPattern.values()) {
            java.util.Arrays.fill(track, 0);
        }
        // 2. Correctly clear the variationPattern tracks
        for (int[] track : pattern.variationPattern.values()) {
            java.util.Arrays.fill(track, 0);
        }
        // 3. Correctly clear the fillMainPattern tracks
        for (int[] track : pattern.fillMainPattern.values()) {
            java.util.Arrays.fill(track, 0);
        }
        // 4. Correctly clear the fillVariationPattern tracks
        for (int[] track : pattern.fillVariationPattern.values()) {
            java.util.Arrays.fill(track, 0);
        }

        // Make sure all values are positive
        if (beats>-1 && divisions>-1) {
            // 1. Build Base Patterns
            if (divisions == 8) {
                buildCompoundPattern(pattern.mainPattern, beats, stepsPerPulse);
                buildCompoundVariation(pattern.variationPattern, beats, stepsPerPulse);
            } else {
                buildSimplePattern(pattern.mainPattern, beats, stepsPerPulse);
                buildSimpleVariation(pattern.variationPattern, beats, stepsPerPulse);
            }

            // 2. Build the Subtle Fills
            // We pass mainPattern so we can copy the kick/hat groove
            applySubtleSnareFill(pattern.fillMainPattern, pattern.mainPattern, beats, stepsPerPulse);
            applySubtleSnareFill(pattern.fillVariationPattern, pattern.variationPattern, beats, stepsPerPulse);
        }
    }


    private static void buildSimplePattern(Map<String, int[]> targetMap, int beats, int stepsPerPulse) {

        int[] kick = targetMap.get("Kick");
        int[] snare = targetMap.get("Snare");
        int[] hihat = targetMap.get("HatClosed");

        if (beats>=0) {
            for (int b = 0; b < beats; b++) {
                int startStep = Math.abs(b) * Math.abs(stepsPerPulse);

                // Kick on Beat 1 (and Beat 3 if 4 or more beats)
                if (b == 0 || (beats >= 4 && b == 2)) {
                    if (kick != null) kick[startStep] = 100;
                    //if (kick != null) kick[startStep] = true;
                }

                // Snare on Beats 2 and 4 (standard backbeat)
                if (b == 1 || b == 3 || (beats > 4 && (b + 1) % 2 == 0)) {
                    if (snare != null) snare[startStep] = 100;
                }

                // Constant Hi-Hats on every beat
                if (hihat != null) hihat[startStep] = 75;
            }
        }
    }

    private static void buildCompoundPattern(Map<String, int[]> targetMap, int beats, int stepsPerPulse) {
        int[] kick = targetMap.get("Kick");
        int[] snare = targetMap.get("Snare");
        int[] hihat = targetMap.get("HatClosed");

        for (int b = 0; b < beats; b++) {
            int startStep = b * stepsPerPulse;

            // Kick on Beat 1
            if (b == 0 && kick != null) {
                kick[startStep] = 100;
            }

            // Standard 6/8 or 12/8 feel: Snare on Beat 4 and 10
            if ((b == 3 || b == 9) && snare!=null) {
                snare[startStep] = 100;
            }

            // Hi-Hat on every "eighth note" (every beat in /8 time)
            if (hihat != null) hihat[startStep] = 100;
        }
    }


    // Pass the specific target map (e.g., pattern.getMainPattern() or pattern.getFillFullPattern())
    private static void buildSimpleVariation(Map<String, int[]> targetMap, int beats, int stepsPerPulse) {
        int[] kick = targetMap.get("Kick");
        int[] snare = targetMap.get("Snare");
        int[] hihat = targetMap.get("HatClosed");

        for (int b = 0; b < beats; b++) {
            int startStep = b * stepsPerPulse;

            // KICK: Foundation on 1 and 3, plus a "ghost" kick for energy
            if (b == 0 || (beats >= 4 && b == 2)) {
                if (kick != null) {
                    kick[startStep] = 100;
                    // Add a second kick half-way to the next beat
                    if (startStep + (stepsPerPulse / 2) < kick.length) {
                        kick[startStep + (stepsPerPulse / 2)] = 70;
                    }
                }
            }

            // SNARE: Solid backbeat
            if (b == 1 || b == 3 || (beats > 4 && (b + 1) % 2 == 0)) {
                if (snare != null) snare[startStep] = 100;
            }

            // HI-HAT: 8th notes (driving pulse)
            if (hihat != null) {
                hihat[startStep] = 85;
                int eighthStep = startStep + (stepsPerPulse / 2);
                if (eighthStep < hihat.length) hihat[eighthStep] = 75;
            }
        }
    }

    private static void buildCompoundVariation(Map<String, int[]> targetMap, int beats, int stepsPerPulse) {
        int[] kick = targetMap.get("Kick");
        int[] snare = targetMap.get("Snare");
        int[] hihat = targetMap.get("HatClosed");

        for (int b = 0; b < beats; b++) {
            int startStep = b * stepsPerPulse;

            // Add a secondary kick for a "driving" 6/8 feel
            if ((b == 0 || b == 2) && kick != null) kick[startStep] = 100;

            // Snare on 4 and 10 (standard)
            if ((b == 3 || b == 9) && snare != null) snare[startStep] = 100;

            // Play the "triplet" pulse for /8 time signatures
            if (hihat != null) {
                hihat[startStep] = 100;
                // Add ghost notes between main pulses
                int third = stepsPerPulse / 3;
                if (startStep + third < hihat.length) hihat[startStep + third] = 75;
            }
        }
    }

    private static void applySubtleSnareFill(Map<String, int[]> targetMap, Map<String, int[]> sourceMap, int beats, int stepsPerPulse) {    // Copy the groove so it doesn't go silent
        for (String instrument : sourceMap.keySet()) {
            int[] sourceTrack = sourceMap.get(instrument);
            int[] targetTrack = targetMap.get(instrument);
            if (sourceTrack != null && targetTrack != null) {
                System.arraycopy(sourceTrack, 0, targetTrack, 0, sourceTrack.length);
            }
        }

        // Add the "Double Snare" lead-in on the final beat
        int[] snare = targetMap.get("Snare");
        if (snare != null) {
            int finalBeatStart = (beats - 1) * stepsPerPulse;
            int andStep = finalBeatStart + (stepsPerPulse / 2);

            snare[finalBeatStart] = 75; // The "4"
            if (andStep < snare.length) {
                snare[andStep] = 100;   // The "and"
            }
        }
    }

}