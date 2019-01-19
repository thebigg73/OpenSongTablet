package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class TextSongConvertTests {
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                { /*input:*/ "Bridge",         /*expected:*/ "[Bridge]\n",        "#01"},
                { /*input:*/ "Verse 1:",       /*expected:*/ "[Verse 1]\n",       "#02"},
                { /*input:*/ "  Chorus:",      /*expected:*/ "[Chorus]\n",        "#03"},
                { /*input:*/ "  Ending:",      /*expected:*/ "[Ending]\n",        "#04"},
                { /*input:*/ "  Instrumental:",/*expected:*/ "[Instrumental]\n",  "#05"},
                { /*input:*/ "  (1st Ending) some text",  /*expected:*/ "[Verse]\n  (1st Ending) some text\n",  "#06"},
        });
    };

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    Context context;

    @Mock
    Resources resources;

    @Parameterized.Parameter // first data value (0) is default
    public String input;

    @Parameterized.Parameter(1)
    public String expected;

    @Parameterized.Parameter(2)
    public String message;

    @Test
    public void convertText() {
        when(resources.getString(R.string.tag_verse)).thenReturn("Verse");
        when(resources.getString(R.string.tag_chorus)).thenReturn("Chorus");
        when(resources.getString(R.string.tag_bridge)).thenReturn("Bridge");
        when(resources.getString(R.string.tag_ending)).thenReturn("Ending");
        when(resources.getString(R.string.tag_instrumental)).thenReturn("Instrumental");
        when(resources.getString(R.string.tag_interlude)).thenReturn("Interlude");
        when(resources.getString(R.string.tag_intro)).thenReturn("Intro");
        when(resources.getString(R.string.tag_prechorus)).thenReturn("Prechorus");
        when(resources.getString(R.string.tag_refrain)).thenReturn("Refrain");
        when(resources.getString(R.string.tag_tag)).thenReturn("Tag");
        when(resources.getString(R.string.tag_reprise)).thenReturn("Reprise");
        when(context.getString(R.string.tag_verse)).thenReturn("Verse");
        when(context.getResources()).thenReturn(resources);
        when(context.createConfigurationContext(any(Configuration.class))).thenReturn(context);

        TextSongConvert textSongConvert = new TextSongConvert();
        assertEquals(message + " Input: '" + input + "'", expected, textSongConvert.convertText(context, input));
    }
}
