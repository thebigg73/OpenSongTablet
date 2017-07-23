// Based on saved preferences, set up our choices

package com.garethevans.church.opensongtablet;

class SetUpColours {

    public static void colours() {
        switch (FullscreenActivity.mDisplayTheme) {
            case "Theme_Holo_Light":
            case "Theme.Holo.Light":
                FullscreenActivity.lyricsTextColor = FullscreenActivity.light_lyricsTextColor;
                FullscreenActivity.lyricsCapoColor = FullscreenActivity.light_lyricsCapoColor;
                FullscreenActivity.lyricsBackgroundColor = FullscreenActivity.light_lyricsBackgroundColor;
                FullscreenActivity.lyricsVerseColor = FullscreenActivity.light_lyricsVerseColor;
                FullscreenActivity.lyricsChorusColor = FullscreenActivity.light_lyricsChorusColor;
                FullscreenActivity.lyricsBridgeColor = FullscreenActivity.light_lyricsBridgeColor;
                FullscreenActivity.lyricsCommentColor = FullscreenActivity.light_lyricsCommentColor;
                FullscreenActivity.lyricsPreChorusColor = FullscreenActivity.light_lyricsPreChorusColor;
                FullscreenActivity.lyricsTagColor = FullscreenActivity.light_lyricsTagColor;
                FullscreenActivity.lyricsChordsColor = FullscreenActivity.light_lyricsChordsColor;
                FullscreenActivity.lyricsCustomColor = FullscreenActivity.light_lyricsCustomColor;
                FullscreenActivity.lyricsBoxColor = FullscreenActivity.light_lyricsTextColor;
                FullscreenActivity.metronomeColor = FullscreenActivity.light_metronome;
                FullscreenActivity.pagebuttonsColor = FullscreenActivity.light_pagebuttons;
                FullscreenActivity.presoAlertFontColor = FullscreenActivity.light_presoAlertFont;
                FullscreenActivity.presoFontColor = FullscreenActivity.light_presoFont;
                FullscreenActivity.presoInfoFontColor = FullscreenActivity.light_presoInfoFont;
                FullscreenActivity.presoShadowColor = FullscreenActivity.light_presoShadow;
                FullscreenActivity.stickytextColor = FullscreenActivity.light_stickytext;
                FullscreenActivity.stickybgColor = FullscreenActivity.light_stickybg;
                break;
            case "Theme_Holo":
            case "Theme.Holo":
                FullscreenActivity.lyricsTextColor = FullscreenActivity.dark_lyricsTextColor;
                FullscreenActivity.lyricsCapoColor = FullscreenActivity.dark_lyricsCapoColor;
                FullscreenActivity.lyricsBackgroundColor = FullscreenActivity.dark_lyricsBackgroundColor;
                FullscreenActivity.lyricsVerseColor = FullscreenActivity.dark_lyricsVerseColor;
                FullscreenActivity.lyricsChorusColor = FullscreenActivity.dark_lyricsChorusColor;
                FullscreenActivity.lyricsBridgeColor = FullscreenActivity.dark_lyricsBridgeColor;
                FullscreenActivity.lyricsCommentColor = FullscreenActivity.dark_lyricsCommentColor;
                FullscreenActivity.lyricsPreChorusColor = FullscreenActivity.dark_lyricsPreChorusColor;
                FullscreenActivity.lyricsTagColor = FullscreenActivity.dark_lyricsTagColor;
                FullscreenActivity.lyricsChordsColor = FullscreenActivity.dark_lyricsChordsColor;
                FullscreenActivity.lyricsCustomColor = FullscreenActivity.dark_lyricsCustomColor;
                FullscreenActivity.lyricsBoxColor = FullscreenActivity.dark_lyricsTextColor;
                FullscreenActivity.metronomeColor = FullscreenActivity.dark_metronome;
                FullscreenActivity.pagebuttonsColor = FullscreenActivity.dark_pagebuttons;
                FullscreenActivity.presoAlertFontColor = FullscreenActivity.dark_presoAlertFont;
                FullscreenActivity.presoFontColor = FullscreenActivity.dark_presoFont;
                FullscreenActivity.presoInfoFontColor = FullscreenActivity.dark_presoInfoFont;
                FullscreenActivity.presoShadowColor = FullscreenActivity.dark_presoShadow;
                FullscreenActivity.stickytextColor = FullscreenActivity.dark_stickytext;
                FullscreenActivity.stickybgColor = FullscreenActivity.dark_stickybg;
                break;
            case "custom1":
                FullscreenActivity.lyricsTextColor = FullscreenActivity.custom1_lyricsTextColor;
                FullscreenActivity.lyricsCapoColor = FullscreenActivity.custom1_lyricsCapoColor;
                FullscreenActivity.lyricsBackgroundColor = FullscreenActivity.custom1_lyricsBackgroundColor;
                FullscreenActivity.lyricsVerseColor = FullscreenActivity.custom1_lyricsVerseColor;
                FullscreenActivity.lyricsChorusColor = FullscreenActivity.custom1_lyricsChorusColor;
                FullscreenActivity.lyricsBridgeColor = FullscreenActivity.custom1_lyricsBridgeColor;
                FullscreenActivity.lyricsCommentColor = FullscreenActivity.custom1_lyricsCommentColor;
                FullscreenActivity.lyricsPreChorusColor = FullscreenActivity.custom1_lyricsPreChorusColor;
                FullscreenActivity.lyricsTagColor = FullscreenActivity.custom1_lyricsTagColor;
                FullscreenActivity.lyricsChordsColor = FullscreenActivity.custom1_lyricsChordsColor;
                FullscreenActivity.lyricsCustomColor = FullscreenActivity.custom1_lyricsCustomColor;
                FullscreenActivity.lyricsBoxColor = FullscreenActivity.custom1_lyricsTextColor;
                FullscreenActivity.metronomeColor = FullscreenActivity.custom1_metronome;
                FullscreenActivity.pagebuttonsColor = FullscreenActivity.custom1_pagebuttons;
                FullscreenActivity.presoAlertFontColor = FullscreenActivity.custom1_presoAlertFont;
                FullscreenActivity.presoFontColor = FullscreenActivity.custom1_presoFont;
                FullscreenActivity.presoInfoFontColor = FullscreenActivity.custom1_presoInfoFont;
                FullscreenActivity.presoShadowColor = FullscreenActivity.custom1_presoShadow;
                FullscreenActivity.stickytextColor = FullscreenActivity.custom1_stickytext;
                FullscreenActivity.stickybgColor = FullscreenActivity.custom1_stickybg;
                break;
            case "custom2":
                FullscreenActivity.lyricsTextColor = FullscreenActivity.custom2_lyricsTextColor;
                FullscreenActivity.lyricsCapoColor = FullscreenActivity.custom2_lyricsCapoColor;
                FullscreenActivity.lyricsBackgroundColor = FullscreenActivity.custom2_lyricsBackgroundColor;
                FullscreenActivity.lyricsVerseColor = FullscreenActivity.custom2_lyricsVerseColor;
                FullscreenActivity.lyricsChorusColor = FullscreenActivity.custom2_lyricsChorusColor;
                FullscreenActivity.lyricsBridgeColor = FullscreenActivity.custom2_lyricsBridgeColor;
                FullscreenActivity.lyricsCommentColor = FullscreenActivity.custom2_lyricsCommentColor;
                FullscreenActivity.lyricsPreChorusColor = FullscreenActivity.custom2_lyricsPreChorusColor;
                FullscreenActivity.lyricsTagColor = FullscreenActivity.custom2_lyricsTagColor;
                FullscreenActivity.lyricsChordsColor = FullscreenActivity.custom2_lyricsChordsColor;
                FullscreenActivity.lyricsCustomColor = FullscreenActivity.custom2_lyricsCustomColor;
                FullscreenActivity.lyricsBoxColor = FullscreenActivity.custom2_lyricsTextColor;
                FullscreenActivity.metronomeColor = FullscreenActivity.custom2_metronome;
                FullscreenActivity.pagebuttonsColor = FullscreenActivity.custom2_pagebuttons;
                FullscreenActivity.presoAlertFontColor = FullscreenActivity.custom2_presoAlertFont;
                FullscreenActivity.presoFontColor = FullscreenActivity.custom2_presoFont;
                FullscreenActivity.presoInfoFontColor = FullscreenActivity.custom2_presoInfoFont;
                FullscreenActivity.presoShadowColor = FullscreenActivity.custom2_presoShadow;
                FullscreenActivity.stickytextColor = FullscreenActivity.custom2_stickytext;
                FullscreenActivity.stickybgColor = FullscreenActivity.custom2_stickybg;
                break;
        }
    }
}