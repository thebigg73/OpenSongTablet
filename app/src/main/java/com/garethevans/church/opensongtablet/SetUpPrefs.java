// Based on saved preferences, set up our choices

package com.garethevans.church.opensongtablet;

public class SetUpPrefs extends Activity {

public void colours() {
            switch (mDisplayTheme) {
            case "Theme_Holo_Light":
            case "Theme.Holo.Light":
                lyricsTextColor = light_lyricsTextColor;
                lyricsCapoColor = light_lyricsCapoColor;
                lyricsBackgroundColor = light_lyricsBackgroundColor;
                lyricsVerseColor = light_lyricsVerseColor;
                lyricsChorusColor = light_lyricsChorusColor;
                lyricsBridgeColor = light_lyricsBridgeColor;
                lyricsCommentColor = light_lyricsCommentColor;
                lyricsPreChorusColor = light_lyricsPreChorusColor;
                lyricsTagColor = light_lyricsTagColor;
                lyricsChordsColor = light_lyricsChordsColor;
                lyricsCustomColor = light_lyricsCustomColor;
                lyricsBoxColor = light_lyricsTextColor;
                metronomeColor = light_metronome;
                break;
            case "Theme_Holo":
            case "Theme.Holo":
                lyricsTextColor = dark_lyricsTextColor;
                lyricsCapoColor = dark_lyricsCapoColor;
                lyricsBackgroundColor = dark_lyricsBackgroundColor;
                lyricsVerseColor = dark_lyricsVerseColor;
                lyricsChorusColor = dark_lyricsChorusColor;
                lyricsBridgeColor = dark_lyricsBridgeColor;
                lyricsCommentColor = dark_lyricsCommentColor;
                lyricsPreChorusColor = dark_lyricsPreChorusColor;
                lyricsTagColor = dark_lyricsTagColor;
                lyricsChordsColor = dark_lyricsChordsColor;
                lyricsCustomColor = dark_lyricsCustomColor;
                lyricsBoxColor = dark_lyricsTextColor;
                metronomeColor = dark_metronome;
                break;
            case "custom1":
                lyricsTextColor = custom1_lyricsTextColor;
                lyricsCapoColor = custom1_lyricsCapoColor;
                lyricsBackgroundColor = custom1_lyricsBackgroundColor;
                lyricsVerseColor = custom1_lyricsVerseColor;
                lyricsChorusColor = custom1_lyricsChorusColor;
                lyricsBridgeColor = custom1_lyricsBridgeColor;
                lyricsCommentColor = custom1_lyricsCommentColor;
                lyricsPreChorusColor = custom1_lyricsPreChorusColor;
                lyricsTagColor = custom1_lyricsTagColor;
                lyricsChordsColor = custom1_lyricsChordsColor;
                lyricsCustomColor = custom1_lyricsCustomColor;
                lyricsBoxColor = custom1_lyricsTextColor;
                metronomeColor = custom1_metronome;
                break;
            case "custom2":
                lyricsTextColor = custom2_lyricsTextColor;
                lyricsCapoColor = custom2_lyricsCapoColor;
                lyricsBackgroundColor = custom2_lyricsBackgroundColor;
                lyricsVerseColor = custom2_lyricsVerseColor;
                lyricsChorusColor = custom2_lyricsChorusColor;
                lyricsBridgeColor = custom2_lyricsBridgeColor;
                lyricsCommentColor = custom2_lyricsCommentColor;
                lyricsPreChorusColor = custom2_lyricsPreChorusColor;
                lyricsTagColor = custom2_lyricsTagColor;
                lyricsChordsColor = custom2_lyricsChordsColor;
                lyricsCustomColor = custom2_lyricsCustomColor;
                lyricsBoxColor = custom2_lyricsTextColor;
                metronomeColor = custom2_metronome;
                break;
        }
}

public void fonts() {

}

}
