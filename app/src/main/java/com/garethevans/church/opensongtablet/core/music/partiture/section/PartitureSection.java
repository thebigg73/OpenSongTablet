package com.garethevans.church.opensongtablet.core.music.partiture.section;

import com.garethevans.church.opensongtablet.core.music.partiture.Partiture;
import com.garethevans.church.opensongtablet.core.music.partiture.PartitureRow;
import com.garethevans.church.opensongtablet.core.music.transpose.AbstractTransposable;
import com.garethevans.church.opensongtablet.core.music.transpose.TransposeContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a logical section of a {@link Partiture}. Each section has a {@link #getName() name} such
 * as "Verse 1" and multiple {@link #getRows() partiture rows}.
 */
public class PartitureSection extends AbstractTransposable<PartitureSection> {

    private PartitureSectionName name;

    private final List<PartitureRow> rows;

    public PartitureSection() {
        super();
        this.rows = new ArrayList<>();
    }

    public PartitureSection(PartitureSectionName name) {
        this();
        this.name = name;
    }

    public PartitureSectionName getName() {
        return this.name;
    }

    public void setName(PartitureSectionName name) {
        this.name = name;
    }

    public List<PartitureRow> getRows() {
        return this.rows;
    }

    public PartitureRow getRow(int i) {
        if (i >= this.rows.size()) {
            return null;
        }
        return this.rows.get(i);
    }

    public void addRow(PartitureRow row) {
        this.rows.add(row);
    }

    public boolean isEmpty() {
        return this.rows.isEmpty();
    }
//
//    /**
//     * @return {@code true} if the last {@link PartitureRow} was {@link PartitureRow#isEmpty() empty}
//     * and has been {@link List#remove(int) removed} from the {@link #getRows() rows}.
//     */
//    public boolean removeLastRowIfEmpty() {
//
//        int last = this.rows.size() - 1;
//        if (last >= 0) {
//            PartitureRow row = this.rows.get(last);
//            if (row.isEmpty()) {
//                this.rows.remove(last);
//                return true;
//            }
//        }
//        return false;
//    }

    @Override
    public PartitureSection transpose(int steps, boolean diatonic, TransposeContext context) {
        PartitureSection section = new PartitureSection(this.name);
        for (PartitureRow row : this.rows) {
            section.rows.add(row.transpose(steps, diatonic, context));
        }
        return section;
    }

    @Override
    public String toString() {
        if (this.name == null) {
            return super.toString();
        }
        return this.name.toString();
    }
}
