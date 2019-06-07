package com.garethevans.church.opensongtablet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class ChordProConvertTests extends ChordProConvert {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                { /*input:*/ ";Intro",         /*expected:*/ ";[Intro]", "#01" },
                { /*input:*/ ";Intro:",        /*expected:*/ ";[Intro]", "#02"  },
                { /*input:*/ ";Outro:",        /*expected:*/ ";[Outro]", "#03"  },
                { /*input:*/ ";Outro",         /*expected:*/ ";[Outro]", "#04"  },
                { /*input:*/ ";Verse:",        /*expected:*/ ";[V]",     "#05"  },
                { /*input:*/ ";VERSE:",        /*expected:*/ ";[V]",     "#06"  },
                { /*input:*/ ";Verse 1:",      /*expected:*/ ";[V1]",    "#07"  },
                { /*input:*/ ";Verse 2:",      /*expected:*/ ";[V2]",    "#08"  },
                { /*input:*/ ";Verse 3:",      /*expected:*/ ";[V3]",    "#09"  },
                { /*input:*/ ";Verse 4:",      /*expected:*/ ";[V4]",    "#10"  },
                { /*input:*/ ";VERSE 1:",      /*expected:*/ ";[V1]",    "#11"  },
                { /*input:*/ ";VERSE 2:",      /*expected:*/ ";[V2]",    "#12"  },
                { /*input:*/ ";VERSE 3:",      /*expected:*/ ";[V3]",    "#13"  },
                { /*input:*/ ";VERSE 4:",      /*expected:*/ ";[V4]",    "#14"  },
                { /*input:*/ ";(VERSE)",       /*expected:*/ ";[V]",     "#15"  },
                { /*input:*/ ";(Verse 1)",     /*expected:*/ ";[V1]",    "#16"  },
                { /*input:*/ ";(Verse 2)",     /*expected:*/ ";[V2]",    "#17"  },
                { /*input:*/ ";(Verse 3)",     /*expected:*/ ";[V3]",    "#18"  },
                { /*input:*/ ";Verse 1",       /*expected:*/ ";[V1]",    "#19"  },
                { /*input:*/ ";Verse 2",       /*expected:*/ ";[V2]",    "#20"  },
                { /*input:*/ ";Verse 3",       /*expected:*/ ";[V3]",    "#21"  },
                { /*input:*/ ";Verse 4",       /*expected:*/ ";[V4]",    "#22"  },
                { /*input:*/ ";VERSE 1",       /*expected:*/ ";[V1]",    "#23"  },
                { /*input:*/ ";VERSE 2",       /*expected:*/ ";[V2]",    "#24"  },
                { /*input:*/ ";VERSE 3",       /*expected:*/ ";[V3]",    "#25"  },
                { /*input:*/ ";VERSE 4",       /*expected:*/ ";[V4]",    "#26"  },
                { /*input:*/ ";Verse",         /*expected:*/ ";[V]",     "#27"  },
                { /*input:*/ ";VERSE",         /*expected:*/ ";[V]",     "#28"  },
                { /*input:*/ ";Prechorus:",    /*expected:*/ ";[P]",     "#29"  },
                { /*input:*/ ";Pre-chorus:",   /*expected:*/ ";[P]",     "#30"  },
                { /*input:*/ ";PreChorus:",    /*expected:*/ ";[P]",     "#31"  },
                { /*input:*/ ";Pre-Chorus:",   /*expected:*/ ";[P]",     "#32"  },
                { /*input:*/ ";PRECHORUS:",    /*expected:*/ ";[P]",     "#33"  },
                { /*input:*/ ";Prechorus 1:",  /*expected:*/ ";[P1]",    "#34"  },
                { /*input:*/ ";Prechorus 2:",  /*expected:*/ ";[P2]",    "#35"  },
                { /*input:*/ ";Prechorus 3:",  /*expected:*/ ";[P3]",    "#36"  },
                { /*input:*/ ";PreChorus 1:",  /*expected:*/ ";[P1]",    "#37"  },
                { /*input:*/ ";PreChorus 2:",  /*expected:*/ ";[P2]",    "#38"  },
                { /*input:*/ ";PreChorus 3:",  /*expected:*/ ";[P3]",    "#39"  },
                { /*input:*/ ";Pre-Chorus 1:", /*expected:*/ ";[P1]",    "#40"  },
                { /*input:*/ ";Pre-Chorus 2:", /*expected:*/ ";[P2]",    "#41"  },
                { /*input:*/ ";Pre-Chorus 3:", /*expected:*/ ";[P3]",    "#42"  },
                { /*input:*/ ";(Chorus)",      /*expected:*/ ";[C]",     "#43"  },
                { /*input:*/ ";Chorus:",       /*expected:*/ ";[C]",     "#44"  },
                { /*input:*/ ";CHORUS:",       /*expected:*/ ";[C]",     "#45"  },
                { /*input:*/ ";Chorus 1:",     /*expected:*/ ";[C1]",    "#46"  },
                { /*input:*/ ";Chorus 2:",     /*expected:*/ ";[C2]",    "#47"  },
                { /*input:*/ ";Chorus 3:",     /*expected:*/ ";[C3]",    "#48"  },
                { /*input:*/ ";CHORUS 1:",     /*expected:*/ ";[C1]",    "#49"  },
                { /*input:*/ ";CHORUS 2:",     /*expected:*/ ";[C2]",    "#50"  },
                { /*input:*/ ";CHORUS 3:",     /*expected:*/ ";[C3]",    "#51"  },
                { /*input:*/ ";Chorus 1",      /*expected:*/ ";[C1]",    "#52"  },
                { /*input:*/ ";Chorus 2",      /*expected:*/ ";[C2]",    "#53"  },
                { /*input:*/ ";Chorus 3",      /*expected:*/ ";[C3]",    "#54"  },
                { /*input:*/ ";CHORUS 1",      /*expected:*/ ";[C1]",    "#55"  },
                { /*input:*/ ";CHORUS 2",      /*expected:*/ ";[C2]",    "#56"  },
                { /*input:*/ ";CHORUS 3",      /*expected:*/ ";[C3]",    "#57"  },
                { /*input:*/ ";Chorus",        /*expected:*/ ";[C]",     "#58"  },
                { /*input:*/ ";CHORUS",        /*expected:*/ ";[C]",     "#59"  },
                { /*input:*/ ";Bridge:",       /*expected:*/ ";[B]",     "#60"  },
                { /*input:*/ ";BRIDGE:",       /*expected:*/ ";[B]",     "#61"  },
                { /*input:*/ ";Bridge 1:",     /*expected:*/ ";[B1]",    "#62"  },
                { /*input:*/ ";Bridge 2:",     /*expected:*/ ";[B2]",    "#63"  },
                { /*input:*/ ";Bridge 3:",     /*expected:*/ ";[B3]",    "#64"  },
                { /*input:*/ ";BRIDGE 1:",     /*expected:*/ ";[B1]",    "#65"  },
                { /*input:*/ ";BRIDGE 2:",     /*expected:*/ ";[B2]",    "#66"  },
                { /*input:*/ ";BRIDGE 3:",     /*expected:*/ ";[B3]",    "#67"  },
                { /*input:*/ ";Tag:",          /*expected:*/ ";[T]",     "#68"  },
                { /*input:*/ ";[V]",           /*expected:*/ ";[V]",     "#69"  },
                { /*input:*/ ";Instr:",        /*expected:*/ ";[Instr]", "#70"  },
                { /*input:*/ ";INTRO",         /*expected:*/ ";[INTRO]", "#71"  },
                { /*input:*/ "# Verse: ",      /*expected:*/ "#[V]",     "#72"  },
                { /*input:*/ "#Verse 5:",      /*expected:*/ "#[V5]",    "#73"  },
                { /*input:*/ "#Verse 15:",     /*expected:*/ "#[V15]",   "#74"  },
                { /*input:*/ "#Verse1",        /*expected:*/ "#[V1]",    "#75"  },
                { /*input:*/ ";Bridge 1a:",    /*expected:*/ ";[B1a]",   "#76"  },
                { /*input:*/ ";Tag",           /*expected:*/ ";[T]",     "#77"  },
                { /*input:*/ ";(Tag)",         /*expected:*/ ";[T]",     "#78"  },
                { /*input:*/ ";(TAG)",         /*expected:*/ ";[T]",     "#79"  },
        });
    }

    @Parameter // first data value (0) is default
    public String input;

    @Parameter(1)
    public String expected;

    @Parameter(2)
    public String message;

    @Test
    public void guessTags_StandardizesTags() {
        assertEquals(message + " Input: '" + input + "'", expected, guessTags(input));
    }
}
