package com.garethevans.church.opensongtablet.core.music.tab;

import com.garethevans.church.opensongtablet.core.music.tone.Tone;
import com.garethevans.church.opensongtablet.core.music.transpose.AbstractTransposable;
import com.garethevans.church.opensongtablet.core.music.transpose.TransposeContext;

public class TabColumn extends AbstractTransposable<TabColumn> {

    private final Tab tab;

    private final TabNote[] tabNotes;

    public TabColumn(Tab tab) {
        this.tab = tab;
        this.tabNotes = new TabNote[tab.getStrings().size()];
    }

    public Tab getTab() {
        return this.tab;
    }

    public TabNote[] getTabNotes() {
        return this.tabNotes;
    }

    @Override
    public TabColumn transpose(int steps, boolean diatonic, TransposeContext context) {
        TabColumn tabColumn = new TabColumn(this.tab);
        for (int i = 0; i < this.tabNotes.length; i++) {
            TabNote note = this.tabNotes[i];
            if (note != null) {
                int chromaticSteps = steps;
                TabString string = tab.getStrings().get(i);
                Tone tone = string.getTone();
                if (diatonic) {
                    Tone tabTone = tone.transpose(steps, diatonic, context);
                    chromaticSteps = tabTone.getStep() - tone.getStep();
                }
                int newFret = note.getFret() + chromaticSteps;
                int newTargetFret = note.getTargetFret() + chromaticSteps;
                int maxFret = this.tab.getMaxFret();
                int index = i;
                if ((newFret < 0) || (newTargetFret < 0)) {
                    if ((index > 0) && (this.tabNotes[index - 1] == null)) {
                        index--;
                        TabString downString = this.tab.getStrings().get(index);
                        int offset = tone.getStep() - downString.getTone().getStep();
                        newFret = newFret + offset;
                        newTargetFret = newTargetFret + offset;
                    }
                } else if ((newFret > maxFret) || (newTargetFret > maxFret)) {
                    if (((index + 1) < this.tabNotes.length) && (this.tabNotes[index + 1] == null)) {
                        index++;
                        TabString upString = this.tab.getStrings().get(index);
                        int offset = upString.getTone().getStep() - tone.getStep();
                        newFret = newFret + offset;
                        newTargetFret = newTargetFret + offset;
                    }
                }
                TabNote transposedNote = TabNote.of(note.getValue(), newFret, newTargetFret, note.getEffects());
                tabColumn.tabNotes[index] = transposedNote;
            }
        }
        return tabColumn;
    }
}
