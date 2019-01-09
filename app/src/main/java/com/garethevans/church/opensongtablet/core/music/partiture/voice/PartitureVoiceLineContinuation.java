package com.garethevans.church.opensongtablet.core.music.partiture.voice;

import com.garethevans.church.opensongtablet.core.format.FormatConstants;

/**
 * Defines how a {@link PartitureVoiceLine} continues its previous line.
 * @see PartitureVoiceLine#getContinuation()
 */
public final class PartitureVoiceLineContinuation {

    /** The line continues with a new voice in the same stave. */
    public static final PartitureVoiceLineContinuation STAVE = new PartitureVoiceLineContinuation(FormatConstants.CONTINUE_STAVE);

    /** The line continues with a new stave or lyrics line below the stave. */
    public static final PartitureVoiceLineContinuation LINE = new PartitureVoiceLineContinuation(FormatConstants.CONTINUE_ROW);

    private final char symbol;

    private PartitureVoiceLineContinuation(char symbol) {
        super();
        this.symbol = symbol;
    }

    public char getSymbol() {
        return this.symbol;
    }

    @Override
    public String toString() {
        return "" + this.symbol;
    }

    public static final PartitureVoiceLineContinuation of(char c) {
        if (c == STAVE.symbol) {
            return STAVE;
        } else if (c == LINE.symbol) {
            return LINE;
        }
        return null;
    }
}
