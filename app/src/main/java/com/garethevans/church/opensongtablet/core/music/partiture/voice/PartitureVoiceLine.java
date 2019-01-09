package com.garethevans.church.opensongtablet.core.music.partiture.voice;

import com.garethevans.church.opensongtablet.core.music.partiture.PartitureLine;
import com.garethevans.church.opensongtablet.core.music.stave.Stave;
import com.garethevans.church.opensongtablet.core.music.transpose.TransposeContext;

/**
 * {@link PartitureLine} for a single voice (inside the {@link com.garethevans.church.opensongtablet.core.music.stave.Stave},
 * or with just the lyrics below).
 */
public class PartitureVoiceLine extends PartitureLine<PartitureVoiceCell, PartitureVoiceLine> {

    private PartitureVoiceLineContinuation continuation;

    public PartitureVoiceLine() {
        super();
    }

    /**
     * @return the optional {@link PartitureVoiceLineContinuation continuation} of this line or {@code null},
     * if this is the first line of a new {@link com.garethevans.church.opensongtablet.core.music.partiture.PartitureRow row}.
     */
    public PartitureVoiceLineContinuation getContinuation() {
        return this.continuation;
    }

    @Override
    public boolean isContinueRow() {
        return (this.continuation != null);
    }

    public void setContinuation(PartitureVoiceLineContinuation continuation) {
        this.continuation = continuation;
    }

    public void addCell(PartitureVoiceCell cell) {
        this.cells.add(cell);
    }

    @Override
    protected PartitureVoiceCell createCell() {
        return new PartitureVoiceCell();
    }

    @Override
    public PartitureVoiceLine transpose(int steps, boolean diatonic, TransposeContext context) {
        PartitureVoiceLine transposed = new PartitureVoiceLine();
        for (PartitureVoiceCell cell : this.cells) {
            transposed.cells.add(cell.transpose(steps, diatonic, context));
        }
        return transposed;
    }

    public void join(PartitureVoiceLine voiceLine) {
        assert (this.continuation != null);
        int len = this.cells.size();
        int otherLen = voiceLine.cells.size();
        if (otherLen < len) {
            len = otherLen;
        }
        for (int i = 0; i < len; i++) {
            PartitureVoiceCell otherCell = voiceLine.cells.get(i);
            Stave otherStave = otherCell.getStave();
            if (otherStave != null) {
                PartitureVoiceCell myCell = this.cells.get(i);
                Stave myStave = myCell.getStave();
                if (this.continuation == PartitureVoiceLineContinuation.STAVE) {
                    otherStave.join(myStave, true);
                    myCell.setStave(otherStave);
                } else if (this.continuation == PartitureVoiceLineContinuation.LINE) {
                    myStave.join(otherStave, false);
                }
            }
        }
    }
}
