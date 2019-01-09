package com.garethevans.church.opensongtablet.core.format;

import com.garethevans.church.opensongtablet.core.music.harmony.Chord;
import com.garethevans.church.opensongtablet.core.music.harmony.ChordExtension;
import com.garethevans.church.opensongtablet.core.music.harmony.ChordMapper;
import com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey;
import com.garethevans.church.opensongtablet.core.music.harmony.TonalSystem;
import com.garethevans.church.opensongtablet.core.music.note.Note;
import com.garethevans.church.opensongtablet.core.music.partiture.Partiture;
import com.garethevans.church.opensongtablet.core.music.partiture.PartitureLine;
import com.garethevans.church.opensongtablet.core.music.partiture.PartitureRow;
import com.garethevans.church.opensongtablet.core.music.partiture.section.PartitureSection;
import com.garethevans.church.opensongtablet.core.music.partiture.section.PartitureSectionName;
import com.garethevans.church.opensongtablet.core.music.partiture.section.PartitureSectionType;
import com.garethevans.church.opensongtablet.core.music.partiture.voice.PartitureVoiceCell;
import com.garethevans.church.opensongtablet.core.music.partiture.voice.PartitureVoiceLine;
import com.garethevans.church.opensongtablet.core.music.partiture.voice.PartitureVoiceLineContinuation;
import com.garethevans.church.opensongtablet.core.music.rythm.Beat;
import com.garethevans.church.opensongtablet.core.music.rythm.MusicalValue;
import com.garethevans.church.opensongtablet.core.music.rythm.Rest;
import com.garethevans.church.opensongtablet.core.music.rythm.ValuedItem;
import com.garethevans.church.opensongtablet.core.music.stave.Clef;
import com.garethevans.church.opensongtablet.core.music.stave.Stave;
import com.garethevans.church.opensongtablet.core.music.stave.StaveBracket;
import com.garethevans.church.opensongtablet.core.music.stave.StaveVoice;
import com.garethevans.church.opensongtablet.core.music.tone.Tone;
import com.garethevans.church.opensongtablet.core.music.tone.TonePitchEnglish;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Test of {@link SongFormatChordPro}.
 */
public class SongFormatChordProTest extends SongFormatTest {

    // Note: the "song" tested here is pure non-sense. It only serves the purpose of a technical test.
    // To avoid copyright violations no real song is used for testing.
    // Background: this crazy song is kind of a joke to a German children winter song.

    private static final String LYRICS_WITH_CHORDS = "#[Chorus 1]\n" +
            "[A]a [Bm7]b [Cadd9]c the caT is [D] dead.\n";

    private static final String LYRICS_WITH_PARTITURE = "#[C1]\n" +
            "<[]K:C,B:4/4,C:G,V:Soprano;S>{A2}[A]a [Bm7]{B2}b [Cadd9]{c4}c {d4}the {c4}caT {B4}is [D]{A2} dead.{z2}\n" +
            "+<V:Alto;A>{F2}{G2}{A4}{B4}{A4}{G4}{F2}{z2}\n" +
            "-<[]C:F,V:Tenor;T>{C2}{D2}{E4}{F4}{E4}{D4}{C2}{z2}\n" +
            "+<V:Bass;B>{A,2}{B,2}{C4}{D4}{C4}{B,4}{A,2}{z2}";

    /** Test of {@link SongFormatChordPro#parse(String)}. */
    @Test
    public void testParseLyricsWithChords() {
        // given
        String lyrics = LYRICS_WITH_CHORDS;

        // when
        Partiture partiture = SongFormatChordPro.INSTANCE.parse(lyrics);

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
        assertThat(cell.getLyric()).isEqualTo("b ");
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

    /** Test of {@link SongFormatChordPro#format(Partiture)}. */
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
        String lyrics = SongFormatChordPro.INSTANCE.format(partiture);

        // then
        assertThat(lyrics).isEqualTo(LYRICS_WITH_CHORDS);
    }


    /** Test of {@link SongFormatChordPro#parse(String)} with full partiture. */
    @Test
    public void testParseLyricsWithPartiture() {
        // given
        String lyrics = LYRICS_WITH_PARTITURE;

        // when
        Partiture partiture = SongFormatChordPro.INSTANCE.parse(lyrics);

        // then
        assertThat(partiture).isNotNull();
        assertThat(partiture.getSections()).hasSize(1);
        PartitureSection section = partiture.getSections().get(0);
        assertThat(section).isNotNull();
        PartitureSectionName name = section.getName();
        assertThat(name.getName()).isEqualTo("C1");
        assertThat(name.getType()).isSameAs(PartitureSectionType.CHORUS);
        assertThat(name.getSuffix()).isEqualTo("1");
        assertThat(section.getRows()).hasSize(1);
        PartitureRow row = section.getRows().get(0);
        assertThat(row).isNotNull();
        assertThat(row.getColumnCount()).isEqualTo(8);
        assertThat(row.getLines()).hasSize(4);
        int lineIndex = 0;
        // first line
        PartitureLine<?, ?> line = row.getLine(lineIndex++);
        assertThat(line).isInstanceOf(PartitureVoiceLine.class);
        PartitureVoiceLine voiceLine = (PartitureVoiceLine) line;
        assertThat(voiceLine.getCells()).hasSize(8);
        assertThat(voiceLine.getContinuation()).isNull();
        Stave stave = new Stave(Clef.G, MusicalKey.C_MAJOR, Beat._4_4);
        stave.addVoice(StaveVoice.SOPRANO);
        stave.addVoice(StaveVoice.ALTO);
        stave.setBracket(StaveBracket.SQUARE);
        int cellIndex = 0;
        checkCell(voiceLine.getCell(cellIndex++), "a ", new Chord(TonePitchEnglish.A, TonalSystem.MAJOR_EMPTY), new Note(Tone.of(TonePitchEnglish.A, 2), MusicalValue._1_2), stave);
        checkCell(voiceLine.getCell(cellIndex++), "b ", new Chord(TonePitchEnglish.B, TonalSystem.of("m"), ChordExtension._7), new Note(Tone.of(TonePitchEnglish.B, 2), MusicalValue._1_2));
        checkCell(voiceLine.getCell(cellIndex++), "c ", new Chord(TonePitchEnglish.C, TonalSystem.MAJOR_EMPTY, ChordExtension.ADD_9), new Note(Tone.of(TonePitchEnglish.C, 3), MusicalValue._1_4));
        checkCell(voiceLine.getCell(cellIndex++), "the ", new Note(Tone.of(TonePitchEnglish.D, 3), MusicalValue._1_4));
        checkCell(voiceLine.getCell(cellIndex++), "caT ", new Note(Tone.of(TonePitchEnglish.C, 3), MusicalValue._1_4));
        checkCell(voiceLine.getCell(cellIndex++), "is ", new Note(Tone.of(TonePitchEnglish.B, 2), MusicalValue._1_4));
        checkCell(voiceLine.getCell(cellIndex++), " dead.", new Chord(TonePitchEnglish.D, TonalSystem.MAJOR_EMPTY), new Note(Tone.of(TonePitchEnglish.A, 2), MusicalValue._1_2));
        checkCell(voiceLine.getCell(cellIndex++), Rest._1_2);
        // second line
        line = row.getLine(lineIndex++);
        assertThat(line).isInstanceOf(PartitureVoiceLine.class);
        voiceLine = (PartitureVoiceLine) line;
        assertThat(voiceLine.getCells()).hasSize(8);
        assertThat(voiceLine.getContinuation()).isSameAs(PartitureVoiceLineContinuation.STAVE);
        cellIndex = 0;
        checkCell(voiceLine.getCell(cellIndex++), "", null, new Note(Tone.of(TonePitchEnglish.F, 2), MusicalValue._1_2), stave);
        checkCell(voiceLine.getCell(cellIndex++), new Note(Tone.of(TonePitchEnglish.G, 2), MusicalValue._1_2));
        checkCell(voiceLine.getCell(cellIndex++), new Note(Tone.of(TonePitchEnglish.A, 2), MusicalValue._1_4));
        checkCell(voiceLine.getCell(cellIndex++), new Note(Tone.of(TonePitchEnglish.B, 2), MusicalValue._1_4));
        checkCell(voiceLine.getCell(cellIndex++), new Note(Tone.of(TonePitchEnglish.A, 2), MusicalValue._1_4));
        checkCell(voiceLine.getCell(cellIndex++), new Note(Tone.of(TonePitchEnglish.G, 2), MusicalValue._1_4));
        checkCell(voiceLine.getCell(cellIndex++), new Note(Tone.of(TonePitchEnglish.F, 2), MusicalValue._1_2));
        checkCell(voiceLine.getCell(cellIndex++), Rest._1_2);
        // third line
        line = row.getLine(lineIndex++);
        assertThat(line).isInstanceOf(PartitureVoiceLine.class);
        voiceLine = (PartitureVoiceLine) line;
        assertThat(voiceLine.getCells()).hasSize(8);
        assertThat(voiceLine.getContinuation()).isSameAs(PartitureVoiceLineContinuation.LINE);
        stave = new Stave(Clef.F, MusicalKey.C_MAJOR, Beat._4_4);
        stave.addVoice(StaveVoice.TENOR);
        stave.addVoice(StaveVoice.BASS);
        stave.setBracket(StaveBracket.SQUARE);
        cellIndex = 0;
        checkCell(voiceLine.getCell(cellIndex++), "", null, new Note(Tone.of(TonePitchEnglish.C, 2), MusicalValue._1_2), stave);
        checkCell(voiceLine.getCell(cellIndex++), new Note(Tone.of(TonePitchEnglish.D, 2), MusicalValue._1_2));
        checkCell(voiceLine.getCell(cellIndex++), new Note(Tone.of(TonePitchEnglish.E, 2), MusicalValue._1_4));
        checkCell(voiceLine.getCell(cellIndex++), new Note(Tone.of(TonePitchEnglish.F, 2), MusicalValue._1_4));
        checkCell(voiceLine.getCell(cellIndex++), new Note(Tone.of(TonePitchEnglish.E, 2), MusicalValue._1_4));
        checkCell(voiceLine.getCell(cellIndex++), new Note(Tone.of(TonePitchEnglish.D, 2), MusicalValue._1_4));
        checkCell(voiceLine.getCell(cellIndex++), new Note(Tone.of(TonePitchEnglish.C, 2), MusicalValue._1_2));
        checkCell(voiceLine.getCell(cellIndex++), Rest._1_2);
        // fourth line
        line = row.getLine(lineIndex++);
        assertThat(line).isInstanceOf(PartitureVoiceLine.class);
        voiceLine = (PartitureVoiceLine) line;
        assertThat(voiceLine.getCells()).hasSize(8);
        assertThat(voiceLine.getContinuation()).isSameAs(PartitureVoiceLineContinuation.STAVE);
        cellIndex = 0;
        checkCell(voiceLine.getCell(cellIndex++), "", null, new Note(Tone.of(TonePitchEnglish.A, 1), MusicalValue._1_2), stave);
        checkCell(voiceLine.getCell(cellIndex++), new Note(Tone.of(TonePitchEnglish.B, 1), MusicalValue._1_2));
        checkCell(voiceLine.getCell(cellIndex++), new Note(Tone.of(TonePitchEnglish.C, 2), MusicalValue._1_4));
        checkCell(voiceLine.getCell(cellIndex++), new Note(Tone.of(TonePitchEnglish.D, 2), MusicalValue._1_4));
        checkCell(voiceLine.getCell(cellIndex++), new Note(Tone.of(TonePitchEnglish.C, 2), MusicalValue._1_4));
        checkCell(voiceLine.getCell(cellIndex++), new Note(Tone.of(TonePitchEnglish.B, 1), MusicalValue._1_4));
        checkCell(voiceLine.getCell(cellIndex++), new Note(Tone.of(TonePitchEnglish.A, 1), MusicalValue._1_2));
        checkCell(voiceLine.getCell(cellIndex++), Rest._1_2);
    }
}
