package com.garethevans.church.opensongtablet.core.music.partiture.comment;

import com.garethevans.church.opensongtablet.core.music.partiture.PartitureCell;
import com.garethevans.church.opensongtablet.core.music.partiture.PartitureLine;
import com.garethevans.church.opensongtablet.core.music.transpose.TransposeContext;

import java.util.Collections;

public class PartitureCommentLine extends PartitureLine<PartitureCell<?>, PartitureCommentLine> {

    private final String comment;

    public PartitureCommentLine(String comment) {
        super(Collections.<PartitureCell<?>> emptyList());
        this.comment = comment;
    }

    public String getComment() {
        return this.comment;
    }

    @Override
    public boolean isContinueRow() {
        return true;
    }

    @Override
    protected PartitureCell<?> createCell() {
        throw new IllegalStateException();
    }

    @Override
    public PartitureCommentLine transpose(int steps, boolean diatonic, TransposeContext context) {
        return this;
    }
}
