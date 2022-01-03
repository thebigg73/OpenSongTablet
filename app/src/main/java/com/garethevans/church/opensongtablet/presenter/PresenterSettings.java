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

    private final String TAG = "PresenterSettings";
    private boolean alertOn;
    private Uri logo, backgroundImage1, backgroundImage2, backgroundVideo1, backgroundVideo2;
    private int backgroundColor, presoTransitionTime, presoXMargin, presoYMargin, presoInfoAlign,
            infoBarChangeDelay, lyricDelay, panicDelay;
    private String backgroundToUse;
    private float logoSize, castRotation, presoInfoBarAlpha, fontSizePresoMax;

    public PresenterSettings(Context c) {
        // Get the preferences on instantiation
        getPreferences(c,(MainActivityInterface) c);
    }

    // The setters
    public void setAlertOn(boolean alertOn) {
        this.alertOn = alertOn;
    }
    public void setLogo(Uri logo) {
        this.logo = logo;
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
    public void setPresoInfoBarAlpha(float presoInfoBarAlpha) {
        this.presoInfoBarAlpha = presoInfoBarAlpha;
    }
    public void setFontSizePresoMax(float fontSizePresoMax) {
        this.fontSizePresoMax = fontSizePresoMax;
    }


    // The getters
    public boolean getAlertOn() {
        return alertOn;
    }
    public Uri getLogo() {
        return logo;
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
    public float getPresoInfoBarAlpha() {
        return presoInfoBarAlpha;
    }
    public float getFontSizePresoMax() {
        return fontSizePresoMax;
    }


    // The helpers for this class
    public void getPreferences(Context c, MainActivityInterface mainActivityInterface) {
        setLogo(getUriFromString(c,mainActivityInterface,mainActivityInterface.
                        getPreferences().getMyPreferenceString(c,"customLogo",""),
                "ost_logo.png"));
        setLogoSize(mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"customLogoSize",0.5f));
        setBackgroundImage1(getUriFromString(c,mainActivityInterface,mainActivityInterface.
                        getPreferences().getMyPreferenceString(c,"backgroundImage1",""),
                "ost_bg.png"));
        setBackgroundImage2(getUriFromString(c,mainActivityInterface,mainActivityInterface.
                        getPreferences().getMyPreferenceString(c,"backgroundImage2",""),
                null));
        setBackgroundVideo1(getUriFromString(c,mainActivityInterface,mainActivityInterface.
                        getPreferences().getMyPreferenceString(c,"backgroundVideo1",""),
                null));
        setBackgroundVideo2(getUriFromString(c,mainActivityInterface,mainActivityInterface.
                        getPreferences().getMyPreferenceString(c,"backgroundVideo2",""),
                null));
        setBackgroundToUse(mainActivityInterface.getPreferences().
                getMyPreferenceString(c, "backgroundToUse","img1"));
        setBackgroundColor(mainActivityInterface.getPreferences().
                getMyPreferenceInt(c,"backgroundColor", ContextCompat.getColor(c,R.color.colorPrimary)));
        setPresoTransitionTime(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"presoTransitionTime", 800));
        setCastRotation(mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"castRotation",0.0f));
        setPresoXMargin(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"presoXMargin",20));
        setPresoYMargin(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"presoYMargin",10));
        setPresoInfoAlign(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"presoInfoAlign", Gravity.END));
        setPresoInfoBarAlpha(mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"presoInfoBarAlpha",0.5f));
        setFontSizePresoMax(mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "fontSizePresoMax", 40f));
    }

    private Uri getUriFromString(Context c, MainActivityInterface mainActivityInterface,
                                 String uriString, String backupString) {
        Uri uri = null;
        if (uriString!=null && !uriString.isEmpty()) {
            uri = mainActivityInterface.getStorageAccess().
                    fixLocalisedUri(c, mainActivityInterface, uriString);
        }
        if ((backupString!=null && !backupString.isEmpty()) &&
                (uri==null || !mainActivityInterface.getStorageAccess().uriExists(c,uri))) {
            uri = mainActivityInterface.getStorageAccess().
                    fixLocalisedUri(c,mainActivityInterface,backupString);
        }
        return uri;
    }
}
