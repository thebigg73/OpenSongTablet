package com.garethevans.church.opensongtablet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class PopUpFindNewSongsFragmentTests extends PopUpFindNewSongsFragment {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                { /*input:*/ "<pre class=\"cproSongBody\"><span class=\"chordWrapper\"><code class=\"chord\" data-chordname=\"A\">A</code><span class=\"chordLyrics\">Text</span></span></pre>",
                        /*expected:*/ "[A]Text", "#01" },
                { /*input:*/ "<pre class=\"cproSongBody\"><span class=\"chordWrapper\"><code class=\"chord\" data-chordname=\"F#m<sup>7</sup>\">F#m<sup>7</sup></code><span class=\"chordLyrics\">Text</span></span></pre>",
                        /*expected:*/ "[F#m7]Text", "#02" },
        });
    }

    @Parameterized.Parameter // first data value (0) is default
    public String input;

    @Parameterized.Parameter(1)
    public String expected;

    @Parameterized.Parameter(2)
    public String message;

    @Test
    public void getLyricsSongSelectChordPro_ExtractsChordCorrectly() {
        assertEquals(message + " Input: '" + input + "'", expected, getLyricsSongSelectChordPro(input));
    }

}
