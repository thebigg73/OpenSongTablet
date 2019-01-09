package com.garethevans.church.opensongtablet.core.music.partiture;

import com.garethevans.church.opensongtablet.core.filter.ListCharFilter;
import com.garethevans.church.opensongtablet.core.format.AbstractMapper;
import com.garethevans.church.opensongtablet.core.format.SongFormatOptions;
import com.garethevans.church.opensongtablet.core.music.partiture.section.PartitureSection;
import com.garethevans.church.opensongtablet.core.music.partiture.section.PartitureSectionName;
import com.garethevans.church.opensongtablet.core.music.partiture.section.PartitureSectionNameMapper;
import com.garethevans.church.opensongtablet.core.music.partiture.voice.PartitureVoiceLine;
import com.garethevans.church.opensongtablet.core.parser.CharStream;

import java.io.IOException;

/**
 * {@link AbstractMapper} for {@link Partiture}.
 */
public abstract class PartitureMapper extends AbstractMapper<Partiture> {

    public static final PartitureMapper CHORD_PRO = PartitureMapperChordPro.INSTANCE;

    public static final PartitureMapper OPEN_SONG = PartitureMapperOpenSong.INSTANCE;

    @Override
    public Partiture parse(CharStream chars) {
        Partiture partiture = new Partiture();
        PartitureLineMapper lineMapper = getLineMapper();
        PartitureSectionNameMapper sectionNameMapper = getSectionNameMapper();
        PartitureSection section = null;
        PartitureSectionName sectionName = sectionNameMapper.parse(chars);
        if (sectionName != null) {
            section = new PartitureSection(sectionName);
            chars.skipUntil(ListCharFilter.NEWLINE);
            chars.skipNewline();
            partiture.addSection(section);
        }
        PartitureRow row = null;
        PartitureVoiceLine voiceLine = null;
        while (chars.hasNext()) {
            sectionName = sectionNameMapper.parse(chars);
            if (sectionName != null) {
                section = new PartitureSection(sectionName);
                partiture.addSection(section);
                row = null;
                chars.skipUntil(ListCharFilter.NEWLINE);
                chars.skipNewline();
            } else {
                PartitureLine<?, ?> line = lineMapper.parse(chars);
                if ((row == null) || !line.isContinueRow()) {
                    row = new PartitureRow();
                    if (section == null) {
                        section = new PartitureSection();
                        partiture.addSection(section);
                    }
                    section.addRow(row);
                }
                if (line instanceof PartitureVoiceLine) {
                    PartitureVoiceLine newVoiceLine = (PartitureVoiceLine) line;
                    if (line.isContinueRow() && (voiceLine != null)) {
                        newVoiceLine.join(voiceLine);
                    }
                    voiceLine = newVoiceLine;
                }
                row.addLine(line);
            }
        }
        return partiture;
    }


    protected abstract PartitureSectionNameMapper getSectionNameMapper();

    protected abstract PartitureLineMapper getLineMapper();

    @Override
    public void format(Partiture partiture, Appendable buffer, SongFormatOptions options) throws IOException {
        if (partiture == null) {
            return;
        }
        for (PartitureSection section : partiture.getSections()) {
            PartitureSectionName name = section.getName();
            if (name != null) {
                getSectionNameMapper().format(name, buffer, options);
                buffer.append(NEWLINE);
            }
            for (PartitureRow row : section.getRows()) {
                for (PartitureLine<?, ?> line : row.getLines()) {
                    getLineMapper().format(line, buffer, options);
                }
            }
        }
    }
}
