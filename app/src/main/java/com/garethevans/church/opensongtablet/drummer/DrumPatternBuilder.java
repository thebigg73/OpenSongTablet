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
        for (int[] track : pattern.getMainPattern().values()) {
            java.util.Arrays.fill(track, 0);
        }
        // 2. Correctly clear the variationPattern tracks
        for (int[] track : pattern.getVariationPattern().values()) {
            java.util.Arrays.fill(track, 0);
        }
        // 3. Correctly clear the fillMainPattern tracks
        for (int[] track : pattern.getFillMainPattern().values()) {
            java.util.Arrays.fill(track, 0);
        }
        // 4. Correctly clear the fillVariationPattern tracks
        for (int[] track : pattern.getFillVariationPattern().values()) {
            java.util.Arrays.fill(track, 0);
        }

        // Make sure all values are positive
        if (beats>-1 && divisions>-1) {
            // 1. Build Base Patterns
            if (divisions == 8) {
                buildCompoundPattern(pattern.getMainPattern(), beats, stepsPerPulse);
                buildCompoundVariation(pattern.getVariationPattern(), beats, stepsPerPulse);
            } else {
                buildSimplePattern(pattern.getMainPattern(), beats, stepsPerPulse);
                buildSimpleVariation(pattern.getVariationPattern(), beats, stepsPerPulse);
            }

            // 2. Build the Subtle Fills
            // We pass mainPattern so we can copy the kick/hat groove
            applySubtleSnareFill(pattern.getFillMainPattern(), pattern.getMainPattern(), beats, stepsPerPulse);
            applySubtleSnareFill(pattern.getFillVariationPattern(), pattern.getVariationPattern(), beats, stepsPerPulse);
        }
    }

    private static void buildSimplePattern(Map<String, int[]> targetMap, int beats, int stepsPerPulse) {
        int[] kick = targetMap.get("Kick");
        int[] snare = targetMap.get("Snare");
        int[] hihat = targetMap.get("HatClosed");

        // Ensure we have valid inputs and positive steps
        if (beats <= 0 || stepsPerPulse <= 0) return;

        for (int b = 0; b < beats; b++) {
            int startStep = b * stepsPerPulse;

            // CRITICAL SAFETY: If the next beat starts outside the array, stop building
            if (kick != null && startStep >= kick.length) break;

            // Kick on Beat 1 (and Beat 3 if 4 or more beats)
            if (b == 0 || (beats >= 4 && b == 2)) {
                if (kick != null) kick[startStep] = 100;
            }

            // Snare on Beats 2 and 4 (standard backbeat)
            if (b == 1 || b == 3 || (beats > 4 && (b + 1) % 2 == 0)) {
                if (snare != null && startStep < snare.length) {
                    snare[startStep] = 100;
                }
            }

            // Constant Hi-Hats on every beat
            if (hihat != null && startStep < hihat.length) {
                hihat[startStep] = 75;
            }
        }
    }

    private static void buildCompoundPattern(Map<String, int[]> targetMap, int beats, int stepsPerPulse) {
        int[] kick = targetMap.get("Kick");
        int[] snare = targetMap.get("Snare");
        int[] hihat = targetMap.get("HatClosed");

        for (int b = 0; b < beats; b++) {
            int startStep = b * stepsPerPulse;

            // Safety check: ensure we don't write past the end of the array
            if (startStep >= (kick != null ? kick.length : 0)) break;

            // Kick on Beat 1
            if (b == 0) kick[startStep] = 100;

            // Snare on Beat 4 (for 6/8) or Beat 4/10 (for 12/8)
            if ((b == 3 || b == 9) && snare != null && startStep < snare.length) {
                snare[startStep] = 100;
            }

            if (hihat != null && startStep < hihat.length) hihat[startStep] = 100;
        }
    }

    private static void buildCompoundVariation(Map<String, int[]> targetMap, int beats, int stepsPerPulse) {
        int[] kick = targetMap.get("Kick");
        int[] snare = targetMap.get("Snare");
        int[] hihat = targetMap.get("HatClosed");

        for (int b = 0; b < beats; b++) {
            int startStep = b * stepsPerPulse;
            if (startStep >= (kick != null ? kick.length : 0)) break;

            // Add a secondary kick for a "driving" feel
            if (b == 0 || b == 2) kick[startStep] = 100;

            // Snare on 4 and 10 (guarded)
            if ((b == 3 || b == 9) && snare != null && startStep < snare.length) {
                snare[startStep] = 100;
            }

            if (hihat != null && startStep < hihat.length) {
                hihat[startStep] = 100;
                int third = stepsPerPulse / 3;
                if (startStep + third < hihat.length) hihat[startStep + third] = 75;
            }
        }
    }

    private static void buildSimpleVariation(Map<String, int[]> targetMap, int beats, int stepsPerPulse) {
        int[] kick = targetMap.get("Kick");
        int[] snare = targetMap.get("Snare");
        int[] hihat = targetMap.get("HatClosed");

        if (beats <= 0 || stepsPerPulse <= 0) return;

        for (int b = 0; b < beats; b++) {
            int startStep = b * stepsPerPulse;

            // GLOBAL SAFETY: If the beat itself is out of bounds, stop.
            if (kick != null && startStep >= kick.length) break;

            // KICK: Foundation on 1 and 3, plus a "ghost" kick
            if (b == 0 || (beats >= 4 && b == 2)) {
                if (kick != null) {
                    kick[startStep] = 100;
                    int ghostKick = startStep + (stepsPerPulse / 2);
                    // BOUNDARY CHECK for the ghost note
                    if (ghostKick < kick.length) {
                        kick[ghostKick] = 70;
                    }
                }
            }

            // SNARE: Solid backbeat
            if (b == 1 || b == 3 || (beats > 4 && (b + 1) % 2 == 0)) {
                if (snare != null && startStep < snare.length) {
                    snare[startStep] = 100;
                }
            }

            // HI-HAT: 8th notes (driving pulse)
            if (hihat != null && startStep < hihat.length) {
                hihat[startStep] = 85;
                int eighthStep = startStep + (stepsPerPulse / 2);
                // BOUNDARY CHECK for the 8th note
                if (eighthStep < hihat.length) {
                    hihat[eighthStep] = 75;
                }
            }
        }
    }

    private static void applySubtleSnareFill(Map<String, int[]> targetMap, Map<String, int[]> sourceMap, int beats, int stepsPerPulse) {
        // 1. Safety Copy: Only copy what fits
        for (String instrument : sourceMap.keySet()) {
            int[] sourceTrack = sourceMap.get(instrument);
            int[] targetTrack = targetMap.get(instrument);
            if (sourceTrack != null && targetTrack != null) {
                int lengthToCopy = Math.min(sourceTrack.length, targetTrack.length);
                System.arraycopy(sourceTrack, 0, targetTrack, 0, lengthToCopy);
            }
        }

        // 2. Double Snare Lead-in
        int[] snare = targetMap.get("Snare");
        if (snare != null) {
            int finalBeatStart = (beats - 1) * stepsPerPulse;
            int andStep = finalBeatStart + (stepsPerPulse / 2);

            // BOUNDARY GUARDS: Essential for 6/8 vs 4/4 switches
            if (finalBeatStart >= 0 && finalBeatStart < snare.length) {
                snare[finalBeatStart] = 100;
            }
            if (andStep >= 0 && andStep < snare.length) {
                snare[andStep] = 100;
            }
        }
    }

}