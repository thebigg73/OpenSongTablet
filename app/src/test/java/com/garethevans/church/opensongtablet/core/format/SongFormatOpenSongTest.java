package com.garethevans.church.opensongtablet.core.format;

import com.garethevans.church.opensongtablet.core.music.harmony.Chord;
import com.garethevans.church.opensongtablet.core.music.harmony.ChordExtension;
import com.garethevans.church.opensongtablet.core.music.harmony.ChordMapper;
import com.garethevans.church.opensongtablet.core.music.harmony.TonalSystem;
import com.garethevans.church.opensongtablet.core.music.partiture.Partiture;
import com.garethevans.church.opensongtablet.core.music.partiture.PartitureLine;
import com.garethevans.church.opensongtablet.core.music.partiture.PartitureRow;
import com.garethevans.church.opensongtablet.core.music.partiture.section.PartitureSection;
import com.garethevans.church.opensongtablet.core.music.partiture.section.PartitureSectionName;
import com.garethevans.church.opensongtablet.core.music.partiture.section.PartitureSectionType;
import com.garethevans.church.opensongtablet.core.music.partiture.voice.PartitureVoiceCell;
import com.garethevans.church.opensongtablet.core.music.partiture.voice.PartitureVoiceLine;
import com.garethevans.church.opensongtablet.core.music.tone.TonePitch;
import com.garethevans.church.opensongtablet.core.music.tone.TonePitchEnglish;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Test of {@link SongFormatOpenSong}.
 */
public class SongFormatOpenSongTest extends SongFormatTest {

    private static final String LYRICS_WITH_CHORDS = "[Chorus 1]\n" +
            ".A Bm7 Cadd9        D\n" +
            " a b   c the caT is  dead.\n";

    /**
     * Test of {@link SongFormatOpenSong#parse(String)}.
     */
    @Test
    public void testParseLyricsWithChords() {
        // given
        String lyrics = LYRICS_WITH_CHORDS;

        // when
        Partiture partiture = SongFormatOpenSong.INSTANCE.parse(lyrics);

        // then
        assertThat(partiture).isNotNull();
        assertThat(partiture.getSections()).hasSize(1);
        PartitureSection section = partiture.getSections().get(0);
        assertThat(section).isNotNull();
        PartitureSectionName name = section.getName();
        assertThat(name.getName()).isEqualTo("Chorus 1");
        assertThat(name.getType()).isSameAs(PartitureSectionType.CHORUS);
        assertThat(name.getSuffix()).isEqualTo(" 1");
        assertThat(section.getRows()).hasSize(1);
        PartitureRow row = section.getRows().get(0);
        assertThat(row).isNotNull();
        assertThat(row.getColumnCount()).isEqualTo(4);
        assertThat(row.getLines()).hasSize(1);
        PartitureLine<?, ?> line = row.getLine(0);
        assertThat(line).isInstanceOf(PartitureVoiceLine.class);
        PartitureVoiceLine voiceLine = (PartitureVoiceLine) line;
        assertThat(voiceLine.getCells()).hasSize(4);
        PartitureVoiceCell cell = voiceLine.getCell(0);
        assertThat(cell).isNotNull();
        assertThat(cell.getStave()).isNull();
        assertThat(cell.getItem()).isNull();
        assertThat(cell.getChord()).isEqualTo(new Chord(TonePitchEnglish.A, TonalSystem.MAJOR_EMPTY));
        assertThat(cell.getLyric()).isEqualTo("a ");
        cell = voiceLine.getCell(1);
        assertThat(cell).isNotNull();
        assertThat(cell.getStave()).isNull();
        assertThat(cell.getItem()).isNull();
        assertThat(cell.getChord()).isEqualTo(new Chord(TonePitchEnglish.B, TonalSystem.of("m"), ChordExtension._7));
        assertThat(cell.getLyric()).isEqualTo("b   ");
        cell = voiceLine.getCell(2);
        assertThat(cell).isNotNull();
        assertThat(cell.getStave()).isNull();
        assertThat(cell.getItem()).isNull();
        assertThat(cell.getChord()).isEqualTo(new Chord(TonePitchEnglish.C, TonalSystem.MAJOR_EMPTY, ChordExtension.ADD_9));
        assertThat(cell.getLyric()).isEqualTo("c the caT is ");
        cell = voiceLine.getCell(3);
        assertThat(cell).isNotNull();
        assertThat(cell.getStave()).isNull();
        assertThat(cell.getItem()).isNull();
        assertThat(cell.getChord()).isEqualTo(new Chord(TonePitchEnglish.D, TonalSystem.MAJOR_EMPTY));
        assertThat(cell.getLyric()).isEqualTo(" dead.");
    }

    /**
     * Test of {@link SongFormatOpenSong#format(Partiture)}.
     */
    @Test
    public void formatLyricsWithChords() {
        // given
        Partiture partiture = new Partiture();
        PartitureSectionName name = new PartitureSectionName("Chorus 1");
        PartitureSection section = new PartitureSection(name);
        PartitureRow row = new PartitureRow();
        PartitureVoiceLine line = new PartitureVoiceLine();
        line.addCell(new PartitureVoiceCell(new Chord(TonePitchEnglish.A, TonalSystem.MAJOR_EMPTY), "a "));
        line.addCell(new PartitureVoiceCell(ChordMapper.INSTANCE.parse("Bm7"), "b "));
        line.addCell(new PartitureVoiceCell(new Chord(TonePitchEnglish.C, TonalSystem.MAJOR_EMPTY, ChordExtension.ADD_9), "c the caT is "));
        line.addCell(new PartitureVoiceCell(new Chord(TonePitchEnglish.D, TonalSystem.MAJOR_EMPTY), " dead."));
        row.addLine(line);
        section.getRows().add(row);
        partiture.getSections().add(section);

        // when
        String lyrics = SongFormatOpenSong.INSTANCE.format(partiture);

        // then
        assertThat(lyrics).isEqualTo(LYRICS_WITH_CHORDS);
    }
}
