package com.garethevans.church.opensongtablet.core.music.transpose;

import com.garethevans.church.opensongtablet.core.music.harmony.Interval;
import com.garethevans.church.opensongtablet.core.music.harmony.TonalSystem;
import com.garethevans.church.opensongtablet.core.music.tone.TonePitch;

public abstract class AbstractTransposable<SELF extends AbstractTransposable<SELF>> implements Transposable<SELF> {

    @Override
    public SELF transposeChromatic(int steps) {
        return transpose(steps, false, new TransposeContext());
    }

    @Override
    public SELF transposeDiatonic(int steps) {
        return transpose(steps, true, new TransposeContext());
    }

    @Override
    public SELF transpose(Interval interval, TransposeContext context) {
        TonalSystem tonalSystem = context.getTonalSystem();
        int steps = interval.getChromaticSteps(tonalSystem);
        if (steps != Integer.MIN_VALUE) {
            return transpose(steps, false, context);
        } else {
            steps = interval.getDiatonicSteps(tonalSystem);
            if (steps == Integer.MIN_VALUE) {
                throw new IllegalStateException("Can not transpose by " + interval);
            }
            return transpose(steps, true, context);
        }
    }

    protected int getChromaticSteps(TonePitch original, TonePitch transposed, int diatonicSteps) {
        int chromaticSteps = transposed.getStep().get() - original.getStep().get();
        if ((chromaticSteps < 0) && (diatonicSteps > 0)) {
            chromaticSteps = chromaticSteps + 12;
        } else if ((chromaticSteps > 0) && (diatonicSteps < 0)) {
            chromaticSteps = 12 - chromaticSteps;
        }
        return chromaticSteps;
    }

}
