package com.garethevans.church.opensongtablet.core.music.partiture;

import com.garethevans.church.opensongtablet.core.music.harmony.Chord;
import com.garethevans.church.opensongtablet.core.music.partiture.section.PartitureSection;
import com.garethevans.church.opensongtablet.core.music.stave.Stave;
import com.garethevans.church.opensongtablet.core.music.transpose.AbstractTransposable;
import com.garethevans.church.opensongtablet.core.music.transpose.TransposeContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a row of a {@link Partiture}. Such row is a consists of a number of {@link #getLines() lines}
 * that can form an entire system of {@link Stave}(s), lyrics, etc.
 *
 * @see PartitureSection#getRows()
 */
public class PartitureRow extends AbstractTransposable<PartitureRow> {

    private final List<PartitureLine<?, ?>> lines;

    public PartitureRow() {
        super();
        this.lines = new ArrayList<>();
    }

    public List<PartitureLine<?, ?>> getLines() {
        return this.lines;
    }

    public void addLine(PartitureLine<?, ?> line) {
        this.lines.add(line);
    }

    public PartitureLine<?, ?> getLine(int i) {
        if (i >= this.lines.size()) {
            return null;
        }
        return lines.get(i);
    }

    /**
     * @return the number of columns, what is the maximum {@link PartitureLine#getCellCount() number of cells}
     * for all {@link #getLines() lines}.
     */
    public int getColumnCount() {
        int columnCount = 0;
        for (PartitureLine<?, ?> line : this.lines) {
            int cellCount = line.getCellCount();
            if (cellCount > columnCount) {
                columnCount = cellCount;
            }
        }
        return columnCount;
    }

    @Override
    public PartitureRow transpose(int steps, boolean diatonic, TransposeContext context) {
        PartitureRow transposed = new PartitureRow();
        for (PartitureLine<?, ?> line : this.lines) {
            transposed.addLine(line.transpose(steps, diatonic, context));
        }
        return transposed;
    }
}
