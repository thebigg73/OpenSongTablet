package com.garethevans.church.opensongtablet.core.music.partiture;

import com.garethevans.church.opensongtablet.core.music.transpose.TransposeContext;

public class PartitureEmptyLine extends PartitureLine<PartitureCell<?>, PartitureEmptyLine> {

    public static final PartitureEmptyLine INSTANCE = new PartitureEmptyLine();

    @Override
    public boolean isContinueRow() {
        return true;
    }

    @Override
    protected PartitureCell<?> createCell() {
        throw new IllegalStateException();
    }

    @Override
    public PartitureEmptyLine transpose(int steps, boolean diatonic, TransposeContext context) {
        return this;
    }


}
