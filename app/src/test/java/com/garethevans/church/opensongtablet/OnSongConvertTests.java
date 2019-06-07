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
public class OnSongConvertTests {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                { /*input:*/ " Intro:",       /*expected:*/ "[Intro]", "#00"},
                { /*input:*/ " Outro:",       /*expected:*/ "[Outro]", "#01"},
                { /*input:*/ " V:",           /*expected:*/ "[V]",     "#02"},
                { /*input:*/ " V1:",          /*expected:*/ "[V1]",    "#03"},
                { /*input:*/ " V2:",          /*expected:*/ "[V2]",    "#04"},
                { /*input:*/ " V9:",          /*expected:*/ "[V9]",    "#05"},
                { /*input:*/ " Verse:",       /*expected:*/ "[V]",     "#06"},
                { /*input:*/ " Verse 1:",     /*expected:*/ "[V1]",    "#07"},
                { /*input:*/ " Verse 2:",     /*expected:*/ "[V2]",    "#08"},
                { /*input:*/ " Verse 3:",     /*expected:*/ "[V3]",    "#09"},
                { /*input:*/ " Verse 4:",     /*expected:*/ "[V4]",    "#10"},
                { /*input:*/ " (Verse)",      /*expected:*/ "[V]",     "#11"},
                { /*input:*/ " (Verse 1)",    /*expected:*/ "[V1]",    "#12"},
                { /*input:*/ " (Verse 2)",    /*expected:*/ "[V2]",    "#13"},
                { /*input:*/ " (Verse 3)",    /*expected:*/ "[V3]",    "#14"},
                { /*input:*/ " (Chorus)",     /*expected:*/ "[y]",     "#15"},
                { /*input:*/ " Chorus",       /*expected:*/ "[y]",     "#16"},
                { /*input:*/ " C:",           /*expected:*/ "[y]",     "#17"},
                { /*input:*/ " C1:",          /*expected:*/ "[C1]",    "#18"},
                { /*input:*/ " C2:",          /*expected:*/ "[C2]",    "#19"},
                { /*input:*/ " C3:",          /*expected:*/ "[C3]",    "#20"},
                { /*input:*/ " C4:",          /*expected:*/ "[C4]",    "#21"},
                { /*input:*/ " C9:",          /*expected:*/ "[C9]",    "#22"},
                { /*input:*/ " Chorus:",      /*expected:*/ "[y]",     "#23"},
                { /*input:*/ " Chorus 1:",    /*expected:*/ "[C1]",    "#24"},
                { /*input:*/ " Chorus 2:",    /*expected:*/ "[C2]",    "#25"},
                { /*input:*/ " Chorus 3:",    /*expected:*/ "[C3]",    "#26"},
                { /*input:*/ " Prechorus:",   /*expected:*/ "[P]",     "#27"},
                { /*input:*/ " Prechorus 1:", /*expected:*/ "[P1]",    "#28"},
                { /*input:*/ " Prechorus 2:", /*expected:*/ "[P2]",    "#29"},
                { /*input:*/ " Prechorus 3:", /*expected:*/ "[P3]",    "#30"},
                { /*input:*/ " Bridge:",      /*expected:*/ "[B]",     "#31"},
                { /*input:*/ " Tag:",         /*expected:*/ "[T]",     "#32"},
                { /*input:*/ "Intro:",        /*expected:*/ "[Intro]", "#33"},
                { /*input:*/ "Outro:",        /*expected:*/ "[Outro]", "#34"},
                { /*input:*/ "V:",            /*expected:*/ "[V]",     "#35"},
                { /*input:*/ "V1:",           /*expected:*/ "[V1]",    "#36"},
                { /*input:*/ "V2:",           /*expected:*/ "[V2]",    "#37"},
                { /*input:*/ "V8:",           /*expected:*/ "[V8]",    "#38"},
                { /*input:*/ "V9:",           /*expected:*/ "[V9]",    "#39"},
                { /*input:*/ "Verse:",        /*expected:*/ "[V]",     "#40"},
                { /*input:*/ "Verse 1:",      /*expected:*/ "[V1]",    "#41"},
                { /*input:*/ "Verse 4:",      /*expected:*/ "[V4]",    "#42"},
                { /*input:*/ "(Verse)",       /*expected:*/ "[V]",     "#43"},
                { /*input:*/ "(Verse 3)",     /*expected:*/ "[V3]",    "#44"},
                { /*input:*/ "(Chorus)",      /*expected:*/ "[y]",     "#45"},
                { /*input:*/ "C:",            /*expected:*/ "[y]",     "#46"},
                { /*input:*/ "C1:",           /*expected:*/ "[C1]",    "#47"},
                { /*input:*/ "C2:",           /*expected:*/ "[C2]",    "#48"},
                { /*input:*/ "C9:",           /*expected:*/ "[C9]",    "#49"},
                { /*input:*/ "Chorus:",       /*expected:*/ "[y]",     "#50"},
                { /*input:*/ "Chorus 1:",     /*expected:*/ "[C1]",    "#51"},
                { /*input:*/ "Chorus 3:",     /*expected:*/ "[C3]",    "#52"},
                { /*input:*/ "Prechorus:",    /*expected:*/ "[P]",     "#53"},
                { /*input:*/ "Prechorus 1:",  /*expected:*/ "[P1]",    "#54"},
                { /*input:*/ "Bridge:",       /*expected:*/ "[B]",     "#55"},
                { /*input:*/ "Tag:",          /*expected:*/ "[T]",     "#56"},
        });
    }

    @Parameter // first data value (0) is default
    public String input;

    @Parameter(1)
    public String expected;

    @Parameter(2)
    public String message;

    @Test
    public void guessTags_DetectsTags() {
        assertEquals(message + " Input: '" + input + "'", expected, OnSongConvert.guessTags(input));
    }
}
