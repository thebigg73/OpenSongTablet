package com.garethevans.church.opensongtablet.core.music.partiture.tab;

import com.garethevans.church.opensongtablet.core.music.partiture.PartitureLine;
import com.garethevans.church.opensongtablet.core.music.transpose.TransposeContext;

public class PartitureTabLine extends PartitureLine<PartitureTabCell, PartitureTabLine> {

    public PartitureTabLine() {
        super();
    }

    public void addCell(PartitureTabCell cell) {
        this.cells.add(cell);
    }

    @Override
    protected PartitureTabCell createCell() {
        return new PartitureTabCell();
    }

    @Override
    public boolean isContinueRow() {
        return true;
    }

    @Override
    public PartitureTabLine transpose(int steps, boolean diatonic, TransposeContext context) {
        PartitureTabLine transposed = new PartitureTabLine();
        for (PartitureTabCell cell : this.cells) {
            transposed.cells.add(cell.transpose(steps, diatonic, context));
        }
        return transposed;
    }
}
