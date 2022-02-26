package com.garethevans.church.opensongtablet.presenter;

import android.content.Context;
import android.net.Uri;
import android.view.Gravity;

import androidx.core.content.ContextCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class PresenterSettings {
    // This holds variables used during presentations
    // They are accessed using getters and setters
    // Anything that ProcessSong needs for creating layouts goes there instead

    private final String TAG = "PresenterSettings";
    private boolean alertOn, logoOn=true, blackscreenOn, blankscreenOn, hideInfoBar, presoShowChords,
            usePresentationOrder;
    private Uri logo, backgroundImage1, backgroundImage2, backgroundVideo1, backgroundVideo2;
    private int backgroundColor, presoTransitionTime, presoXMargin, presoYMargin, presoInfoAlign,
        presoLyricsAlign, presoLyricsVAlign, currentSection=-1;
    private String backgroundToUse, presoAlertText, ccliLicence;
    private float logoSize, castRotation, presoInfoBarAlpha, fontSizePresoMax, presoAlertTextSize,
            presoBackgroundAlpha, presoTitleTextSize, presoAuthorTextSize, presoCopyrightTextSize;
    private SongSectionsAdapter songSectionsAdapter;


    public PresenterSettings(Context c) {
        // Get all of the preferences on instantiation
        getAllPreferences(c,(MainActivityInterface) c);
    }

    // The setters
    public void setAlertOn(boolean alertOn) {
        this.alertOn = alertOn;
    }
    public void setLogo(Uri logo) {
        this.logo = logo;
    }
    public void setLogoOn(boolean logoOn) {
        this.logoOn = logoOn;
    }
    public void setBlackscreenOn(boolean blackscreenOn) {
        this.blackscreenOn = blackscreenOn;
    }
    public void setBlankscreenOn(boolean blankscreenOn) {
        this.blankscreenOn = blankscreenOn;
    }
    public void setLogoSize(float logoSize) {
        this.logoSize = logoSize;
    }
    public void setBackgroundImage1(Uri backgroundImage1) {
        this.backgroundImage1 = backgroundImage1;
    }
    public void setBackgroundImage2(Uri backgroundImage2) {
        this.backgroundImage2 = backgroundImage2;
    }
    public void setBackgroundVideo1(Uri backgroundVideo1) {
        this.backgroundVideo1 = backgroundVideo1;
    }
    public void setBackgroundVideo2(Uri backgroundVideo2) {
        this.backgroundVideo2 = backgroundVideo2;
    }
    public void setBackgroundToUse(String backgroundToUse) {
        this.backgroundToUse = backgroundToUse;
    }
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
    public void setPresoTransitionTime(int presoTransitionTime) {
        this.presoTransitionTime = presoTransitionTime;
    }
    public void setCastRotation(float castRotation) {
        this.castRotation = castRotation;
    }
    public void setPresoXMargin(int presoXMargin) {
        this.presoXMargin = presoXMargin;
    }
    public void setPresoYMargin(int presoYMargin) {
        this.presoYMargin = presoYMargin;
    }
    public void setPresoInfoAlign(int presoInfoAlign) {
        this.presoInfoAlign = presoInfoAlign;
    }
    public void setPresoLyricsAlign(int presoLyricsAlign) {
        this.presoLyricsAlign = presoLyricsAlign;
    }
    public void setPresoLyricsVAlign(int presoLyricsVAlign) {
        this.presoLyricsVAlign = presoLyricsVAlign;
    }
    public void setPresoInfoBarAlpha(float presoInfoBarAlpha) {
        this.presoInfoBarAlpha = presoInfoBarAlpha;
    }
    public void setFontSizePresoMax(float fontSizePresoMax) {
        this.fontSizePresoMax = fontSizePresoMax;
    }
    public void setPresoAlertTextSize(float presoAlertTextSize) {
        this.presoAlertTextSize = presoAlertTextSize;
    }
    public void setPresoAlertText(String presoAlertText) {
        this.presoAlertText = presoAlertText;
    }
    public void setPresoBackgroundAlpha(float presoBackgroundAlpha) {
        this.presoBackgroundAlpha = presoBackgroundAlpha;
    }
    public void setPresoShowChords(boolean presoShowChords) {
        this.presoShowChords = presoShowChords;
    }
    public void setUsePresentationOrder(boolean usePresentationOrder) {
        this.usePresentationOrder = usePresentationOrder;
    }
    public void setHideInfoBar(boolean hideInfoBar) {
        this.hideInfoBar = hideInfoBar;
    }
    public void setPresoTitleTextSize(float presoTitleTextSize) {
        this.presoTitleTextSize = presoTitleTextSize;
    }
    public void setPresoAuthorTextSize(float presoAuthorTextSize) {
        this.presoAuthorTextSize = presoAuthorTextSize;
    }
    public void setPresoCopyrightTextSize(float presoCopyrightTextSize) {
        this.presoCopyrightTextSize = presoCopyrightTextSize;
    }
    public void setCurrentSection(int currentSection) {
        this.currentSection = currentSection;
    }
    public void setSongSectionsAdapter(SongSectionsAdapter songSectionsAdapter) {
        this.songSectionsAdapter = songSectionsAdapter;
    }
    public void setCcliLicence(String ccliLicence) {
        this.ccliLicence = ccliLicence;
    }

    // The getters
    public boolean getAlertOn() {
        return alertOn;
    }
    public Uri getLogo() {
        return logo;
    }
    public boolean getLogoOn() {
        return logoOn;
    }
    public boolean getBlackscreenOn() {
        return blackscreenOn;
    }
    public boolean getBlankscreenOn() {
        return blankscreenOn;
    }
    public float getLogoSize() {
        return logoSize;
    }
    public Uri getBackgroundImage1() {
        return backgroundImage1;
    }
    public Uri getBackgroundImage2() {
        return backgroundImage2;
    }
    public Uri getBackgroundVideo1() {
        return backgroundVideo1;
    }
    public Uri getBackgroundVideo2() {
        return backgroundVideo2;
    }
    public String getBackgroundToUse() {
        return backgroundToUse;
    }
    public int getBackgroundColor() {
        return backgroundColor;
    }
    public Uri getChosenBackground() {
        switch (backgroundToUse) {
            case "img1":
                return backgroundImage1;
            case "img2":
                return backgroundImage2;
            case "vid1":
                return backgroundVideo1;
            case "vid2":
                return backgroundVideo2;
            default:
            case "color":
                return null;
        }
    }
    public int getPresoTransitionTime() {
        return presoTransitionTime;
    }
    public float getCastRotation() {
        return castRotation;
    }
    public int getPresoXMargin() {
        return presoXMargin;
    }
    public int getPresoYMargin() {
        return presoYMargin;
    }
    public int getPresoInfoAlign() {
        return presoInfoAlign;
    }
    public int getPresoLyricsAlign() {
        return presoLyricsAlign;
    }
    public int getPresoLyricsVAlign() {
        return presoLyricsVAlign;
    }
    public float getPresoInfoBarAlpha() {
        return presoInfoBarAlpha;
    }
    public float getFontSizePresoMax() {
        return fontSizePresoMax;
    }
    public float getPresoAlertTextSize() {
        return presoAlertTextSize;
    }
    public String getPresoAlertText() {
        return presoAlertText;
    }
    public float getPresoBackgroundAlpha() {
        return presoBackgroundAlpha;
    }
    public boolean getPresoShowChords() {
        return presoShowChords;
    }
    public boolean getUsePresentationOrder() {
        return usePresentationOrder;
    }
    public boolean getHideInfoBar() {
        return hideInfoBar;
    }
    public float getPresoTitleTextSize() {
        return presoTitleTextSize;
    }
    public float getPresoAuthorTextSize() {
        return presoAuthorTextSize;
    }
    public float getPresoCopyrightTextSize() {
        return presoCopyrightTextSize;
    }
    public int getCurrentSection() {
        return currentSection;
    }
    public SongSectionsAdapter getSongSectionsAdapter() {
        return songSectionsAdapter;
    }
    public String getCcliLicence () {
        return ccliLicence;
    }


    // The helpers for this class
    public void getAllPreferences(Context c, MainActivityInterface mainActivityInterface) {
        // This calls all preference groups
        getImagePreferences(c, mainActivityInterface);
        getScreenSetupPreferences(c, mainActivityInterface);
        getInfoPreferences(c, mainActivityInterface);
        getAlertPreferences(c, mainActivityInterface);
    }

    public void getImagePreferences(Context c, MainActivityInterface mainActivityInterface) {
        setLogo(getUriFromString(c,mainActivityInterface,mainActivityInterface.
                        getPreferences().getMyPreferenceString("customLogo",""),
                "ost_logo.png"));
        setLogoSize(mainActivityInterface.getPreferences().getMyPreferenceFloat("customLogoSize",0.5f));
        setBackgroundImage1(getUriFromString(c,mainActivityInterface,mainActivityInterface.
                        getPreferences().getMyPreferenceString("backgroundImage1",""),
                "ost_bg.png"));
        setBackgroundImage2(getUriFromString(c,mainActivityInterface,mainActivityInterface.
                        getPreferences().getMyPreferenceString("backgroundImage2",""),
                null));
        setBackgroundVideo1(getUriFromString(c,mainActivityInterface,mainActivityInterface.
                        getPreferences().getMyPreferenceString("backgroundVideo1",""),
                null));
        setBackgroundVideo2(getUriFromString(c,mainActivityInterface,mainActivityInterface.
                        getPreferences().getMyPreferenceString("backgroundVideo2",""),
                null));
        setBackgroundToUse(mainActivityInterface.getPreferences().
                getMyPreferenceString("backgroundToUse","img1"));
        setBackgroundColor(mainActivityInterface.getPreferences().
                getMyPreferenceInt("backgroundColor", ContextCompat.getColor(c,R.color.red)));
        setPresoBackgroundAlpha(mainActivityInterface.getPreferences().
                getMyPreferenceFloat("presoBackgroundAlpha",1f));
    }
    public void getScreenSetupPreferences(Context c, MainActivityInterface mainActivityInterface) {
        setPresoTransitionTime(mainActivityInterface.getPreferences().getMyPreferenceInt("presoTransitionTime", 800));
        setCastRotation(mainActivityInterface.getPreferences().getMyPreferenceFloat("castRotation",0.0f));
        setPresoXMargin(mainActivityInterface.getPreferences().getMyPreferenceInt("presoXMargin",0));
        setPresoYMargin(mainActivityInterface.getPreferences().getMyPreferenceInt("presoYMargin",0));
        setPresoInfoAlign(mainActivityInterface.getPreferences().getMyPreferenceInt("presoInfoAlign", Gravity.END));
        setPresoLyricsAlign(mainActivityInterface.getPreferences().getMyPreferenceInt("presoLyricsAlign", Gravity.CENTER_HORIZONTAL));
        setPresoLyricsVAlign(mainActivityInterface.getPreferences().getMyPreferenceInt("presoLyricsVAlign", Gravity.CENTER_VERTICAL));
        setPresoShowChords(mainActivityInterface.getPreferences().getMyPreferenceBoolean("presoShowChords", false));
        setFontSizePresoMax(mainActivityInterface.getPreferences().getMyPreferenceFloat("fontSizePresoMax", 40f));
        setUsePresentationOrder(mainActivityInterface.getPreferences().getMyPreferenceBoolean("usePresentationOrder", false));
    }
    public void getInfoPreferences(Context c, MainActivityInterface mainActivityInterface) {
        setPresoInfoBarAlpha(mainActivityInterface.getPreferences().getMyPreferenceFloat("presoInfoBarAlpha",0.5f));
        setHideInfoBar(mainActivityInterface.getPreferences().getMyPreferenceBoolean("hideInfoBar",true));
        setPresoTitleTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat("presoTitleTextSize",14f));
        setPresoAuthorTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat("presoAuthorTextSize",12f));
        setPresoCopyrightTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat("presoCopyrightTextSize",12f));
        setCcliLicence(mainActivityInterface.getPreferences().getMyPreferenceString("ccliLicence",""));
    }
    public void getAlertPreferences(Context c, MainActivityInterface mainActivityInterface) {
        setPresoAlertText(mainActivityInterface.getPreferences().getMyPreferenceString("presoAlertText",""));
        setPresoAlertTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat("presoAlertTextSize", 12f));
    }

    private Uri getUriFromString(Context c, MainActivityInterface mainActivityInterface,
                                 String uriString, String backupString) {
        Uri uri = null;
        if (uriString!=null && !uriString.isEmpty()) {
            uri = mainActivityInterface.getStorageAccess().
                    fixLocalisedUri(uriString);
        }
        if ((backupString!=null && !backupString.isEmpty()) &&
                (uri==null || !mainActivityInterface.getStorageAccess().uriExists(uri))) {
            uri = mainActivityInterface.getStorageAccess().
                    fixLocalisedUri(backupString);
        }
        return uri;
    }
}
