package com.garethevans.church.opensongtablet.core.music.partiture;

import com.garethevans.church.opensongtablet.core.music.transpose.AbstractTransposable;

import java.util.ArrayList;
import java.util.List;

public abstract class PartitureLine<C extends PartitureCell<?>, SELF extends PartitureLine<C, SELF>> extends AbstractTransposable<SELF> {

    protected final List<C> cells;

    public PartitureLine() {
        super();
        this.cells = new ArrayList<>();
    }

    public PartitureLine(List<C> cells) {
        super();
        this.cells = cells;
    }

    /**
     * @return the {@link List} of {@link PartitureCell}s.
     */
    public List<C> getCells() {
        return this.cells;
    }

    /**
     * @return the number of {@link #getCell(int) cells} in this line.
     */
    public int getCellCount() {
        return this.cells.size();
    }

    /**
     * @param i the index of the requested {@link PartitureCell}.
     * @return the requested {@link PartitureCell} or {@code null} if undefined or the given index is
     * less or equal to {@link #getCellCount() cell count}.
     */
    public C getCell(int i) {
        if (i >= this.cells.size()) {
            return null;
        }
        return this.cells.get(i);
    }

    /**
     * @param i the index of the requested {@link PartitureCell}.
     * @return the requested {@link PartitureCell}. If it did not previously exist, it has been created.
     * The {@link #getCellCount() cell count} is automatically increased as needed.
     */
    public C getOrCreateCell(int i) {
        int size = this.cells.size();
        int delta = i - size;
        while (delta >= 0) {
            this.cells.add(createCell());
            delta--;
        }
        return this.cells.get(i);
    }

    protected abstract C createCell();

    /**
     * @return {@code true} if this line belongs to the same {@link PartitureRow}, {@code false} otherwise
     * (if this is the first line of a new {@link PartitureRow}).
     */
    public abstract boolean isContinueRow();

}
